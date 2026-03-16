// ❌ BAD: Thread.sleep() in code called from a Nexus handler or activity
//
// Problems:
// - Blocks a thread with no durability guarantee
// - If the worker is killed mid-sleep, the timer is lost — nothing resumes
// - Risks exceeding the 10-second Nexus handler timeout if called synchronously
public class ComplianceChecker {
    public ComplianceResult checkCompliance(ComplianceRequest request) {
        try {
            Thread.sleep(10_000); // ❌ not durable; do not use in Temporal/Nexus context
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        // ... compliance logic
    }
}


// ✅ GOOD: Workflow.sleep() inside the workflow
//
// Benefits:
// - Timer is checkpointed in Temporal event history — survives worker restarts
// - Does not block a thread
// - Visible as a TimerFired event in the Temporal UI
// - Correctly demonstrates Nexus + Temporal durability: kill the worker mid-sleep,
//   restart it, and the workflow resumes exactly where it left off
public class ComplianceWorkflowImpl implements ComplianceWorkflow {
    public ComplianceResult run(ComplianceRequest request) {
        autoResult = complianceActivity.checkCompliance(request); // fast, non-blocking

        Workflow.sleep(Duration.ofSeconds(10)); // ✅ durable timer, no thread blocked

        // ... rest of workflow logic
    }
}
