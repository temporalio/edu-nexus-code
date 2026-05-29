# Decoupling Temporal Services with Nexus and the Python SDK

**Time:** ~30 min (core path) · **Difficulty:** Intermediate · **SDK:** Python 3.10+

> **About this tutorial.** You'll start with a working monolith — one Worker, one team — and decouple it into two independently-deployable services that still feel like one program to call. Along the way, you'll kill a Worker mid-flight and watch a real durable Nexus call survive, replay, and finish.

## What you'll learn

By the end of this tutorial, you'll be able to:

1. Register a **Nexus Endpoint** using the Temporal CLI to route operations from one Namespace to another.
2. Define a **Nexus Service contract** in Python using `@nexusrpc.service` and `nexusrpc.Operation` type hints.
3. Implement **synchronous and asynchronous Nexus Operation handlers** with `@nexus.workflow_run_operation` and `@nexusrpc.handler.sync_operation`.
4. Replace an Activity call with a durable **Nexus call** in a Workflow by swapping the stub.
5. **Diagnose Nexus operations in the Web UI Event History** — distinguish `NexusOperationScheduled`, `NexusOperationStarted`, and the pending-operation state.
6. **Decide when Nexus is the right tool** versus Child Workflows or shared Activities.

## Prerequisites

- Comfortable with Temporal **Workflows**, **Activities**, and **Workers** in Python. If not, run the [Hello World tutorial](https://learn.temporal.io/getting_started/python/hello_world_in_python/) first.
- Python **3.10+** and [`uv`](https://docs.astral.sh/uv/) installed locally (only needed for the local fallback path — Instruqt has everything pre-installed).
- [Temporal CLI](https://docs.temporal.io/cli) **v1.3.0 or higher** (Instruqt has it pre-installed).

## What you'll build

A banking payment system shared by two teams — **Payments** and **Compliance** — that today runs on a single Worker. One Compliance bug crashes payments. You'll split it into two Workers in two Namespaces, calling each other through a single Nexus Endpoint, and prove the boundary is durable by killing a Worker mid-call.

You'll process three transactions in three runs:

| Transaction | Amount | Risk      | Path                                | Final result        |
|-------------|--------|-----------|-------------------------------------|---------------------|
| TXN-A       | $250   | LOW       | Auto-approved by Compliance         | `COMPLETED`         |
| TXN-B       | $12,000| MEDIUM    | Waits for human review              | `COMPLETED` (after approval) |
| TXN-C       | $75,000| HIGH      | Auto-declined by Compliance         | `DECLINED_COMPLIANCE` |

<!-- DIAGRAM: side-by-side. LEFT: monolith — single "payments-processing" Worker with both Payments and Compliance code, single blast radius shaded red. RIGHT: decoupled — "payments-namespace" with Payments Worker, "compliance-namespace" with Compliance Worker, arrow between them labeled "Nexus Endpoint: compliance-endpoint". -->

---

## Module 0 — Get the lab running

> **~4 min · Setup**

### Option 1 — Instruqt (recommended)

Click the lab launcher in the course page. The environment includes:

- Temporal CLI + dev server pre-installed
- Python 3.11 with all dependencies in a `uv` venv
- This tutorial's code at `/root/edu-nexus-code/python`
- A terminal multiplexer with named tabs for each Worker

Skip to Module 1.

If Instruqt is unavailable, use **Appendix A — Local fallback setup**.

---

## Module 1 — The problem: a shared blast radius

> **~3 min · Read**

Today, your bank's payment system runs three steps in sequence:

1. **Validate** the payment (amount, account numbers).
2. **Check compliance** (risk score, sanctions screening).
3. **Execute** the payment through the gateway.

All three steps live in a single Worker, in a single Namespace, on a single Task Queue. The Payments team and the Compliance team share the deployment. That means **one team's bug is everyone's outage** — a NullPointerException in compliance code crashes the Worker mid-payment, and now nobody can process money.

### What fails without Nexus

You have three obvious alternatives. None of them are good:

| Approach                                    | What you give up                                                                                                  |
|---------------------------------------------|-------------------------------------------------------------------------------------------------------------------|
| Wrap Compliance in an HTTP API + Activity   | Lose durability across the network. The Activity retries, but the call into Compliance is opaque to Temporal.    |
| Share Activities across teams (current state)| Lose isolation. One team deploys a bad change → both teams' Workers crash.                                       |
| **Nexus**                                   | Nothing meaningful. Two independent Workers, durable cross-Namespace call, same one-line method invocation.        |

Nexus is the Temporal-native answer: **cross-Namespace calls with the same durability guarantees as Activities**, while letting each team own its own Worker, deployment cadence, and namespace.

<!-- DIAGRAM: the three-row "trade-off" comparison above as a visual table with checkmarks/x's on three axes: durable, isolated, type-safe contract. -->

---

## Module 2 — Nexus building blocks

> **~5 min · Read + match-the-term check**

Four concepts. Memorize the difference between them — this is the only conceptual content in the tutorial.

| Term                | What it is                                                                                                       | Lives where                            |
|---------------------|------------------------------------------------------------------------------------------------------------------|----------------------------------------|
| **Nexus Service**   | A named contract — a Python class decorated with `@nexusrpc.service` listing typed Operations.                   | Shared code, imported by both teams.   |
| **Nexus Operation** | An individual callable method declared on a Service, with input and output types.                                | Inside the Service class.              |
| **Nexus Endpoint**  | A routing rule that maps an endpoint *name* (e.g., `compliance-endpoint`) → target Namespace + Task Queue.       | Registered on the Temporal Server.     |
| **Nexus Registry**  | The Server-side directory of all Endpoints in your Temporal Service.                                             | Managed by the Server.                 |

**The mental model:** the Service is the **interface**, the Operation is a **method on that interface**, the Endpoint is the **address book entry** that points the caller at the right Worker, and the Registry is the address book itself.

<!-- DIAGRAM: a UML-style class for ComplianceNexusService showing two operations (check_compliance, submit_review). Below it an arrow labeled "compliance-endpoint" pointing into a box labeled "compliance-namespace / compliance-risk task queue". Inside that box: ComplianceNexusServiceHandler, ComplianceWorkflow, assess_risk activity. -->

### Quick check

Match each term to the example:

1. `@nexusrpc.service class ComplianceNexusService: ...`
2. `check_compliance: nexusrpc.Operation[ComplianceRequest, ComplianceResult]`
3. `temporal operator nexus endpoint create --name compliance-endpoint ...`
4. The internal table the server uses to look up `compliance-endpoint`.

<details>
<summary>Answers</summary>

1 → Nexus Service · 2 → Nexus Operation · 3 → Nexus Endpoint · 4 → Nexus Registry

</details>

---

## Module 3 — Checkpoint 0: run the monolith

> **~6 min · Do + check**

Before decoupling anything, verify the baseline works. This is the system you'll transform.

### Run it

In **T2** (you'll re-purpose this terminal for the Compliance Worker later), start the monolith Worker:

```bash
uv run python -m monolith.worker
```

You should see:

```
Monolith Worker started on task queue: payments-processing
```

In **T4**, run the three starters one at a time:

```bash
uv run python -m monolith.starter --txn TXN-A
uv run python -m monolith.starter --txn TXN-B
uv run python -m monolith.starter --txn TXN-C
```

> **Note.** TXN-B will block for a moment, then auto-complete with the placeholder review logic in the monolith. We'll replace that with real human-in-the-loop in Module 11.

### Inspect in the Web UI

Open `http://localhost:8233`. Make sure the Namespace dropdown shows **`default`** (the monolith currently runs there — you'll move it later). Open each Workflow Execution and look at its Event History.

> **Check.** You should see three Workflow Executions with these results:
>
> - `TXN-A` → `Completed` with output `approved=true, reason="auto-approved low risk"`
> - `TXN-B` → `Completed` with output `approved=true, reason="human review (placeholder)"`
> - `TXN-C` → `Completed` with output `approved=false, reason="DECLINED_COMPLIANCE"`
>
> Notice how every step — validation, compliance, execution — runs as an **Activity Execution** of the same Workflow inside one Namespace. That's the single blast radius.

Stop the monolith Worker (`Ctrl+C` in T2). You won't need it again.

---

## Module 4 — TODO 1: define the Nexus Service contract

> **~5 min · Do**

Open `shared/nexus_service.py`. You'll see scaffolding with placeholder types and a class with no decorators. Your job: turn it into a typed Nexus Service contract.

The contract must declare two Operations:

- `check_compliance(ComplianceRequest) -> ComplianceResult` — the long-running risk check (will become **async**, backed by a Workflow).
- `submit_review(ReviewRequest) -> ComplianceResult` — the human approval/denial signal (will become **sync**, backed by a Workflow Update).

### Change it

```python
# shared/nexus_service.py
from dataclasses import dataclass

import nexusrpc


@dataclass
class ComplianceRequest:
    transaction_id: str
    amount_cents: int
    account_from: str
    account_to: str


@dataclass
class ReviewRequest:
    transaction_id: str
    approved: bool
    explanation: str


@dataclass
class ComplianceResult:
    approved: bool
    reason: str


@nexusrpc.service
class ComplianceNexusService:
    check_compliance: nexusrpc.Operation[ComplianceRequest, ComplianceResult]
    submit_review: nexusrpc.Operation[ReviewRequest, ComplianceResult]
```

That's it — no method bodies. A Nexus Service is **a typed contract, nothing more**. The implementation lives in a separate handler class (Module 5), and the caller depends only on this contract (Module 8).

> **Why dataclasses?** Nexus uses the Temporal SDK's Data Converter to serialize Operation inputs and outputs across the boundary. Python dataclasses serialize to JSON cleanly and stay type-safe at both ends. If your handler is in a different language (Go, Java, .NET), use Protobuf instead — see the docs on [polyglot Nexus Services](https://docs.temporal.io/develop/python/nexus#define-nexus-service-contract).

> **Check.** Run `uv run mypy shared/nexus_service.py`. It should pass with no errors. The decorator wires up the type signatures used by the rest of the system.

---

## Module 5 — TODO 2: implement the Operation handlers

> **~10 min · Do + read**

Open `compliance/nexus_handler.py`. You'll see an empty handler class waiting for two methods. This is the **only** place the Compliance team writes Nexus glue code — and even here, the code is purely Temporal primitives. No business logic.

The two handlers use **two different decorators** for two different lifecycle shapes:

| Pattern        | Decorator                          | What the handler returns                  | When to use                                                                       |
|----------------|------------------------------------|-------------------------------------------|-----------------------------------------------------------------------------------|
| **Async**      | `@nexus.workflow_run_operation`    | A `nexus.WorkflowHandle` for a started Workflow | Operation backed by a long-running Workflow. Temporal delivers the result later. |
| **Sync**       | `@nexusrpc.handler.sync_operation` | The Operation's output directly           | Operation completes in ≤ 10 seconds (typically a Query, Signal, or Update).      |

### Change it

```python
# compliance/nexus_handler.py
import nexusrpc.handler
from temporalio import nexus

from shared.nexus_service import (
    ComplianceNexusService,
    ComplianceRequest,
    ComplianceResult,
    ReviewRequest,
)
from compliance.workflow import ComplianceWorkflow


@nexusrpc.handler.service_handler(service=ComplianceNexusService)
class ComplianceNexusServiceHandler:
    @nexus.workflow_run_operation
    async def check_compliance(
        self,
        ctx: nexus.WorkflowRunOperationContext,
        input: ComplianceRequest,
    ) -> nexus.WorkflowHandle[ComplianceResult]:
        return await ctx.start_workflow(
            ComplianceWorkflow.run,
            input,
            id=f"compliance-{input.transaction_id}",
        )

    @nexusrpc.handler.sync_operation
    async def submit_review(
        self,
        ctx: nexusrpc.handler.StartOperationContext,
        input: ReviewRequest,
    ) -> ComplianceResult:
        handle = nexus.client().get_workflow_handle_for(
            ComplianceWorkflow.run,
            f"compliance-{input.transaction_id}",
        )
        return await handle.execute_update(ComplianceWorkflow.review, input)
```

### Why this shape matters

Two ideas to internalize before you move on:

**1. The async handler returns a `WorkflowHandle`, not the result.** Temporal records the Workflow ID, the operation is marked "Started," and the caller workflow durably waits. If the Compliance Worker dies and restarts, Temporal looks up the Workflow ID, sees it's still running, and continues. **You never duplicate the Workflow** because the ID is `compliance-{transaction_id}` — deterministic per business identity.

**2. The sync handler must finish in ≤ 10 seconds.** That's why it's an Update (or Signal/Query): a short request-response cycle to an existing Workflow. The actual decision-making is inside `ComplianceWorkflow.review`, not in this handler.

<details>
<summary>Why no business logic in the handler?</summary>

Nexus handlers should only invoke Temporal primitives — `start_workflow`, `execute_update`, `query`, etc. Business logic belongs in Workflows (durable) or Activities (retryable). If your handler does an HTTP call or computes a risk score, it's outside Temporal's durability guarantees and you lose the very thing Nexus is giving you.

</details>

> **Check.** `uv run mypy compliance/nexus_handler.py` should pass. If it complains that `WorkflowHandle` is missing — make sure you're importing `from temporalio import nexus` (not `from temporalio.workflow import nexus`, which is the caller side).

---

## Module 6 — TODO 3: wire the Compliance Worker

> **~5 min · Do + check**

Open `compliance/worker.py`. You'll see most of a normal Worker setup, with one line left for you.

### Change it

```python
# compliance/worker.py
import asyncio

from temporalio.client import Client
from temporalio.worker import Worker

from compliance.activities import assess_risk
from compliance.workflow import ComplianceWorkflow
from compliance.nexus_handler import ComplianceNexusServiceHandler

NAMESPACE = "compliance-namespace"
TASK_QUEUE = "compliance-risk"


async def main() -> None:
    client = await Client.connect("localhost:7233", namespace=NAMESPACE)
    worker = Worker(
        client,
        task_queue=TASK_QUEUE,
        workflows=[ComplianceWorkflow],
        activities=[assess_risk],
        nexus_service_handlers=[ComplianceNexusServiceHandler()],
    )
    print(f"Compliance Worker started on: {TASK_QUEUE}")
    await worker.run()


if __name__ == "__main__":
    asyncio.run(main())
```

The single line that makes this a Nexus-capable Worker:

```python
nexus_service_handlers=[ComplianceNexusServiceHandler()],
```

That's the entire wiring story on the handler side. The Worker now polls `compliance-risk` for **both** Workflow tasks and Nexus tasks.

### Checkpoint 1

In **T2**, start the Compliance Worker:

```bash
uv run python -m compliance.worker
```

> **Check.** You should see:
> ```
> Compliance Worker started on: compliance-risk
> ```
>
> The Worker is now polling `compliance-namespace` on the `compliance-risk` Task Queue. Leave it running — you'll bring up the Payments Worker next.

---

## Module 7 — Checkpoint 1.5: register the Nexus Endpoint

> **~3 min · Do**

The Worker is polling, but the Server doesn't yet know that an endpoint named `compliance-endpoint` should route to it. Time to add it to the Nexus Registry.

**Think of it as a phone contact.** You're saving the *name* "compliance-endpoint" with the *number* "compliance-namespace + compliance-risk task queue."

In **T4**, run:

```bash
temporal operator nexus endpoint create \
  --name compliance-endpoint \
  --target-namespace compliance-namespace \
  --target-task-queue compliance-risk
```

Verify it stuck:

```bash
temporal operator nexus endpoint list
```

> **Check.** You should see `compliance-endpoint` in the list with target `compliance-namespace / compliance-risk`. If you need to fix a typo, use `temporal operator nexus endpoint delete --name compliance-endpoint` and recreate.

---

## Module 8 — TODO 4: swap the Activity stub for a Nexus stub

> **~5 min · Do**

Open `payments/workflow.py`. The current implementation calls Compliance through a *shared Activity*. You'll change that single line so the call goes through Nexus instead — same method signature, same return type, but durable across the Namespace boundary.

### Before

```python
# payments/workflow.py — current state
@workflow.defn
class PaymentProcessingWorkflow:
    @workflow.run
    async def run(self, request: PaymentRequest) -> PaymentResult:
        await workflow.execute_activity(
            validate_payment, request,
            start_to_close_timeout=timedelta(seconds=10),
        )

        compliance_result: ComplianceResult = await workflow.execute_activity(
            check_compliance_activity,
            ComplianceRequest(...),
            start_to_close_timeout=timedelta(minutes=10),
        )

        if not compliance_result.approved:
            return PaymentResult(status="DECLINED_COMPLIANCE", ...)

        return await workflow.execute_activity(
            execute_payment, request,
            start_to_close_timeout=timedelta(seconds=30),
        )
```

### After

```python
# payments/workflow.py — target state
from datetime import timedelta
from temporalio import workflow

with workflow.unsafe.imports_passed_through():
    from shared.nexus_service import (
        ComplianceNexusService,
        ComplianceRequest,
        ComplianceResult,
    )

COMPLIANCE_ENDPOINT = "compliance-endpoint"


@workflow.defn
class PaymentProcessingWorkflow:
    @workflow.run
    async def run(self, request: PaymentRequest) -> PaymentResult:
        await workflow.execute_activity(
            validate_payment, request,
            start_to_close_timeout=timedelta(seconds=10),
        )

        compliance_client = workflow.create_nexus_client(
            service=ComplianceNexusService,
            endpoint=COMPLIANCE_ENDPOINT,
        )
        compliance_result: ComplianceResult = await compliance_client.execute_operation(
            ComplianceNexusService.check_compliance,
            ComplianceRequest(
                transaction_id=request.transaction_id,
                amount_cents=request.amount_cents,
                account_from=request.account_from,
                account_to=request.account_to,
            ),
            schedule_to_close_timeout=timedelta(minutes=10),
        )

        if not compliance_result.approved:
            return PaymentResult(status="DECLINED_COMPLIANCE", reason=compliance_result.reason)

        return await workflow.execute_activity(
            execute_payment, request,
            start_to_close_timeout=timedelta(seconds=30),
        )
```

### What changed

- **`workflow.execute_activity(check_compliance_activity, ...)`** became **`compliance_client.execute_operation(ComplianceNexusService.check_compliance, ...)`**.
- The endpoint name is referenced *inside the Workflow* — no separate worker-level binding needed in Python. (If you've seen the Java version, the binding moves into `NexusServiceOptions` at Worker registration; Python's simpler.)
- `schedule_to_close_timeout` replaces the Activity's `start_to_close_timeout`. It's the **Nexus Operation's** total budget — covers retries by the Nexus Machinery, scheduling delay, *and* the underlying Workflow's run time.

> **Subtle but important.** The line in the Workflow looks like a method call into a class your team doesn't own. That's by design. The Compliance team can rewrite their Workflow, scale it, version it, even move it to a different cluster — and as long as the Service contract and Endpoint name stay the same, your code doesn't change.

> **Check.** `uv run mypy payments/workflow.py` should pass. The decorator on `ComplianceNexusService` gives `execute_operation` enough type information to confirm the input is `ComplianceRequest` and the result is `ComplianceResult`.

---

## Module 9 — TODO 5: start the Payments Worker — Checkpoint 2

> **~8 min · Do + check**

Open `payments/worker.py`. Unlike the Java version, there is **no special Nexus configuration at the Worker level in Python.** Just register the Workflow and any Activities it still uses (`validate_payment`, `execute_payment`).

### Change it

```python
# payments/worker.py
import asyncio

from temporalio.client import Client
from temporalio.worker import Worker

from payments.activities import validate_payment, execute_payment
from payments.workflow import PaymentProcessingWorkflow, ReviewCallerWorkflow

NAMESPACE = "payments-namespace"
TASK_QUEUE = "payments-processing"


async def main() -> None:
    client = await Client.connect("localhost:7233", namespace=NAMESPACE)
    worker = Worker(
        client,
        task_queue=TASK_QUEUE,
        workflows=[PaymentProcessingWorkflow, ReviewCallerWorkflow],
        activities=[validate_payment, execute_payment],
    )
    print(f"Payments Worker started on: {TASK_QUEUE}")
    await worker.run()


if __name__ == "__main__":
    asyncio.run(main())
```

The Activity import for `check_compliance_activity` is gone — the Activity itself is gone. That code now lives in `ComplianceWorkflow`, on the other side of the boundary.

### Checkpoint 2: decoupled, end-to-end

In **T3**, start the Payments Worker:

```bash
uv run python -m payments.worker
```

You should now have:

- **T1**: Temporal dev server
- **T2**: Compliance Worker (`compliance-namespace`)
- **T3**: Payments Worker (`payments-namespace`)
- **T4**: ready for starters

In **T4**, run all three transactions:

```bash
uv run python -m payments.starter --txn TXN-A
uv run python -m payments.starter --txn TXN-B
uv run python -m payments.starter --txn TXN-C
```

Expected behavior:

- **TXN-A** completes in ~5 seconds with `COMPLETED` (LOW risk, auto-approved).
- **TXN-B** appears to *hang* — that's correct. It's waiting for a human review (Module 11).
- **TXN-C** completes in ~5 seconds with `DECLINED_COMPLIANCE` (HIGH risk, auto-declined).

### Look at it in the Web UI

In `http://localhost:8233`, switch the Namespace dropdown between `payments-namespace` and `compliance-namespace`. You'll see:

- In **`payments-namespace`**: three `PaymentProcessingWorkflow` Executions (one per TXN).
- In **`compliance-namespace`**: three `ComplianceWorkflow` Executions, each with ID `compliance-TXN-A` / `compliance-TXN-B` / `compliance-TXN-C`.

> **Check.** Open `TXN-A`'s payment Workflow in `payments-namespace` and look at its Event History. You should see:
>
> - `NexusOperationScheduled` — when the Workflow asked Nexus to call the Service
> - `NexusOperationStarted` — when the Compliance Worker accepted the request and started the handler Workflow
> - `NexusOperationCompleted` — when the handler Workflow returned the result
>
> Each carries the `compliance-endpoint` name in its attributes. The call survives across the boundary, durably, with no extra code on your part.

<!-- DIAGRAM: Web UI screenshot mockup of payment workflow Event History showing the three NexusOperation* events highlighted, with annotations pointing to the linked compliance workflow. -->

---

## Module 10 — Checkpoint 3: durability across the boundary

> **~8 min · Fail + do + check**

This is the demo that sells Nexus. You're going to deliberately kill the Compliance Worker mid-call and watch the payment Workflow survive without lifting a finger.

### The setup

`ComplianceWorkflow` (which we pre-built for you) has a `workflow.sleep(timedelta(seconds=10))` inside it — a deliberate 10-second window where the Compliance side is "thinking." That gives you time to break it.

Make sure all four terminals are running their assigned roles (server, Compliance Worker, Payments Worker, starter).

### Break it

In **T4**, start TXN-A again with a fresh ID:

```bash
uv run python -m payments.starter --txn TXN-A-KILL
```

In **T2**, **immediately** hit `Ctrl+C` to kill the Compliance Worker. Do this within the 10-second window.

### Observe

In the Web UI, open the `TXN-A-KILL` Workflow in `payments-namespace`. Refresh.

> **Check.** You should see:
>
> - A **"Pending Nexus Operation"** indicator with a "Started" badge.
> - An attempt counter slowly ticking upward — the Nexus Machinery is retrying delivery, but the handler Worker isn't there to receive it.
> - The payment Workflow itself is **healthy** — it's durably waiting, not crashed.

### Recover

In **T2**, restart the Compliance Worker:

```bash
uv run python -m compliance.worker
```

Watch the same Workflow page in the Web UI refresh. Within a few seconds:

- The pending Nexus operation transitions to `NexusOperationCompleted`.
- The payment Workflow finishes with `COMPLETED`.

> **Check.** Open `compliance-TXN-A-KILL` in `compliance-namespace`. Look at the Event History. **There is only one execution** — no duplicate Compliance check ran, even though the call was retried while the Worker was dead. That's because the handler used `start_workflow` with a deterministic ID (`compliance-{transaction_id}`), and `start_workflow` is idempotent on that ID.

This is the property that makes Nexus durable: **at-least-once delivery + deterministic IDs = exactly-once business effect**, surviving Worker outages, network partitions, and Temporal Server hops.

<details>
<summary>What if the Compliance Worker never comes back?</summary>

The Nexus Operation retries until its `schedule_to_close_timeout` is exceeded (you set it to 10 minutes in Module 8). At that point, the Operation fails, and the calling Workflow sees a `NexusOperationError`. You'd handle it the same way you handle any Activity failure — with a `try`/`except` block and your own fallback or compensation logic.

The point is: the failure surfaces as an *exception in your Workflow code*, on your terms — not as a crash, a stuck workflow, or silent data loss.

</details>

---

## Module 11 — The human review path — final checkpoint

> **~6 min · Do**

TXN-B has been sitting in `compliance-namespace` for a while now, waiting for a reviewer. Time to approve it through Nexus.

### What's already built

Two files we pre-provided to keep the focus on Nexus:

- **`compliance/workflow.py`** — `ComplianceWorkflow` has an `@workflow.update` method called `review` that delivers the human decision and unblocks the `wait_condition`.
- **`payments/review_caller.py`** — a thin `ReviewCallerWorkflow` that calls `submit_review` through Nexus. We use a Workflow (not direct client code) so the Payments team never needs to know the Compliance team's Workflow ID.

```python
# payments/review_caller.py — for reference, no edits required
from temporalio import workflow

with workflow.unsafe.imports_passed_through():
    from shared.nexus_service import (
        ComplianceNexusService,
        ComplianceResult,
        ReviewRequest,
    )

COMPLIANCE_ENDPOINT = "compliance-endpoint"


@workflow.defn
class ReviewCallerWorkflow:
    @workflow.run
    async def run(self, review: ReviewRequest) -> ComplianceResult:
        client = workflow.create_nexus_client(
            service=ComplianceNexusService,
            endpoint=COMPLIANCE_ENDPOINT,
        )
        return await client.execute_operation(
            ComplianceNexusService.submit_review,
            review,
            schedule_to_close_timeout=timedelta(seconds=15),
        )
```

Note the **same Nexus client pattern** as in `PaymentProcessingWorkflow`. Different Operation (`submit_review`), but identical wiring — that's the contract paying off.

### Run it

Make sure all four terminals are healthy (server, both workers, T4 free). In **T4**:

```bash
uv run python -m payments.review_starter --txn TXN-B --approve true
```

> **Check.** In T4 you should see something like:
> ```
> Review submitted for TXN-B: approved=True, reason="approved by reviewer"
> ```
>
> Back in T3 (Payments Worker), the original `TXN-B` Workflow that was waiting will resume and complete. Check the Web UI — it should now be `Completed`.

### Try the denial path

Edit `payments/review_starter.py` (or just re-run with a different ID and flag):

```bash
uv run python -m payments.starter --txn TXN-D --amount 12000
# Then:
uv run python -m payments.review_starter --txn TXN-D --approve false
```

The payment Workflow should finish with `DECLINED_COMPLIANCE`.

---

## Module 12 — Knowledge check + Nexus vs. Child Workflow

> **~8 min · Check + read**

### Quiz

Five questions. Try to answer before peeking.

**1. Where is the `compliance-endpoint` configured in your Python code?**

<details><summary>Answer</summary>

Inside the caller Workflow, in `workflow.create_nexus_client(endpoint=COMPLIANCE_ENDPOINT)`. There is no Worker-level Nexus configuration in the Python SDK — unlike Java, the binding lives in the Workflow.

</details>

**2. What happens to a payment Workflow if the Compliance Worker is down when the Nexus call is made?**

<details><summary>Answer</summary>

The Nexus Machinery retries delivery until `schedule_to_close_timeout` is reached. The payment Workflow is durably waiting; it doesn't crash or lose progress. When the Compliance Worker comes back, the operation completes and the payment Workflow resumes. (You saw this in Module 10.)

</details>

**3. What's the difference between `@nexusrpc.service` and `@nexusrpc.handler.service_handler`?**

<details><summary>Answer</summary>

`@nexusrpc.service` decorates the **contract** (interface) — input/output types and operation names. Both caller and handler import this.
`@nexusrpc.handler.service_handler(service=...)` decorates the **implementation** — the class that contains the actual Operation handler methods. Only the handler side imports this.

</details>

**4. Why use `@nexus.workflow_run_operation` for `check_compliance` instead of `@nexusrpc.handler.sync_operation`?**

<details><summary>Answer</summary>

A sync handler must respond in ≤ 10 seconds. The compliance check involves a long-running Workflow with a durable sleep — easily over 10s. The workflow-run operation starts the Workflow and lets the Temporal Server deliver the result asynchronously when it eventually completes, no matter how long it takes.

Using sync here would also cause **duplicate Workflow starts** on retries, because each retry would attempt a new `start_workflow` call without the same idempotency guarantees you get from the async path returning a `WorkflowHandle` with a stable ID.

</details>

**5. Why does the `submit_review` handler call `execute_update` on a Workflow instead of containing the approval logic directly?**

<details><summary>Answer</summary>

Handlers should contain only Temporal primitives, not business logic. The actual review decision logic — what counts as approved, what reason text to record — lives in `ComplianceWorkflow.review`. The handler's job is to deliver the decision durably to the existing Workflow Execution, nothing more.

</details>

### Nexus vs. Child Workflow vs. Activity — decision guide

This is the most-asked question after attendees see Nexus for the first time.

| You want to…                                                                          | Use…                          | Why                                                                                                  |
|---------------------------------------------------------------------------------------|-------------------------------|------------------------------------------------------------------------------------------------------|
| Run a side-effect or external call (DB write, HTTP, send email)                        | **Activity**                  | Activities are the right tool for any code that needs retries and timeouts but doesn't span teams.    |
| Compose a sub-routine *owned by the same team, in the same Namespace*                  | **Child Workflow**            | Lower overhead than Nexus, shares the parent's deployment lifecycle, can be cancelled with the parent. |
| Call into another team's Namespace, or across deployment boundaries                    | **Nexus**                     | Only Nexus gives you durable cross-Namespace calls with an independent deployment lifecycle.          |
| Same as Child Workflow, but the team owns a different code repo on the same Namespace  | **Either** — prefer Nexus     | The contract gives you future-proofing if the team eventually splits Namespaces.                      |
| Run a polyglot system (Python caller, Go handler)                                      | **Nexus**                     | Child Workflows must be in the same SDK; Nexus is the cross-language story.                          |

**Quick heuristic:** "Same team / same Namespace / same SDK" → Child Workflow. "Different team / different Namespace / different deployment cadence" → Nexus.

<!-- DIAGRAM: a small decision tree starting "Need to call something from your Workflow?" → branches on the questions in the table above. -->

### Scenario quiz

**Scenario A.** Your team owns a `recommendations-namespace`. The `homepage-namespace` team wants to display a recommendation when a user signs in. Sign-in must not be blocked if recommendations is down. **What do you use, and why?**

<details><summary>Answer</summary>

Nexus, with a short `schedule_to_close_timeout` and a `try`/`except` in the sign-in Workflow to fall back to a default recommendation. Cross-Namespace, cross-team — Nexus is the only option that gives durability and isolation. The short timeout + fallback gives you graceful degradation.

</details>

**Scenario B.** Inside your `payments-namespace`, your `PaymentProcessingWorkflow` needs to invoke a fraud-scoring routine that lives in the same repo, owned by the same team. **Nexus or Child Workflow?**

<details><summary>Answer</summary>

Child Workflow. Same team, same Namespace, same deployment — Nexus adds boundary overhead (an Endpoint, a separate Worker poller) for no isolation benefit. If the fraud-scoring team later splits off and moves to their own Namespace, *that's* when you migrate to Nexus.

</details>

---

## Module 13 — What you built

> **~2 min**

You started with a single Worker, single Namespace, single blast radius. You finished with:

- **Two independent Workers** in **two Namespaces**, deployable separately, owned by different teams.
- A **typed Nexus Service contract** both teams import as the only shared dependency.
- **One Nexus Endpoint** in the Registry connecting them.
- A **durable cross-Namespace call** that survives Worker outages — proved with the kill-the-worker test in Module 10.
- A **human-in-the-loop review** path using a sync Nexus Operation that delivers a Workflow Update.

The transformation you made:

```diff
- compliance_result = await workflow.execute_activity(check_compliance_activity, ...)
+ compliance_result = await compliance_client.execute_operation(
+     ComplianceNexusService.check_compliance, ...,
+ )
```

**One line in your Workflow. Two teams, two Namespaces, two deployment cadences.** Same durability guarantees. That's the shape of Nexus.

### Concepts you used

- `@nexusrpc.service` + `nexusrpc.Operation[...]` — the contract
- `@nexusrpc.handler.service_handler` — the handler shell
- `@nexus.workflow_run_operation` — async, Workflow-backed
- `@nexusrpc.handler.sync_operation` — sync, ≤ 10s
- `workflow.create_nexus_client` — the Workflow-side stub
- `temporal operator nexus endpoint create` — Registry wiring
- `nexus_service_handlers=[...]` — Worker-side registration
- Event History events: `NexusOperationScheduled`, `NexusOperationStarted`, `NexusOperationCompleted`

### What's next

- **Multi-step async pipelines** — chain multiple Nexus Operations across teams. See [docs](https://docs.temporal.io/develop/python/nexus).
- **Cancellation propagation** — cancel the caller Workflow, and Nexus propagates the cancel signal across the boundary. See [Cancel a Nexus Operation](https://docs.temporal.io/develop/python/nexus#canceling-a-nexus-operation).
- **Nexus on Temporal Cloud** — set up cross-Namespace calls with mTLS and Namespace allowlists. See [Nexus on Temporal Cloud](https://docs.temporal.io/develop/python/nexus#nexus-calls-across-namespaces-temporal-cloud).
- **Versioning Nexus Services** — evolve your contract without breaking existing callers.

---

## Appendix A — Local fallback setup (optional)

Use this only if Instruqt is unavailable.

```bash
git clone https://github.com/temporalio/edu-nexus-code.git
cd edu-nexus-code/python
uv sync
```

Open **four terminals** in this directory. You'll use them throughout:

| Terminal | Role                  |
|----------|-----------------------|
| **T1**   | Temporal dev server   |
| **T2**   | Compliance Worker     |
| **T3**   | Payments Worker       |
| **T4**   | Starters (CLI commands) |

In **T1**, start the dev server with both Namespaces pre-created:

```bash
temporal server start-dev \
  --namespace payments-namespace \
  --namespace compliance-namespace \
  --ui-port 8233
```

> **Check.** Open `http://localhost:8233`. You should see the Web UI with a Namespace dropdown showing `default`, `payments-namespace`, and `compliance-namespace`. If not, check the T1 output for errors before continuing.

---

*Tutorial code: <https://github.com/temporalio/edu-nexus-code/tree/main/python>*
*Questions? Join us in [Temporal Community Slack](https://t.mp/slack).*
