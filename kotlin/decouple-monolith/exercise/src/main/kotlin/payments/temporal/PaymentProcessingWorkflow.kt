package payments.temporal

import io.temporal.workflow.WorkflowInterface
import io.temporal.workflow.WorkflowMethod
import payments.domain.PaymentRequest
import payments.domain.PaymentResult

@WorkflowInterface
interface PaymentProcessingWorkflow {
    @WorkflowMethod
    fun processPayment(request: PaymentRequest): PaymentResult
}
