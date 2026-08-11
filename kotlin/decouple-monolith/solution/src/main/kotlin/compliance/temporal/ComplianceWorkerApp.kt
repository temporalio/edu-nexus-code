package compliance.temporal

import compliance.ComplianceChecker
import compliance.temporal.activity.ComplianceActivityImpl
import compliance.temporal.workflow.ComplianceWorkflowImpl
import io.temporal.client.WorkflowClient
import io.temporal.client.WorkflowClientOptions
import io.temporal.serviceclient.WorkflowServiceStubs
import io.temporal.worker.WorkerFactory

/**
 * The Compliance team's Worker. Handles Nexus requests from Payments.
 *
 * The Task Queue MUST match --target-task-queue on the Nexus Endpoint.
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

    // Worker (newWorker): poll the Task Queue the Endpoint points at.
    val factory = WorkerFactory.newInstance(client)
    val taskQueue = "compliance-risk"
    val worker = factory.newWorker(taskQueue)

    // Register (register*): everything this Worker is responsible for. The Nexus handler
    // is what makes this team callable from Payments. Without it the Worker still starts,
    // it just never answers a Nexus Operation.
    worker.registerWorkflowImplementationTypes(ComplianceWorkflowImpl::class.java)
    worker.registerActivitiesImplementations(ComplianceActivityImpl(ComplianceChecker()))
    worker.registerNexusServiceImplementation(ComplianceNexusServiceImpl())

    // Start (start): begin polling.
    factory.start()

    println("=========================================================")
    println("  Compliance Worker started on: $taskQueue")
    println("  Namespace: compliance-namespace")
    println("  Registered: ComplianceWorkflow, ComplianceActivity, ComplianceNexusService")
    println("=========================================================")
}
