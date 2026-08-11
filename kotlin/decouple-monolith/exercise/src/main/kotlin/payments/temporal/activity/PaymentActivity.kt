package payments.temporal.activity

import io.temporal.activity.ActivityInterface
import io.temporal.activity.ActivityMethod
import payments.domain.PaymentRequest

@ActivityInterface
interface PaymentActivity {
    @ActivityMethod
    fun validatePayment(request: PaymentRequest): Boolean

    @ActivityMethod
    fun executePayment(request: PaymentRequest): String
}
