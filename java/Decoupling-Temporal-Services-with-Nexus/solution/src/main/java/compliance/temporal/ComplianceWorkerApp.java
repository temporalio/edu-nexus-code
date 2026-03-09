package compliance.temporal;

import compliance.ComplianceChecker;
import compliance.temporal.activity.ComplianceActivityImpl;
import compliance.temporal.workflow.ComplianceWorkflowImpl;
import io.temporal.client.WorkflowClient;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;

/**
 * Compliance team's worker — handles Nexus requests from Payments.
 * Task queue: "compliance-risk"
 *
 * Registers three things:
 *   1. ComplianceWorkflowImpl — the workflow that wraps the activity
 *   2. ComplianceActivityImpl — the activity that runs the checker
 *   3. ComplianceNexusServiceImpl — the Nexus handler that launches the workflow
 */
public class ComplianceWorkerApp {

    public static void main(String[] args) {
        // C — Connect to Temporal
        WorkflowServiceStubs service = WorkflowServiceStubs.newLocalServiceStubs();
        WorkflowClient client = WorkflowClient.newInstance(service);

        // R — Create factory and worker
        WorkerFactory factory = WorkerFactory.newInstance(client);
        Worker worker = factory.newWorker("compliance-risk");

        // W — Wire workflow, activity, and Nexus handler
        worker.registerWorkflowImplementationTypes(ComplianceWorkflowImpl.class);
        worker.registerActivitiesImplementations(new ComplianceActivityImpl(new ComplianceChecker()));
        worker.registerNexusServiceImplementation(new ComplianceNexusServiceImpl());

        // L — Launch
        factory.start();

        System.out.println("=========================================================");
        System.out.println("  Compliance Worker started on: compliance-risk");
        System.out.println("  Registered: ComplianceWorkflow, ComplianceActivity,");
        System.out.println("              ComplianceNexusServiceImpl (sync handler)");
        System.out.println("  Waiting for Nexus requests from Payments team...");
        System.out.println("=========================================================");
    }
}
