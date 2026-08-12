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
 * You have two TODOs here: 10 and 11. Do them after TODO 8, which moves the Workflow
 * onto a Nexus stub.
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
    // THE LINE TO CHANGE is the very next line of code, the one starting with
    // `worker.registerWorkflowImplementationTypes(`. Today it says: run this one
    // Workflow. You need it to say two more things.
    //
    // 1. Where compliance lives.
    //    In PaymentProcessingWorkflowImpl you created a stub for
    //    ComplianceNexusService. That stub knows the service by NAME but has no idea
    //    which Worker answers it. So somewhere you have to write down the pair:
    //
    //        the name "ComplianceNexusService"  ->  the Endpoint "compliance-endpoint"
    //
    //    That pair is all "Endpoint mapping" means. It goes here, on the Worker, and
    //    NOT in the Workflow. That is what keeps the Workflow reusable: change the
    //    Endpoint later and no Workflow code changes.
    //
    // 2. That ReviewCallerWorkflowImpl exists.
    //    It calls the same service, so it needs the same pairing. Challenge 5 uses it.
    //
    // COPY THIS over the line below, and fill in the one blank marked TODO:
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
    // Stuck on the blank? "Ask AI" on https://docs.temporal.io:
    //   "how do I set the Endpoint on NexusServiceOptions in Java?"
    worker.registerWorkflowImplementationTypes(PaymentProcessingWorkflowImpl::class.java)

    worker.registerActivitiesImplementations(PaymentActivityImpl(PaymentGateway()))

    // ── TODO 11 ───────────────────────────────────────────────────────────────────
    // Delete the `worker.registerActivitiesImplementations(ComplianceActivityImpl(...))`
    // line below, plus the two compliance imports at the top of this file
    // (ComplianceChecker and ComplianceActivityImpl).
    //
    // Compliance code no longer runs in this process once it is reachable over Nexus.
    // This deletion is the decoupling. After it, a bug in Compliance code can no
    // longer crash this process.
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
