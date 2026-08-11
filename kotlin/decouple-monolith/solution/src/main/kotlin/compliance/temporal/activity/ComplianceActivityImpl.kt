package compliance.temporal.activity

import compliance.ComplianceChecker
import compliance.domain.ComplianceRequest
import compliance.domain.ComplianceResult

/** [GIVEN] Compliance Activity implementation. Thin wrapper around ComplianceChecker. */
class ComplianceActivityImpl(private val complianceChecker: ComplianceChecker) : ComplianceActivity {

    override fun checkCompliance(request: ComplianceRequest): ComplianceResult =
        complianceChecker.checkCompliance(request)
}
