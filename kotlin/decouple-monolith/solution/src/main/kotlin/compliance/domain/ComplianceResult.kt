package compliance.domain

/**
 * [GIVEN] Result of a compliance check.
 * Returned by the Compliance team to the Payments team.
 *
 * approved    true means proceed with the payment, false means block it
 * riskLevel   "LOW", "MEDIUM", or "HIGH"
 * explanation one-line explanation of the decision
 */
data class ComplianceResult(
    var transactionId: String = "",
    var approved: Boolean = false,
    var riskLevel: String = "",
    var explanation: String = "",
)
