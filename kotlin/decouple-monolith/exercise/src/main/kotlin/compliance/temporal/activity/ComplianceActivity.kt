package compliance.temporal.activity

import compliance.domain.ComplianceRequest
import compliance.domain.ComplianceResult
import io.temporal.activity.ActivityInterface
import io.temporal.activity.ActivityMethod

/**
 * [GIVEN] Compliance Activity interface.
 *
 * In the monolith, PaymentProcessingWorkflow calls this Activity directly.
 * After decoupling, the Activity stays here but is called from the Compliance
 * Worker, inside the Workflow that the Nexus handler starts.
 */
@ActivityInterface
interface ComplianceActivity {
    @ActivityMethod
    fun checkCompliance(request: ComplianceRequest): ComplianceResult
}
