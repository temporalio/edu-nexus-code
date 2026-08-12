package compliance

import compliance.domain.ComplianceRequest
import compliance.domain.ComplianceResult

/**
 * [GIVEN] Rule-based compliance checker. Deterministic, no API keys needed.
 *
 * Same logic whether it runs as an Activity or behind a Nexus handler.
 *
 * Rules:
 *   - OFAC sanctioned country on either side -> HIGH risk, blocked
 *   - Amount over $50,000 -> HIGH risk, blocked (TXN-C hits this)
 *   - Amount over $10,000 or international to an unusual jurisdiction -> MEDIUM risk (TXN-B hits this)
 *   - Everything else -> LOW risk, approved
 */
class ComplianceChecker {

    private val sanctionedCountries = setOf("North Korea", "Iran", "Cuba", "Syria", "Venezuela")
    private val commonCountries = setOf("US", "UK", "Canada", "Germany", "France", "Japan", "Australia")

    fun checkCompliance(request: ComplianceRequest): ComplianceResult {
        println(
            "[ComplianceChecker] Evaluating ${request.transactionId}" +
                " | $${"%.2f".format(request.amount)}" +
                " | ${request.senderCountry} -> ${request.receiverCountry}"
        )

        // Rule 1: Sanctioned country -> HIGH risk, blocked
        if (request.receiverCountry in sanctionedCountries || request.senderCountry in sanctionedCountries) {
            return ComplianceResult(
                request.transactionId,
                false,
                "HIGH",
                "Destination/source country is OFAC-sanctioned. Transaction blocked per regulatory requirements.",
            )
        }

        // Rule 2: Very high amount -> HIGH risk, blocked
        if (request.amount > 50_000) {
            return ComplianceResult(
                request.transactionId,
                false,
                "HIGH",
                "Transaction amount exceeds \$50,000 threshold. Requires enhanced due diligence review.",
            )
        }

        // Rule 3: International transfer over $10K or unusual jurisdiction -> MEDIUM risk
        val isInternational = request.senderCountry != request.receiverCountry
        val isUnusualJurisdiction = isInternational && request.receiverCountry !in commonCountries

        if (request.amount > 10_000 || isUnusualJurisdiction) {
            return ComplianceResult(
                request.transactionId,
                true,
                "MEDIUM",
                "International transfer above \$10K threshold. Approved with AML monitoring note.",
            )
        }

        // Rule 4: Low risk, routine transaction
        return ComplianceResult(
            request.transactionId,
            true,
            "LOW",
            "Routine domestic/standard international transfer. No regulatory concerns.",
        )
    }
}
