package compliance.temporal;

import compliance.domain.ComplianceRequest;
import compliance.domain.ComplianceResult;
import compliance.temporal.workflow.ComplianceWorkflow;
import io.nexusrpc.handler.OperationHandler;
import io.nexusrpc.handler.OperationImpl;
import io.nexusrpc.handler.ServiceImpl;
import io.temporal.client.WorkflowOptions;
import io.temporal.nexus.WorkflowClientOperationHandlers;
import io.temporal.nexus.WorkflowHandle;
import shared.nexus.ComplianceNexusService;

/**
 * Nexus service handler — receives cross-team calls from Payments.
 *
 * Uses fromWorkflowHandle to properly link the Nexus operation to the
 * backing ComplianceWorkflow. This ensures retries reuse the same
 * workflow instead of creating duplicates.
 */
@ServiceImpl(service = ComplianceNexusService.class)
public class ComplianceNexusServiceImpl {

    @OperationImpl
    public OperationHandler<ComplianceRequest, ComplianceResult> checkCompliance() {
        return WorkflowClientOperationHandlers.fromWorkflowHandle((ctx, details, client, input) -> {
            ComplianceWorkflow wf = client.newWorkflowStub(
                    ComplianceWorkflow.class,
                    WorkflowOptions.newBuilder()
                            .setTaskQueue("compliance-risk")
                            .setWorkflowId("compliance-" + input.getTransactionId())
                            .build());

            return WorkflowHandle.fromWorkflowMethod(wf::run, input);
        });
    }
}
