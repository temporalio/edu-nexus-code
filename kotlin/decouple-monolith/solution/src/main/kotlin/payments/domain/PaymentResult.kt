package payments.domain

/**
 * [GIVEN] Result of a payment Workflow execution.
 *
 * status values:
 *   "COMPLETED"           payment processed successfully
 *   "REJECTED"            failed payment validation
 *   "DECLINED_COMPLIANCE" compliance check returned approved=false
 *   "FAILED"              unexpected error
 */
data class PaymentResult(
    var success: Boolean = false,
    var transactionId: String = "",
    var status: String = "",
    var riskLevel: String? = null,
    var explanation: String? = null,
    var confirmationNumber: String? = null,
    var error: String? = null,
)
