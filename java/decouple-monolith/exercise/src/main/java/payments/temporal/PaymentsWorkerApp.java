package payments.temporal;

import compliance.ComplianceChecker;
import compliance.temporal.activity.ComplianceActivityImpl;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import io.temporal.worker.WorkflowImplementationOptions;
import io.temporal.workflow.NexusServiceOptions;
import payments.PaymentGateway;
import payments.Shared;
import payments.temporal.activity.PaymentActivityImpl;

import java.util.Collections;

/**
 * ═══════════════════════════════════════════════════════════════════
 *  MONOLITH VERSION — This works! Run it at Checkpoint 0.
 * ═══════════════════════════════════════════════════════════════════
 *
 * Currently this single worker handles EVERYTHING:
 *   - PaymentProcessingWorkflow
 *   - PaymentActivity (validate + execute)
 *   - ComplianceActivity (compliance check)  <-- will move to its own worker
 *
 * All on one task queue: "payments-processing"
 *
 * ── TODO 5: Add Nexus endpoint mapping + remove ComplianceActivity ──
 *
 * After completing TODOs 1-4, come back here and make THREE changes:
 *
 * CHANGE 1: Register both workflows with NexusServiceOptions (maps service to endpoint)
 *   Currently:   worker.registerWorkflowImplementationTypes(PaymentProcessingWorkflowImpl.class)
 *   Change to:   worker.registerWorkflowImplementationTypes(
 *                    WorkflowImplementationOptions.newBuilder()
 *                        .setNexusServiceOptions(Collections.singletonMap(
 *                            "ComplianceNexusService",
 *                            NexusServiceOptions.newBuilder()
 *                                .setEndpoint("compliance-endpoint")
 *                                .build()))
 *                        .build(),
 *                    PaymentProcessingWorkflowImpl.class,
 *                    ReviewCallerWorkflowImpl.class);
 *
 * CHANGE 2: Remove ComplianceActivityImpl registration
 *   The compliance check now runs on the Compliance worker via Nexus.
 *   Delete the lines that register ComplianceActivityImpl.
 *   Also remove the ComplianceChecker import and instantiation.
 *
 * CHANGE 3: (Already done) ReviewCallerWorkflowImpl is registered above.
 *   Both workflows use the same ComplianceNexusService endpoint mapping.
 */
public class PaymentsWorkerApp {

    public static void main(String[] args) {
        // C — Connect to Temporal (payments-namespace)
        WorkflowServiceStubs service = WorkflowServiceStubs.newLocalServiceStubs();
        WorkflowClientOptions clientOptions = WorkflowClientOptions.newBuilder()
                .setNamespace("payments-namespace")
                .build();
        WorkflowClient client = WorkflowClient.newInstance(service, clientOptions);

        // R — Register
        WorkerFactory factory = WorkerFactory.newInstance(client);
        Worker worker = factory.newWorker(Shared.TASK_QUEUE);

        // ┌─────────────────────────────────────────────────────────────┐
        // │ TODO 5 (CHANGE 1): Replace this simple registration with   │
        // │ WorkflowImplementationOptions that map                     │
        // │ "ComplianceNexusService" to "compliance-endpoint"          │
        // │ Include BOTH PaymentProcessingWorkflowImpl.class AND       │
        // │ ReviewCallerWorkflowImpl.class in the same call.           │
        // └─────────────────────────────────────────────────────────────┘
        worker.registerWorkflowImplementationTypes(PaymentProcessingWorkflowImpl.class);

        // A — Activities
        PaymentGateway gateway = new PaymentGateway();
        worker.registerActivitiesImplementations(new PaymentActivityImpl(gateway));

        // ┌─────────────────────────────────────────────────────────────┐
        // │ TODO 5 (CHANGE 2): Delete these two lines after adding     │
        // │ Nexus. Compliance now runs on its own worker.              │
        // └─────────────────────────────────────────────────────────────┘
        ComplianceChecker checker = new ComplianceChecker();
        worker.registerActivitiesImplementations(new ComplianceActivityImpl(checker));

        // L — Launch
        factory.start();

        System.out.println("=========================================================");
        System.out.println("  Payments Worker started on: " + Shared.TASK_QUEUE);
        System.out.println("  Namespace: payments-namespace");
        System.out.println("  Registered: PaymentProcessingWorkflow, PaymentActivity");
        System.out.println("              ComplianceActivity (monolith — will decouple)");
        System.out.println("=========================================================");
    }
}
