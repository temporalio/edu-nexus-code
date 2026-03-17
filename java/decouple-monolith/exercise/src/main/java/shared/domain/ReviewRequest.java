package shared.domain;

/**
 * [GIVEN] Request data for submitting a human review decision via Nexus.
 *
 * Used by the ReviewCallerWorkflow to call the submitReview Nexus operation.
 * The Compliance team's sync Nexus handler receives this and sends a Workflow
 * Update to the running ComplianceWorkflow.
 */
public class ReviewRequest {

    private String transactionId;
    private boolean approved;
    private String explanation;

    public ReviewRequest() {}

    public ReviewRequest(String transactionId, boolean approved, String explanation) {
        this.transactionId = transactionId;
        this.approved = approved;
        this.explanation = explanation;
    }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public boolean isApproved() { return approved; }
    public void setApproved(boolean approved) { this.approved = approved; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
}
