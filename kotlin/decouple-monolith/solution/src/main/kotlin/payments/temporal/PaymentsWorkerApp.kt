package payments.temporal

import io.temporal.client.WorkflowClient
import io.temporal.client.WorkflowClientOptions
import io.temporal.serviceclient.WorkflowServiceStubs
import io.temporal.worker.WorkerFactory
import io.temporal.worker.WorkflowImplementationOptions
import io.temporal.workflow.NexusServiceOptions
import payments.PaymentGateway
import payments.Shared
import payments.temporal.activity.PaymentActivityImpl

/**
 * DECOUPLED VERSION. Payments Worker with the Nexus Endpoint mapping.
 *
 * Changes from the monolith:
 *   1. Workflows registered with NexusServiceOptions, mapping the contract to an Endpoint
 *   2. ComplianceActivityImpl registration removed, it lives on the Compliance Worker now
 */
fun main() {
    // Client (newInstance): connect, scoped to the Payments team's own Namespace.
    val service = WorkflowServiceStubs.newLocalServiceStubs()
    val client = WorkflowClient.newInstance(
        service,
        WorkflowClientOptions.newBuilder()
            .setNamespace("payments-namespace")
            .build(),
    )

    // Worker (newWorker): poll the Payments Task Queue.
    val factory = WorkerFactory.newInstance(client)
    val worker = factory.newWorker(Shared.TASK_QUEUE)

    // Register (register*): the Endpoint name lives here, not in the Workflow. This is
    // the seam that lets Compliance move Namespace or Task Queue without Payments
    // changing a line of code.
    worker.registerWorkflowImplementationTypes(
        WorkflowImplementationOptions.newBuilder()
            .setNexusServiceOptions(
                mapOf(
                    "ComplianceNexusService" to NexusServiceOptions.newBuilder()
                        .setEndpoint("compliance-endpoint")
                        .build()
                )
            )
            .build(),
        PaymentProcessingWorkflowImpl::class.java,
        ReviewCallerWorkflowImpl::class.java,
    )

    // Payment Activities only. Compliance moved to its own Worker.
    worker.registerActivitiesImplementations(PaymentActivityImpl(PaymentGateway()))

    // Start (start): begin polling.
    factory.start()

    println("=========================================================")
    println("  Payments Worker started on: ${Shared.TASK_QUEUE}")
    println("  Namespace: payments-namespace")
    println("  Registered: PaymentProcessingWorkflow, ReviewCallerWorkflow, PaymentActivity")
    println("  Nexus: ComplianceNexusService -> compliance-endpoint")
    println("=========================================================")
}
