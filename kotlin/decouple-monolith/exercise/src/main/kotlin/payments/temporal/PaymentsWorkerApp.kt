package payments.temporal

import compliance.ComplianceChecker
import compliance.temporal.activity.ComplianceActivityImpl
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
 * MONOLITH VERSION. One Worker, one deployment, one blast radius.
 *
 * Right now this single process runs everything:
 *   - PaymentProcessingWorkflow   (Payments team)
 *   - PaymentActivity             (Payments team)
 *   - ComplianceActivity          (Compliance team's code, running in Payments' process)
 *
 * That last line is the problem. A bug in compliance code takes payments down with it.
 *
 * Do TODO 10 after TODOs 8 and 9, which move the Workflow onto a Nexus stub.
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

    // ── TODO 10 ───────────────────────────────────────────────────────────────────
    // Two edits, both on the registration lines below.
    //
    // ONE. Replace the registerWorkflowImplementationTypes line with this, filling in
    // the single blank:
    //
    //     worker.registerWorkflowImplementationTypes(
    //         WorkflowImplementationOptions.newBuilder()
    //             .setNexusServiceOptions(
    //                 mapOf(
    //                     "ComplianceNexusService" to NexusServiceOptions.newBuilder()
    //                         // TODO: point this at the Endpoint "compliance-endpoint"
    //                         .build()
    //                 )
    //             )
    //             .build(),
    //         PaymentProcessingWorkflowImpl::class.java,
    //         ReviewCallerWorkflowImpl::class.java,
    //     )
    //
    // TWO. Delete the ComplianceActivityImpl registration, and the two compliance
    // imports at the top of this file.
    //
    // Stuck on the blank? "Ask AI" on https://docs.temporal.io:
    //   "how do I set the Endpoint on NexusServiceOptions in Java?"
    worker.registerWorkflowImplementationTypes(PaymentProcessingWorkflowImpl::class.java)

    worker.registerActivitiesImplementations(PaymentActivityImpl(PaymentGateway()))

    worker.registerActivitiesImplementations(ComplianceActivityImpl(ComplianceChecker()))

    // Start (start): begin polling.
    factory.start()

    println("=========================================================")
    println("  Payments Worker started on: ${Shared.TASK_QUEUE}")
    println("  Namespace: payments-namespace")
    println("  Registered: PaymentProcessingWorkflow, PaymentActivity")
    println("              ComplianceActivity (monolith, will decouple)")
    println("=========================================================")
}
