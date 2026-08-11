package compliance.domain

/**
 * [GIVEN] Input to the compliance check.
 * Sent by the Payments team to the Compliance team.
 *
 * Every property has a default value, so Kotlin also emits a no-arg constructor.
 * That is what lets the Java SDK's default Jackson converter deserialize this type
 * across the Nexus boundary without any custom DataConverter wiring.
 */
data class ComplianceRequest(
    var transactionId: String = "",
    var amount: Double = 0.0,
    var senderCountry: String = "",
    var receiverCountry: String = "",
    var description: String = "",
)
