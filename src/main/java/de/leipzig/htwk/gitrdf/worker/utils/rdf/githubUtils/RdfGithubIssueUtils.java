package de.leipzig.htwk.gitrdf.worker.utils.rdf.githubUtils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;

import de.leipzig.htwk.gitrdf.worker.utils.rdf.RdfUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;

import static de.leipzig.htwk.gitrdf.worker.service.impl.GithubRdfConversionTransactionService.*;
import static de.leipzig.htwk.gitrdf.worker.utils.rdf.RdfUtils.uri;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RdfGithubIssueUtils {

    private static final String PLATFORM_NS = PLATFORM_NAMESPACE + ":";
    private static final String GH_NS = PLATFORM_GITHUB_NAMESPACE + ":";

    // Base-Classes - Platform
    public static Node rdfTypeProperty() {
        return RdfUtils.uri("rdf:type");
    }

    public static Node titleProperty() {
        return RdfUtils.uri(PLATFORM_NS + "ticketTitle");
    }

    public static Node bodyProperty() {
        return RdfUtils.uri(PLATFORM_NS + "ticketBody");
    }

    // Platform - GitHub

    public static Node issueIdProperty() {
        return RdfUtils.uri(GH_NS + "issueId");
    }

    public static Node issueNumberProperty() {
        return RdfUtils.uri(GH_NS + "issueNumber");
    }

    public static Node stateProperty() {
        return RdfUtils.uri(GH_NS + "issueState");
    }

    public static Node userProperty() {
        return RdfUtils.uri(GH_NS + "user");
    }

    public static Node labelProperty() {
        return RdfUtils.uri(GH_NS + "issueLabel");
    }

    public static Node assigneeProperty() {
        return RdfUtils.uri(GH_NS + "issueAssignee");
    }

    public static Node milestoneProperty() {
        return RdfUtils.uri(GH_NS + "issueMilestone");
    }

    public static Node createdAtProperty() {
        return RdfUtils.uri(GH_NS + "issueCreatedAt");
    }

    public static Node updatedAtProperty() {
        return RdfUtils.uri(GH_NS + "issueUpdatedAt");
    }

    public static Node closedAtProperty() {
        return RdfUtils.uri(GH_NS + "issueClosedAt");
    }

    public static Node issueCreatedByProperty() {
        return RdfUtils.uri("github:issueCreatedBy");
    }

    public static Node issueUpdatedByProperty() {
        return RdfUtils.uri("github:issueUpdatedBy");
    }

    public static Node issueClosedByProperty() {
        return RdfUtils.uri("github:issueClosedBy");
    }

    // Existing Triple Creator Methods

    public static Triple createRdfTypeProperty(String issueUri) {
        return Triple.create(RdfUtils.uri(issueUri), rdfTypeProperty(), RdfUtils.uri("github:GithubIssue"));
    }

    public static Triple createIssueIdProperty(String issueUri, long id) {
        return Triple.create(RdfUtils.uri(issueUri), issueIdProperty(), RdfUtils.stringLiteral(Long.toString(id)));
    }

    public static Triple createIssueNumberProperty(String issueUri, int number) {
        return Triple.create(RdfUtils.uri(issueUri), issueNumberProperty(),
                RdfUtils.stringLiteral(Integer.toString(number)));
    }

    public static Triple createIssueStateProperty(String issueUri, String state) {
        return Triple.create(RdfUtils.uri(issueUri), stateProperty(), uri(GH_NS + state.toLowerCase()));
    }

    public static Triple createIssueTitleProperty(String issueUri, String title) {
        return Triple.create(RdfUtils.uri(issueUri), titleProperty(), RdfUtils.stringLiteral(title));
    }

    public static Triple createIssueBodyProperty(String issueUri, String body) {
        return Triple.create(RdfUtils.uri(issueUri), bodyProperty(), RdfUtils.stringLiteral(body));
    }

    public static Triple createIssueUserProperty(String issueUri, String userUri) {
        return Triple.create(RdfUtils.uri(issueUri), userProperty(), RdfUtils.uri(userUri));
    }

    public static Triple createIssueLabelProperty(String issueUri, String labelUri) {
        return Triple.create(RdfUtils.uri(issueUri), labelProperty(), RdfUtils.uri(labelUri));
    }

    public static Triple createIssueAssigneeProperty(String issueUri, String userUri) {
        return Triple.create(RdfUtils.uri(issueUri), assigneeProperty(), RdfUtils.uri(userUri));
    }

    public static Triple createIssueMilestoneProperty(String issueUri, String milestoneUri) {
        return Triple.create(RdfUtils.uri(issueUri), milestoneProperty(), RdfUtils.uri(milestoneUri));
    }

    public static Triple createIssueCreatedAtProperty(String issueUri, LocalDateTime createdAtDateTime) {
        return Triple.create(RdfUtils.uri(issueUri), createdAtProperty(), RdfUtils.dateTimeLiteral(createdAtDateTime));
    }

    public static Triple createIssueUpdatedAtProperty(String issueUri, LocalDateTime updatedAtDateTime) {
        return Triple.create(RdfUtils.uri(issueUri), updatedAtProperty(), RdfUtils.dateTimeLiteral(updatedAtDateTime));
    }

    public static Triple createIssueClosedAtProperty(String issueUri, LocalDateTime closedAtDateTime) {
        return Triple.create(RdfUtils.uri(issueUri), closedAtProperty(), RdfUtils.dateTimeLiteral(closedAtDateTime));
    }

    public static Triple createIssueCommitEntryProperty(String issueUri, Node commitEntryNode) {
        return Triple.create(RdfUtils.uri(issueUri), RdfUtils.uri(GH_NS + "merge"), commitEntryNode);
    }

    public static Triple createIssueCreatedByProperty(String issueUri, String userUri) {
        return Triple.create(RdfUtils.uri(issueUri), issueCreatedByProperty(), RdfUtils.uri(userUri));
    }

    public static Triple createIssueUpdatedByProperty(String issueUri, String userUri) {
        return Triple.create(RdfUtils.uri(issueUri), issueUpdatedByProperty(), RdfUtils.uri(userUri));
    }

    public static Triple createIssueClosedByProperty(String issueUri, String userUri) {
        return Triple.create(RdfUtils.uri(issueUri), issueClosedByProperty(), RdfUtils.uri(userUri));
    }

}