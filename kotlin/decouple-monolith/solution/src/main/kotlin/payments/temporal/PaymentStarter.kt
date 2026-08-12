package payments.temporal

import io.temporal.client.WorkflowClient
import io.temporal.client.WorkflowClientOptions
import io.temporal.client.WorkflowOptions
import io.temporal.client.WorkflowStub
import io.temporal.serviceclient.WorkflowServiceStubs
import payments.Shared
import payments.domain.PaymentRequest
import payments.domain.PaymentResult
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import kotlin.system.exitProcess

/**
 * [GIVEN] Starts three payment Workflows.
 *
 * Unlike the Java starter, this one starts all three and then collects results, instead
 * of running them one at a time. That matters once Nexus is in play: TXN-B is MEDIUM
 * risk, so its handler Workflow pauses for human review and would otherwise block
 * TXN-C behind it. Starting concurrently lets TXN-A and TXN-C finish while TXN-B sits
 * visibly pending, which is the point of the human-in-the-loop path.
 *
 * The starter does not know or care whether compliance runs locally or via Nexus.
 */

// How long to wait before calling a Workflow "pending review". TXN-A and TXN-C finish in
// roughly 12 seconds (the handler Workflow sleeps for 10 of those).
private const val COLLECT_DEADLINE_SECONDS = 25L

fun main() {
    println("==========================================================")
    println("  PAYMENT STARTER - Decouple Monolith")
    println("  Running 3 transactions through Temporal")
    println("==========================================================")
    println()

    val service = WorkflowServiceStubs.newLocalServiceStubs()
    val client = WorkflowClient.newInstance(
        service,
        WorkflowClientOptions.newBuilder()
            .setNamespace("payments-namespace")
            .build(),
    )

    val transactions = listOf(
        PaymentRequest(
            "TXN-A", 250.00, "USD", "US", "US",
            "Routine supplier payment", "ACC-001", "ACC-002",
        ),
        PaymentRequest(
            "TXN-B", 12_000.00, "USD", "US", "UK",
            "International consulting fee", "ACC-003", "ACC-004",
        ),
        PaymentRequest(
            "TXN-C", 75_000.00, "USD", "US", "US",
            "Large capital transfer", "ACC-005", "ACC-006",
        ),
    )

    // Start all three without blocking, so a pause in one cannot hold up the others.
    val pending = transactions.map { txn ->
        val workflowId = "payment-${txn.transactionId}"
        val workflow = client.newWorkflowStub(
            PaymentProcessingWorkflow::class.java,
            WorkflowOptions.newBuilder()
                .setTaskQueue(Shared.TASK_QUEUE)
                .setWorkflowId(workflowId)
                .build(),
        )
        WorkflowClient.start(workflow::processPayment, txn)

        println("  Started: $workflowId")
        println(
            "    Amount: $${"%.2f".format(txn.amount)}" +
                " | Route: ${txn.senderCountry} -> ${txn.receiverCountry}"
        )
        txn to WorkflowStub.fromTyped(workflow)
    }

    println()
    println("  All 3 started. Collecting results...")
    println()

    // Wait on all three together against a single deadline. Waiting on them one at a
    // time would let TXN-B's review pause burn the whole budget, so TXN-C would be
    // reported as pending even though it finished seconds earlier.
    val futures = pending.map { (txn, stub) -> txn to stub.getResultAsync(PaymentResult::class.java) }
    try {
        CompletableFuture
            .allOf(*futures.map { it.second }.toTypedArray())
            .get(COLLECT_DEADLINE_SECONDS, TimeUnit.SECONDS)
    } catch (e: TimeoutException) {
        // Expected: TXN-B parks for human review and never completes on its own.
    } catch (e: ExecutionException) {
        // A Workflow failed. Reported per transaction below.
    }

    var pendingReview = 0

    futures.forEach { (txn, future) ->
        println("----------------------------------------------------")
        println("  ${txn.transactionId}")

        if (!future.isDone) {
            pendingReview++
            println("  Result: STILL RUNNING")
            println("  Reason: either it is parked for human review (MEDIUM risk), or the")
            println("          Compliance handler is not answering right now.")
            println("  Action: check the Temporal UI. If it is parked for review, run")
            println("          './gradlew reviewStarter'.")
        } else {
            try {
                val result = future.get()
                println("  Result: ${result.status}")
                println("  Risk:   ${result.riskLevel ?: "N/A"}")
                println("  Reason: ${result.explanation ?: "N/A"}")
                result.confirmationNumber?.let { println("  Conf#:  $it") }
                result.error?.let { println("  Error:  $it") }
            } catch (e: ExecutionException) {
                println("  Result: FAILED")
                println("  Error:  ${e.cause?.message ?: e.message}")
            }
        }
        println()
    }

    println("==========================================================")
    if (pendingReview > 0) {
        println("  $pendingReview transaction(s) have not resolved yet.")
        println("  Parked for review? Run './gradlew reviewStarter'.")
        println("  Compliance Worker down? Start it and they resume on their own.")
    } else {
        println("  All 3 transactions resolved!")
    }
    println("  Check Temporal UI: http://localhost:8233")
    println("==========================================================")

    exitProcess(0)
}
