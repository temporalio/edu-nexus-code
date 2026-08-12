package payments.temporal.activity

import payments.PaymentGateway
import payments.domain.PaymentRequest

/** [GIVEN] Payment Activity implementation. Thin wrapper around PaymentGateway. */
class PaymentActivityImpl(private val paymentGateway: PaymentGateway) : PaymentActivity {

    override fun validatePayment(request: PaymentRequest): Boolean =
        paymentGateway.validatePayment(request)

    override fun executePayment(request: PaymentRequest): String =
        paymentGateway.executePayment(request)
}
