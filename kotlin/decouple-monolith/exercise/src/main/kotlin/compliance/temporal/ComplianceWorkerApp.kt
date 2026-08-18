package compliance.temporal

import compliance.ComplianceChecker
import compliance.temporal.activity.ComplianceActivityImpl
import compliance.temporal.workflow.ComplianceWorkflowImpl
import io.temporal.client.WorkflowClient
import io.temporal.client.WorkflowClientOptions
import io.temporal.serviceclient.WorkflowServiceStubs
import io.temporal.worker.WorkerFactory

/**
 * The Compliance team's Worker. This process is the only place their code runs.
 *
 * The connection and the Worker itself are set up for you. What is missing is the part
 * that decides what this Worker can actually do: see TODO 7 below.
 */
fun main() {
    // Client (newInstance): connect, scoped to the Compliance team's own Namespace.
    val service = WorkflowServiceStubs.newLocalServiceStubs()
    val client = WorkflowClient.newInstance(
        service,
        WorkflowClientOptions.newBuilder()
            .setNamespace("compliance-namespace")
            .build(),
    )

    // Worker (newWorker): poll the Task Queue the Endpoint will point at. This value
    // MUST match the --target-task-queue you give the Nexus Endpoint, or the Endpoint
    // routes work to a queue nobody is polling.
    val factory = WorkerFactory.newInstance(client)
    val taskQueue = "compliance-risk"
    val worker = factory.newWorker(taskQueue)

    // ── TODO 7 ───────────────────────────────────────────────────────────────────
    // Register (register*): a Worker only handles work it has been told about. This one
    // is responsible for three things, each a separate call on `worker`:
    //
    //   1. The Workflow that carries out a compliance check.
    //   2. The Activity that Workflow calls to run the actual risk assessment. This one
    //      is not registered by type: the Worker needs a constructed instance, and that
    //      instance needs a ComplianceChecker to do its work.
    //   3. The Nexus handler, which is what makes this team callable by Payments at all.
    //      Leave this one out and the Worker still starts, Payments' calls just go
    //      unanswered forever. That is the failure you are guarding against.
    //
    // Each call name starts with `register`.
    //
    // Stuck? "Ask AI" on https://docs.temporal.io:
    //   "what do I register on a Java Worker that handles Nexus Operations?"


    // Start (start): begin polling.
    factory.start()

    println("=========================================================")
    println("  Compliance Worker started on: $taskQueue")
    println("  Namespace: compliance-namespace")
    println("=========================================================")
}
