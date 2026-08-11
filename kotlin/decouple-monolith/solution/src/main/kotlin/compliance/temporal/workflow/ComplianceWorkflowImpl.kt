package compliance.temporal.workflow

import compliance.domain.ComplianceRequest
import compliance.domain.ComplianceResult
import compliance.temporal.activity.ComplianceActivity
import io.temporal.activity.ActivityOptions
import io.temporal.workflow.Workflow
import java.time.Duration

/**
 * [GIVEN] Compliance Workflow implementation. Pre-built for this exercise.
 *
 * The 10-second Workflow.sleep is intentional. It gives you a window to stop the
 * Compliance Worker with Ctrl+C and restart it, so you can watch the Nexus Operation
 * survive the outage. The timer runs on the server, so nothing is lost when the
 * Worker goes away.
 */
class ComplianceWorkflowImpl : ComplianceWorkflow {

    private var request: ComplianceRequest? = null
    private var autoResult: ComplianceResult? = null
    private var reviewResult: ComplianceResult? = null

    private val complianceActivity: ComplianceActivity = Workflow.newActivityStub(
        ComplianceActivity::class.java,
        ActivityOptions.newBuilder()
            .setStartToCloseTimeout(Duration.ofSeconds(30))
            .build(),
    )

    override fun run(request: ComplianceRequest): ComplianceResult {
        this.request = request

        // Step 1: Run the automated compliance check
        val auto = complianceActivity.checkCompliance(request)
        autoResult = auto

        // Durable delay. Stop the Compliance Worker mid-sleep, restart it, and the
        // Workflow resumes on its own.
        Workflow.sleep(Duration.ofSeconds(10))

        // Step 2: LOW or HIGH risk returns immediately
        if (auto.riskLevel != "MEDIUM") {
            return auto
        }

        // Step 3: MEDIUM risk waits for human review via Update.
        // Bounded on purpose. An unbounded await would leave this Workflow Running
        // forever when nobody reviews. Eight minutes is deliberately shorter than the
        // caller's 10-minute scheduleToCloseTimeout, so the handler resolves and reports
        // a decision rather than letting the caller time out first.
        val reviewed = Workflow.await(Duration.ofMinutes(8)) { reviewResult != null }
        if (!reviewed) {
            return ComplianceResult(
                transactionId = request.transactionId,
                approved = false,
                riskLevel = "MEDIUM",
                explanation = "No reviewer responded within 8 minutes. Not approved.",
            )
        }
        return reviewResult!!
    }

    override fun review(approved: Boolean, explanation: String): ComplianceResult {
        val result = ComplianceResult(
            transactionId = request!!.transactionId,
            approved = approved,
            riskLevel = "MEDIUM",
            explanation = explanation,
        )
        reviewResult = result
        return result
    }

    override fun validateReview(approved: Boolean, explanation: String) {
        check(autoResult?.riskLevel == "MEDIUM") { "Workflow is not awaiting review" }
        check(reviewResult == null) { "Review already submitted" }
    }
}
