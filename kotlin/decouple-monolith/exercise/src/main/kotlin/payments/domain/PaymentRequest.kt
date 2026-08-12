package payments.domain

/** [GIVEN] A payment transaction to be processed. */
data class PaymentRequest(
    var transactionId: String = "",
    var amount: Double = 0.0,
    var currency: String = "",
    var senderCountry: String = "",
    var receiverCountry: String = "",
    var description: String = "",
    var senderAccount: String = "",
    var receiverAccount: String = "",
)
