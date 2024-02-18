package de.leipzig.htwk.gitrdf.worker.service.impl;

import de.leipzig.htwk.gitrdf.worker.config.GithubConfig;
import de.leipzig.htwk.gitrdf.worker.database.entity.GithubRepositoryOrderEntity;
import de.leipzig.htwk.gitrdf.worker.database.entity.enums.GitRepositoryOrderStatus;
import de.leipzig.htwk.gitrdf.worker.database.entity.lob.GithubRepositoryOrderEntityLobs;
import de.leipzig.htwk.gitrdf.worker.utils.GitUtils;
import de.leipzig.htwk.gitrdf.worker.utils.ZipUtils;
import de.leipzig.htwk.gitrdf.worker.utils.rdf.RdfCommitUtils;
import jakarta.persistence.EntityManager;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFWriter;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.hibernate.engine.jdbc.BlobProxy;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
public class GithubRdfConversionTransactionService {

    private static final int TWENTY_FIVE_MEGABYTE = 1024 * 1024 * 25;

    public static final String GIT_URI = "git://";

    public static final String GIT_NAMESPACE = "git";

    private final GithubHandlerService githubHandlerService;

    private final GithubConfig githubConfig;

    private final EntityManager entityManager;

    private final int commitsPerIteration;

    public GithubRdfConversionTransactionService(
            GithubHandlerService githubHandlerService,
            EntityManager entityManager,
            GithubConfig githubConfig,
            @Value("${worker.commits-per-iteration}") int commitsPerIteration) throws IOException {

        this.githubHandlerService = githubHandlerService;
        this.githubConfig = githubConfig;
        this.entityManager = entityManager;
        this.commitsPerIteration = commitsPerIteration;
    }

    // TODO (ccr): Refactor -> code is currently not in use -> maybe delete or refactor to a cleaner state
    @Transactional(rollbackFor = {IOException.class, GitAPIException.class, URISyntaxException.class, InterruptedException.class}) // Runtime-Exceptions are rollbacked by default; Checked-Exceptions not
    public InputStream performGithubRepoToRdfConversionAndReturnCloseableInputStream(
            long id, File rdfTempFile) throws IOException, GitAPIException, URISyntaxException, InterruptedException {

        GitHub gitHubHandle = githubHandlerService.getGithubHandle();

        // du kriegst die .git nicht als Teil der Zip ->
        //gitHubHandle.getRepository("").listCommits()

        GithubRepositoryOrderEntityLobs githubRepositoryOrderEntityLobs
                = entityManager.find(GithubRepositoryOrderEntityLobs.class, id);

        GithubRepositoryOrderEntity githubRepositoryOrderEntity
                = entityManager.find(GithubRepositoryOrderEntity.class, id);

        String owner = githubRepositoryOrderEntity.getOwnerName();
        String repo = githubRepositoryOrderEntity.getRepositoryName();

        GHRepository targetRepo = getGithubRepositoryHandle(owner, repo, gitHubHandle);

        File gitFile = getDotGitFileFromGithubRepositoryHandle(targetRepo, id, owner, repo);

        InputStream needsToBeClosedOutsideOfTransaction
                = writeRdf(gitFile, githubRepositoryOrderEntity, githubRepositoryOrderEntityLobs, rdfTempFile);

        githubRepositoryOrderEntity.setStatus(GitRepositoryOrderStatus.DONE);

        return needsToBeClosedOutsideOfTransaction;
    }

    @Transactional(rollbackFor = {IOException.class, GitAPIException.class}) // Runtime-Exceptions are rollbacked by default; Checked-Exceptions not
    public InputStream performGithubRepoToRdfConversionWithGitCloningLogicAndReturnCloseableInputStream(
            long id, File gitWorkingDirectory, File rdfTempFile) throws IOException, GitAPIException {

        Git gitHandler = null;

        try {

            GithubRepositoryOrderEntityLobs githubRepositoryOrderEntityLobs
                    = entityManager.find(GithubRepositoryOrderEntityLobs.class, id);

            GithubRepositoryOrderEntity githubRepositoryOrderEntity
                    = entityManager.find(GithubRepositoryOrderEntity.class, id);

            String owner = githubRepositoryOrderEntity.getOwnerName();
            String repo = githubRepositoryOrderEntity.getRepositoryName();

            gitHandler = performGitClone(owner, repo, gitWorkingDirectory);

            InputStream needsToBeClosedOutsideOfTransaction
                    = writeRdf(gitHandler, githubRepositoryOrderEntity, githubRepositoryOrderEntityLobs, rdfTempFile);

            githubRepositoryOrderEntity.setStatus(GitRepositoryOrderStatus.DONE);

            return needsToBeClosedOutsideOfTransaction;

        } finally {

            if (gitHandler != null) gitHandler.close();

        }

    }

    private Git performGitClone(String ownerName, String repositoryName, File gitWorkingDirectory) throws GitAPIException {

        String gitRepoTargetUrl = String.format("https://github.com/%s/%s.git", ownerName, repositoryName);

        return Git.cloneRepository()
                .setURI(gitRepoTargetUrl)
                .setDirectory(gitWorkingDirectory)
                .setCredentialsProvider(new UsernamePasswordCredentialsProvider(
                        githubConfig.getGithubSystemUserName(),
                        githubConfig.getGithubSystemUserPersonalAccessToken())) // ---> maybe we dont even need this for public repositories?
                .call();
    }

    private void throwExceptionIfThereIsNotExactlyOneFileInTheExtractedZipDirectory(
            File[] filesOfExtractedZipDirectory,
            long entityId,
            String ownerName,
            String repoName) {

        int fileAmountInExtractedZipDirectory = filesOfExtractedZipDirectory.length;

        throwExceptionIfThereAreNoFilesInTheExtractedZipDirectory(
                fileAmountInExtractedZipDirectory, entityId, ownerName, repoName);

        throwExceptionIfThereIsMoreThanOneFileInTheExtractedZipDirectory(
                filesOfExtractedZipDirectory, entityId, ownerName, repoName);
    }

    private void throwExceptionIfThereAreNoFilesInTheExtractedZipDirectory(
            int fileAmountInExtractedZipDirectory,
            long entityId,
            String ownerName,
            String repoName) {

        if (fileAmountInExtractedZipDirectory < 1) {
            String exceptionMessage = String.format("Error extracting files from zip for github repository with id " +
                    "'%d', owner '%s' and repository name '%s'. Expected exactly one single file in the " +
                    "extracted github repository zip. But found no files instead.", entityId, ownerName, repoName);

            throw new RuntimeException(exceptionMessage);
        }
    }

    private void throwExceptionIfThereIsMoreThanOneFileInTheExtractedZipDirectory(
            File[] filesOfExtractedZipDirectory,
            long entityId,
            String ownerName,
            String repoName) {

        int fileAmountInExtractedZipDirectory = filesOfExtractedZipDirectory.length;

        if (fileAmountInExtractedZipDirectory > 1) {
            String exceptionMessage = String.format("Error extracting files from zip for github repository with id " +
                            "'%d', owner '%s' and repository name '%s'. Expected only one single file in the " +
                            "extracted github repository zip. But found %d files. Names of the files are: '%s'",
                    entityId,
                    ownerName,
                    repoName,
                    fileAmountInExtractedZipDirectory,
                    getNamesOfFiles(filesOfExtractedZipDirectory));

            throw new RuntimeException(exceptionMessage);
        }
    }

    private String getNamesOfFiles(File[] files) {

        StringBuilder fileNameBuilder = new StringBuilder("[");

        boolean firstEntry = true;

        for (File file : files) {

            if (firstEntry) {
                fileNameBuilder.append(String.format("%s", file.getName()));
                firstEntry = false;
            } else {
                fileNameBuilder.append(String.format(", %s", file.getName()));
            }

        }

        fileNameBuilder.append("]");
        return fileNameBuilder.toString();
    }

    private GHRepository getGithubRepositoryHandle(String ownerName, String repositoryName, GitHub gitHubHandle) throws IOException {
        String targetRepoName = String.format("%s/%s", ownerName, repositoryName);
        return gitHubHandle.getRepository(targetRepoName);
    }

    private File getDotGitFileFromGithubRepositoryHandle(
            GHRepository githubRepositoryHandle,
            long entityId,
            String ownerName,
            String repositoryName) throws IOException {

        File extractedZipFileDirectory = githubRepositoryHandle.readZip(
                input -> ZipUtils.extractZip(input, TWENTY_FIVE_MEGABYTE),
                null);

        File[] filesOfExtractedZipDirectory = extractedZipFileDirectory.listFiles();

        if (filesOfExtractedZipDirectory == null) {

            String exceptionMessage = String.format("Error while trying to list files of extracted zip file directory. " +
                            "Returned value is null. Id is: '%d', owner is: '%s' and repository name is: '%s'",
                    entityId, ownerName, repositoryName);

            throw new RuntimeException(exceptionMessage);
        }

        throwExceptionIfThereIsNotExactlyOneFileInTheExtractedZipDirectory(
                filesOfExtractedZipDirectory, entityId, ownerName, repositoryName);

        File parentFolderOfDotGitFile = filesOfExtractedZipDirectory[0];

        return GitUtils.getDotGitFileFromParentDirectoryFileAndThrowExceptionIfNoOrMoreThanOneExists(
                parentFolderOfDotGitFile);
    }

    // TODO (ccr): duplicate code -> change that!
    private InputStream writeRdf(
            File gitFile,
            GithubRepositoryOrderEntity entity,
            GithubRepositoryOrderEntityLobs entityLobs,
            File rdfTempFile) throws GitAPIException, IOException {

        Repository gitRepository = new FileRepositoryBuilder().setGitDir(gitFile).build();
        Git gitHandler = new Git(gitRepository);

        return writeRdf(gitHandler, entity, entityLobs, rdfTempFile);
    }

    private InputStream writeRdf(
            Git gitHandler,
            GithubRepositoryOrderEntity entity,
            GithubRepositoryOrderEntityLobs entityLobs,
            File rdfTempFile) throws GitAPIException, IOException {

        try (OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(rdfTempFile))) {

            StreamRDF writer = StreamRDFWriter.getWriterStream(outputStream, RDFFormat.TURTLE_BLOCKS);
            //writer.prefix(GIT_NAMESPACE, GIT_URI);
            //writer.prefix("xsd", ""); TODO (ccr): eventuell xsd präfix hinzufügen, wenn das apache jena nicht von selbst macht

            for (int iteration = 0; iteration < Integer.MAX_VALUE; iteration++) {

                int skipCount = calculateSkipCountAndThrowExceptionIfIntegerOverflowIsImminent(iteration, commitsPerIteration);

                Iterable<RevCommit> commits = gitHandler.log().setSkip(skipCount).setMaxCount(commitsPerIteration).call();

                boolean finished = true;

                writer.start();

                for (RevCommit commit : commits) {

                    finished = false;

                    String gitHash = commit.getId().name();

                    String commitUri = getGithubCommitUri(entity.getOwnerName(), entity.getRepositoryName(), gitHash);

                    writer.triple(RdfCommitUtils.createCommitHashProperty(commitUri, gitHash));

                    writer.triple(RdfCommitUtils.createAuthorNameProperty(commitUri, commit.getAuthorIdent().getName()));

                    writer.triple(RdfCommitUtils.createAuthorEmailProperty(commitUri, commit.getAuthorIdent().getEmailAddress()));

                    Instant instant = Instant.ofEpochSecond(commit.getCommitTime());
                    LocalDateTime commitDateTime = instant.atZone(ZoneId.of("Europe/Berlin")).toLocalDateTime();

                    writer.triple(RdfCommitUtils.createAuthorDateProperty(commitUri, commitDateTime));

                    writer.triple(RdfCommitUtils.createCommitDateProperty(commitUri, commitDateTime));

                    writer.triple(RdfCommitUtils.createCommitterNameProperty(commitUri, commit.getCommitterIdent().getName()));

                    writer.triple(RdfCommitUtils.createCommitterEmailProperty(commitUri, commit.getCommitterIdent().getEmailAddress()));

                    writer.triple(RdfCommitUtils.createCommitMessageProperty(commitUri, commit.getFullMessage()));

                    // No possible solution found yet for merge commits -> maybe traverse to parent? Maybe both
                    //Property mergeCommitProperty = rdfModel.createProperty(gitUri + "MergeCommit");
                    //commit.getParent()
                }

                writer.finish();

                if (finished) {
                    break;
                }

                if (iteration + 1 == Integer.MAX_VALUE) {
                    throw new RuntimeException(
                            "While iterating through commit log and transforming log to rdf: Exceeded iteration max count (integer overflow)");
                }

            }

        }

        BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(rdfTempFile));

        entityLobs.setRdfFile(BlobProxy.generateProxy(bufferedInputStream, rdfTempFile.length()));

        return bufferedInputStream;
    }

    private int calculateSkipCountAndThrowExceptionIfIntegerOverflowIsImminent(
            int currentIteration, int commitsPerIteration) {

        long longCurrentIteration = (long) currentIteration;
        long longCommitsPerIteration = (long) commitsPerIteration;

        long skips = longCurrentIteration * longCommitsPerIteration;

        if (skips > Integer.MAX_VALUE) {
            throw new RuntimeException("While iterating through commit log and transforming log to rdf: Exceeded skip max count (integer overflow)");
        }

        return (int) skips;
    }

    private String getGithubCommitUri(String owner, String repository, String commitHash) {
        return "https://github.com/" + owner + "/" + repository + "/commit/" + commitHash;
    }

}
