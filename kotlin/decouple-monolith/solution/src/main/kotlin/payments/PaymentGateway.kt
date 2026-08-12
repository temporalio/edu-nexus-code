package payments

import payments.domain.PaymentRequest
import kotlin.random.Random

/**
 * [GIVEN] Simulated payment gateway. In production this would call Stripe, PayPal, SWIFT.
 *
 * The 10% failure rate is deliberate: it shows Temporal retrying an Activity for free.
 * Because of it, never assert on a clean first attempt, assert on final Workflow state.
 */
class PaymentGateway {

    fun validatePayment(request: PaymentRequest): Boolean {
        if (request.amount <= 0) {
            println("[PaymentGateway] REJECTED: Invalid amount for ${request.transactionId}")
            return false
        }
        if (request.senderAccount.isEmpty() || request.receiverAccount.isEmpty()) {
            println("[PaymentGateway] REJECTED: Missing account info for ${request.transactionId}")
            return false
        }
        println("[PaymentGateway] Validation passed for ${request.transactionId}")
        return true
    }

    fun executePayment(request: PaymentRequest): String {
        println(
            "[PaymentGateway] Processing ${request.transactionId}" +
                " | $${"%.2f".format(request.amount)}" +
                " | ${request.senderCountry} -> ${request.receiverCountry}"
        )

        // Simulate processing time
        Thread.sleep(500 + Random.nextLong(500))

        // Simulate occasional gateway failures. Temporal retries automatically.
        if (Random.nextDouble() < 0.10) {
            throw RuntimeException(
                "Payment gateway timeout for ${request.transactionId}, connection to banking network failed"
            )
        }

        // Derived from the transaction ID, not the clock, so a retry of this Activity
        // returns the SAME confirmation number instead of minting a second one. With a
        // 10% failure rate above, a timestamp here would mean retries look like distinct
        // payments. Idempotency is what makes an at-least-once retry safe.
        val confirmationNumber = "CONF-${request.transactionId}"
        println("[PaymentGateway] Payment executed: $confirmationNumber")
        return confirmationNumber
    }
}
