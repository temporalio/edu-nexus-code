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
 * TODO 5 has three parts, each marked at the line it applies to. Do them after TODO 4
 * has moved the Workflow onto a Nexus stub.
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

    // ── TODO 5a + 5b ─────────────────────────────────────────────────────────────
    // Replace the single `worker.registerWorkflowImplementationTypes(...)` line below.
    // As written it has two problems.
    //
    // 5a. It says nothing about WHERE the ComplianceNexusService contract lives. The
    //     Workflow you just edited names the contract but not its address, so this is
    //     where the address goes. There is another version of
    //     registerWorkflowImplementationTypes that takes WorkflowImplementationOptions
    //     as its FIRST argument. Use it to attach Nexus Service options that map the
    //     service name "ComplianceNexusService" to the Endpoint "compliance-endpoint".
    //     That service name is a plain String, not a class.
    //
    // 5b. It registers only PaymentProcessingWorkflowImpl. Add
    //     ReviewCallerWorkflowImpl to the same call. It calls the same contract, so it
    //     needs the same mapping. Challenge 5 uses it to approve a payment.
    //
    // Shape of what you write:
    //
    //     worker.registerWorkflowImplementationTypes(
    //         WorkflowImplementationOptions.newBuilder()
    //             // map "ComplianceNexusService" -> Endpoint "compliance-endpoint"
    //             .build(),
    //         PaymentProcessingWorkflowImpl::class.java,
    //         ReviewCallerWorkflowImpl::class.java,
    //     )
    //
    // Stuck? "Ask AI" on https://docs.temporal.io:
    //   "how do I map a Nexus Service to an Endpoint on a Java Worker?"
    worker.registerWorkflowImplementationTypes(PaymentProcessingWorkflowImpl::class.java)

    worker.registerActivitiesImplementations(PaymentActivityImpl(PaymentGateway()))

    // ── TODO 5c ──────────────────────────────────────────────────────────────────
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
