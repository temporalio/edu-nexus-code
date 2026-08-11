package shared.domain

/**
 * [GIVEN] Request data for submitting a human review decision via Nexus.
 *
 * Used by the ReviewCallerWorkflow to call the submitReview Nexus Operation.
 * The Compliance team's sync Nexus handler receives this and sends a Workflow
 * Update to the running ComplianceWorkflow.
 */
data class ReviewRequest(
    var transactionId: String = "",
    var approved: Boolean = false,
    var explanation: String = "",
)
