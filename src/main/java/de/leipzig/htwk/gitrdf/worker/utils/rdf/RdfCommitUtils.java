package de.leipzig.htwk.gitrdf.worker.utils.rdf;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.patch.FileHeader;

import java.time.LocalDateTime;

import static de.leipzig.htwk.gitrdf.worker.utils.rdf.RdfUtils.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RdfCommitUtils {

    // org.apache.jena.datatypes.xsd.XSDDatatype -> static xsd Datatype collection from apache jena

    // somewhat of an applicable base uri to the contents of git: https://git-scm.com/docs/gitglossary

    public static Node commitHashProperty() {
        return uri("https://git-scm.com/docs/gitglossary#Documentation/gitglossary.txt-aiddefSHA1aSHA-1");
    }

    public static Node authorNameProperty() {
       return uri("git://AuthorName");
    }

    public static Node authorEmailProperty() {
        return uri("git://AuthorEmail");
    }

    public static Node authorDateProperty() {
        return uri("git://AuthorDate");
    }

    public static Node commitDateProperty() {
        return uri("git://CommitDate");
    }

    public static Node committerNameProperty() {
        return uri("git://CommitterName");
    }

    public static Node committerEmailProperty() {
        return uri("git://CommitterEmail");
    }

    public static Node commitMessageProperty() {
        return uri("git://CommitMessage");
    }

    public static Node commitBranchNameProperty() {
        return uri("git://BranchName");
    }

    public static Node commitDiffEntryEditTypeProperty() {
        return uri("git://ChangeType");
    }

    public static Node commitResource() {
        return uri("git://Commit");
    }

    public static Node branchResource() {
        return uri("git://Branch");
    }

    public static Node tagResource() {
        return uri("git://Tag");
    }

    public static Node commitDiffEntryResource() {
        return uri("git://CommitDiffEntry");
    }

    public static Node commitDiffEntryOldFileNameProperty() {
        return uri("git://CommitDiffEntryOldFileName");
    }

    public static Node commitDiffEntryNewFileNameProperty() {
        return uri("git://CommitDiffEntryNewFileName");
    }

    public static Node commitDiffEditResource() {
        return uri("git://CommitDiffEdit");
    }

    public static Node commitDiffEditTypeProperty() {
        return uri("git://CommitDiffEditType");
    }

    public static Node editOldLineNumberBeginProperty() { return uri("git://EditOldLineNumberBegin"); }

    public static Node editNewLineNumberBeginProperty() { return uri("git://EditNewLineNumberBegin"); }

    public static Node editOldLineNumberEndProperty() { return uri("git://EditOldLineNumberEnd"); }

    public static Node editNewLineNumberEndProperty() { return uri("git://EditNewLineNumberEnd"); }



    public static Triple createCommitHashProperty(String commitUri, String commitHash) {
        return Triple.create(uri(commitUri), commitHashProperty(), stringLiteral(commitHash));
    }

    public static Triple createAuthorNameProperty(String commitUri, String authorNameValue) {
        return Triple.create(uri(commitUri), authorNameProperty(), stringLiteral(authorNameValue));
    }

    public static Triple createAuthorEmailProperty(String commitUri, String authorEmailValue) {
        return Triple.create(uri(commitUri), authorEmailProperty(), stringLiteral(authorEmailValue));
    }

    public static Triple createAuthorDateProperty(String commitUri, LocalDateTime authorDateTimeValue) {
        return Triple.create(uri(commitUri), authorDateProperty(), dateTimeLiteral(authorDateTimeValue));
    }

    public static Triple createCommitDateProperty(String commitUri, LocalDateTime commitDateTimeValue) {
        return Triple.create(uri(commitUri), commitDateProperty(), dateTimeLiteral(commitDateTimeValue));
    }

    public static Triple createCommitterNameProperty(String commitUri, String committerNameValue) {
        return Triple.create(uri(commitUri), committerNameProperty(), stringLiteral(committerNameValue));
    }

    public static Triple createCommitterEmailProperty(String commitUri, String committerEmailValue) {
        return Triple.create(uri(commitUri), committerEmailProperty(), stringLiteral(committerEmailValue));
    }

    public static Triple createCommitMessageProperty(String commitUri, String commitMessageValue) {
        return Triple.create(uri(commitUri), commitMessageProperty(), stringLiteral(commitMessageValue));
    }


    public static Triple createCommitDiffEntryEditTypeProperty(Node diffEntryNode, DiffEntry.ChangeType changeType) {
        return Triple.create(diffEntryNode, commitDiffEntryEditTypeProperty(), stringLiteral(changeType.toString())); // TODO: changetype-literal?
    }

    public static Triple createCommitResource(String commitUri, Node commitNode) {
        return Triple.create(uri(commitUri), commitResource(), commitNode);
    }

    public static Triple createCommitDiffEntryResource(Node commitNode, Node diffEntryNode) {
        return Triple.create(commitNode, commitDiffEntryResource(), diffEntryNode);
    }

    public static Triple createCommitDiffEntryProperty(String commitUri, Node diffEntryNode) {
        return Triple.create(uri(commitUri), commitDiffEntryResource(), diffEntryNode);
    }

    public static Triple createCommitDiffEntryOldFileNameProperty(Node diffEntryNode, FileHeader fileHeader) {
        return Triple.create(diffEntryNode, commitDiffEntryOldFileNameProperty(), stringLiteral(fileHeader.getOldPath()));
    }

    public static Triple createCommitDiffEntryNewFileNameProperty(Node diffEntryNode, FileHeader fileHeader) {
        return Triple.create(diffEntryNode, commitDiffEntryNewFileNameProperty(), stringLiteral(fileHeader.getNewPath()));
    }

    public static Triple createCommitDiffEditResource(Node diffEntryNode, Node diffEditNode) {
        return Triple.create(diffEntryNode, commitDiffEditResource(), diffEditNode);
    }

    public static Triple createCommitDiffEditTypeProperty(Node editNode, Edit.Type editType) {
        return Triple.create(editNode, commitDiffEditTypeProperty(), stringLiteral(editType.toString())); // TODO: edittype-literal?
    }

    public static Triple createEditOldLineNumberBeginProperty(Node editNode, int lineNumberBegin ) {
        return Triple.create(editNode, editOldLineNumberBeginProperty(), longLiteral(lineNumberBegin));
    }

    public static Triple createEditNewLineNumberBeginProperty(Node editNode, int lineNumberBegin ) {
        return Triple.create(editNode, editNewLineNumberBeginProperty(), longLiteral(lineNumberBegin));
    }

    public static Triple createEditOldLineNumberEndProperty(Node editNode, int lineNumberEnd ) {
        return Triple.create(editNode, editOldLineNumberEndProperty(), longLiteral(lineNumberEnd));
    }

    public static Triple createEditNewLineNumberEndProperty(Node editNode, int lineNumberEnd ) {
        return Triple.create(editNode, editNewLineNumberEndProperty(), longLiteral(lineNumberEnd));
    }

    public static Triple createCommitBranchNameProperty(String commitUri, String branchName) {
        return Triple.create(uri(commitUri), commitBranchNameProperty(), stringLiteral(branchName));
    }

    public static Triple createBranchResource(Node branchNode, String branchName) {
        return Triple.create(branchNode, branchResource(), stringLiteral(branchName));
    }

    public static Triple createTagResource(Node tagNode, String tagName) {
        return Triple.create(tagNode, tagResource(), stringLiteral(tagName));
    }
}
