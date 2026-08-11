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
    // 5a. This registration carries no Endpoint mapping. Tell the Worker WHERE the
    //     ComplianceNexusService contract lives. There is an overload of
    //     registerWorkflowImplementationTypes that takes WorkflowImplementationOptions
    //     first: use it to attach Nexus Service options keyed by the service name
    //     "ComplianceNexusService", pointing at the Endpoint "compliance-endpoint".
    //     The key is a plain string, not a class.
    //
    // 5b. It also knows about only one Workflow. Register ReviewCallerWorkflowImpl
    //     alongside PaymentProcessingWorkflowImpl in that same call. Both call the same
    //     contract, so both need the same Endpoint mapping.
    //
    // Stuck? "Ask AI" on https://docs.temporal.io:
    //   "how do I map a Nexus Service to an Endpoint on a Java Worker?"
    worker.registerWorkflowImplementationTypes(PaymentProcessingWorkflowImpl::class.java)

    worker.registerActivitiesImplementations(PaymentActivityImpl(PaymentGateway()))

    // ── TODO 5c ──────────────────────────────────────────────────────────────────
    // Delete this registration, and the two compliance imports at the top of the file.
    // Compliance code no longer runs in this process once it is reachable over Nexus.
    // This deletion IS the decoupling: watch the blast radius shrink.
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
