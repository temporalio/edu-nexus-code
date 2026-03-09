package compliance.temporal;

// ═══════════════════════════════════════════════════════════════════
//  TODO 5: Create the Compliance team's worker
// ═══════════════════════════════════════════════════════════════════
//
// This is the CRAWL worker pattern with THREE registrations:
//
//   C — Connect to Temporal
//   R — Create factory and worker on "compliance-risk"
//   W — Wire:
//       1. worker.registerWorkflowImplementationTypes(ComplianceWorkflowImpl.class)
//       2. worker.registerActivitiesImplementations(new ComplianceActivityImpl(new ComplianceChecker()))
//       3. worker.registerNexusServiceImplementation(new ComplianceNexusServiceImpl())
//   L — Launch (factory.start())
//
// ── Task queue: "compliance-risk" ───────────────────────────────
// This MUST match what you set as --target-task-queue when creating
// the Nexus endpoint via the CLI. If they don't match, calls fail.

import compliance.ComplianceChecker;
import compliance.temporal.activity.ComplianceActivityImpl;
import compliance.temporal.workflow.ComplianceWorkflowImpl;
import io.temporal.client.WorkflowClient;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;

public class ComplianceWorkerApp {

    public static void main(String[] args) {
        // TODO: C — Connect to Temporal
        WorkflowServiceStubs service = WorkflowServiceStubs.newLocalServiceStubs();
        WorkflowClient client = WorkflowClient.newInstance(service);

        // TODO: R — Create factory and worker on "compliance-risk"
        WorkerFactory factory = WorkerFactory.newInstance(client);
        Worker worker = factory.newWorker("compliance-risk");

        // TODO: W — Register workflow, activity, and Nexus handler
        //   worker.registerWorkflowImplementationTypes(ComplianceWorkflowImpl.class);
        //   worker.registerActivitiesImplementations(new ComplianceActivityImpl(new ComplianceChecker()));
        //   worker.registerNexusServiceImplementation(new ComplianceNexusServiceImpl());

        // TODO: L — Start the factory and print a startup banner
        factory.start();
    }
}
