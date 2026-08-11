package shared.nexus

import compliance.domain.ComplianceRequest
import compliance.domain.ComplianceResult
import io.nexusrpc.Operation
import io.nexusrpc.Service
import shared.domain.ReviewRequest

/**
 * Nexus Service interface. The shared contract between the Payments and Compliance teams.
 *
 * Both teams depend on this interface:
 *   - Payments creates a stub from it, inside the caller Workflow
 *   - Compliance implements a handler for it, on its own Worker
 *
 * The Nexus runtime validates every method at Worker startup, so all of them need
 * @Operation, even ones you are not calling yet.
 */
@Service
interface ComplianceNexusService {

    @Operation
    fun checkCompliance(request: ComplianceRequest): ComplianceResult

    @Operation
    fun submitReview(request: ReviewRequest): ComplianceResult
}
