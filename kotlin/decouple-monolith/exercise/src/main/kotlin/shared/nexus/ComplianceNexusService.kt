package shared.nexus

import compliance.domain.ComplianceRequest
import compliance.domain.ComplianceResult
import shared.domain.ReviewRequest

/**
 * The shared contract between the Payments and Compliance teams. Like an OpenAPI spec,
 * but durable. Payments builds a stub from it, Compliance implements a handler for it.
 *
 * Looking things up during this lab:
 *   Kotlin uses the Temporal JAVA SDK, so search the Java docs, not Kotlin. Searching
 *   "Kotlin Nexus" returns nothing useful. Start at
 *   https://docs.temporal.io/develop/java/nexus/feature-guide
 *
 *   Faster still: the "Ask AI" button in the top right of https://docs.temporal.io
 *   answers questions in plain language. Try "how do I define a Nexus Service contract
 *   in Java?" Get used to it now, the later TODOs are harder.
 */
// ── TODO 1 ──────────────────────────────────────────────────────────────────────
// Annotate this interface so Temporal treats it as a cross-team Nexus contract rather
// than an ordinary Kotlin interface.
interface ComplianceNexusService {

    // ── TODO 2 ──────────────────────────────────────────────────────────────────
    // Mark this method as a callable Nexus Operation.
    fun checkCompliance(request: ComplianceRequest): ComplianceResult

    // ── TODO 3 ──────────────────────────────────────────────────────────────────
    // Mark this one too. The runtime validates EVERY method in the interface at Worker
    // startup, so a method you are not calling yet still needs the annotation, or the
    // Worker fails with "Missing @Operation annotation".
    fun submitReview(request: ReviewRequest): ComplianceResult
}
