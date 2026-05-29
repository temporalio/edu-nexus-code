# Decoupling Temporal Services with Nexus and the Python SDK

**Time:** ~30 min (Instruqt path) · **Difficulty:** Intermediate · **SDK:** Python 3.10+

> **About this tutorial.** You'll start with a working monolith — one Worker, one team — and decouple it into two independently-deployable services that still feel like one program to call. Along the way, you'll kill a Worker mid-flight and watch a real durable Nexus call survive, replay, and finish.

## What you'll learn

By the end of this tutorial, you'll be able to:

1. Register a **Nexus Endpoint** using the Temporal CLI to route operations from one Namespace to another.
2. Define a **Nexus Service contract** in Python with `@nexusrpc.service` and `nexusrpc.Operation` type hints.
3. Implement an **asynchronous Nexus Operation handler** with `@nexus.workflow_run_operation`.
4. Replace an Activity call with a durable **Nexus call** in a Workflow.
5. **Diagnose Nexus operations** in the Web UI Event History (`NexusOperationScheduled`, `NexusOperationStarted`, `NexusOperationCompleted`).
6. **Decide when Nexus is the right tool** versus Child Workflows or shared Activities.

## Prerequisites

- Comfortable with Temporal **Workflows**, **Activities**, and **Workers** in Python. If not, run the [Hello World tutorial](https://learn.temporal.io/getting_started/python/hello_world_in_python/) first.

## What you'll build

A banking payment system shared by two teams — **Payments** and **Compliance** — that today runs on a single Worker. One Compliance bug crashes payments. You'll split it into two Workers in two Namespaces, calling each other through a single Nexus Endpoint, and prove the boundary is durable by killing a Worker mid-call.

You'll process three transactions:

| Transaction | Amount  | Risk   | Final result          |
|-------------|---------|--------|-----------------------|
| TXN-A       | $250    | LOW    | `COMPLETED` (auto-approved) |
| TXN-B       | $12,000 | MEDIUM | `COMPLETED` (auto-approved in this short tutorial — see *Going further*) |
| TXN-C       | $75,000 | HIGH   | `DECLINED_COMPLIANCE` (auto-declined) |

<!-- DIAGRAM: side-by-side. LEFT: monolith — single "payments-processing" Worker with both Payments and Compliance code, single blast radius shaded red. RIGHT: decoupled — "payments-namespace" with Payments Worker, "compliance-namespace" with Compliance Worker, arrow between them labeled "Nexus Endpoint: compliance-endpoint". -->

---

## Module 0 — Get the lab running

> **~2 min · Setup**

Click the **Instruqt lab launcher** in the course page. Your environment includes:

- Temporal CLI + dev server pre-installed and running
- Python 3.11 with all dependencies in a `uv` venv
- This tutorial's code at `/root/edu-nexus-code/python`
- A terminal multiplexer with named tabs **T1** (server), **T2** (Compliance Worker), **T3** (Payments Worker), **T4** (starters)

> **Check.** In any terminal, run `temporal --version` and open the Web UI at `http://localhost:8233`. You should see a Namespace dropdown with `default`, `payments-namespace`, and `compliance-namespace`.

> **Prefer to run locally?** See the *Local setup* appendix at the end. The Instruqt path is faster and avoids environment issues — recommended.

---

## Module 1 — The problem: a shared blast radius

> **~2 min · Read**

Your bank's payment system runs three steps in sequence: **validate**, **check compliance**, **execute**. All three live in a single Worker on a single Task Queue. The Payments and Compliance teams share the deployment, which means **one team's bug is everyone's outage** — a runtime crash in compliance code kills the Worker mid-payment, and now nobody can process money.

You have three alternatives. Two are bad:

| Approach                                | What you give up                                                                |
|-----------------------------------------|---------------------------------------------------------------------------------|
| Wrap Compliance in an HTTP API + Activity | Lose durability across the network. Temporal can't see inside the call.        |
| Share Activities across teams (current) | Lose isolation. One bad deploy crashes both teams' Workers.                     |
| **Nexus**                               | Nothing meaningful. Two independent Workers, durable cross-Namespace call.      |

Nexus is the Temporal-native answer: **cross-Namespace calls with the same durability guarantees as Activities**, while letting each team own its own Worker, deployment cadence, and Namespace.

---

## Module 2 — Nexus building blocks

> **~3 min · Read**

Four concepts:

| Term                | What it is                                                                              | Lives where                          |
|---------------------|-----------------------------------------------------------------------------------------|--------------------------------------|
| **Nexus Service**   | A typed contract — a Python class decorated with `@nexusrpc.service`.                   | Shared code, imported by both teams. |
| **Nexus Operation** | A callable method on a Service, with input and output types.                            | Inside the Service class.            |
| **Nexus Endpoint**  | A routing rule mapping endpoint *name* → target Namespace + Task Queue.                  | Registered on the Temporal Server.   |
| **Nexus Registry**  | The Server-side directory of all Endpoints.                                              | Managed by the Server.               |

**Mental model:** the **Service** is the interface, the **Operation** is a method on it, the **Endpoint** is the address-book entry pointing the caller at the right Worker, and the **Registry** is the address book.

<!-- DIAGRAM: a UML-style class for ComplianceNexusService with one operation (check_compliance). Below: arrow labeled "compliance-endpoint" pointing into a box labeled "compliance-namespace / compliance-risk task queue" containing ComplianceNexusServiceHandler, ComplianceWorkflow, assess_risk activity. -->

---

## Module 3 — Run the monolith

> **~3 min · Do + check**

Before decoupling anything, see the baseline.

In **T2**, start the monolith Worker:

```bash
uv run python -m monolith.worker
```

In **T4**, run all three transactions:

```bash
uv run python -m monolith.starter --txn TXN-A
uv run python -m monolith.starter --txn TXN-B
uv run python -m monolith.starter --txn TXN-C
```

> **Check.** Open `http://localhost:8233`, switch to the `default` Namespace, and confirm three Workflow Executions:
>
> - `TXN-A` → `Completed` with `approved=true`
> - `TXN-B` → `Completed` with `approved=true`
> - `TXN-C` → `Completed` with `approved=false, reason="DECLINED_COMPLIANCE"`

Notice that all three Workflow Executions live in the same Namespace, on the same Task Queue. That's the single blast radius.

Stop the monolith Worker (`Ctrl+C` in T2). You won't need it again.

---

## Module 4 — TODO 1: define the Nexus Service contract

> **~3 min · Do**

Open `shared/nexus_service.py`. Add the decorator and operation type hint:

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
class ComplianceResult:
    approved: bool
    reason: str


@nexusrpc.service
class ComplianceNexusService:
    check_compliance: nexusrpc.Operation[ComplianceRequest, ComplianceResult]
```

No method bodies — **a Nexus Service is a typed contract, nothing more.** The implementation lives in a separate handler class (next module), and the caller depends only on this contract.

> **Check.** Run `uv run mypy shared/nexus_service.py` — should pass.

---

## Module 5 — TODO 2: implement the Operation handler

> **~5 min · Do**

Open `compliance/nexus_handler.py`. The compliance check is a long-running Workflow (it includes a risk assessment Activity and a deliberate `workflow.sleep` for the durability demo coming up in Module 9), so the handler is **asynchronous** — it starts a Workflow and returns a handle, and Temporal delivers the result later.

```python
# compliance/nexus_handler.py
import nexusrpc.handler
from temporalio import nexus

from shared.nexus_service import (
    ComplianceNexusService,
    ComplianceRequest,
    ComplianceResult,
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
```

Two important properties of this shape:

1. The handler returns a `WorkflowHandle`, not the result. Temporal records the Workflow ID, marks the operation "Started," and the caller workflow durably waits. **If the Compliance Worker dies and restarts, no duplicate Workflow runs** — the ID `compliance-{transaction_id}` is deterministic per business identity, so `start_workflow` is idempotent.
2. The handler contains only Temporal primitives. **No business logic.** The risk-assessment code lives inside `ComplianceWorkflow`, on the durable side of the boundary.

> **Check.** `uv run mypy compliance/nexus_handler.py` should pass.

---

## Module 6 — TODO 3: wire the Compliance Worker + create the Endpoint

> **~4 min · Do + check**

### Wire the Worker

Open `compliance/worker.py` and add one line — `nexus_service_handlers`:

```python
# compliance/worker.py
worker = Worker(
    client,
    task_queue="compliance-risk",
    workflows=[ComplianceWorkflow],
    activities=[assess_risk],
    nexus_service_handlers=[ComplianceNexusServiceHandler()],
)
```

That's the entire wiring story on the handler side. The Worker now polls `compliance-risk` for **both** Workflow tasks and Nexus tasks.

In **T2**, start it:

```bash
uv run python -m compliance.worker
```

> **Check.** Expect `Compliance Worker started on: compliance-risk`. Leave it running.

### Register the Endpoint

The Worker is polling, but the Server doesn't yet know the name `compliance-endpoint` should route to it. **Think of this as saving a phone contact:** the *name* `compliance-endpoint`, the *number* `compliance-namespace + compliance-risk`.

In **T4**:

```bash
temporal operator nexus endpoint create \
  --name compliance-endpoint \
  --target-namespace compliance-namespace \
  --target-task-queue compliance-risk
```

> **Check.** `temporal operator nexus endpoint list` includes `compliance-endpoint`.

---

## Module 7 — TODO 4: swap the Activity stub for a Nexus stub

> **~3 min · Do**

Open `payments/workflow.py`. One line changes — the compliance call goes through Nexus instead of a shared Activity.

```python
# payments/workflow.py
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

**What changed:** `workflow.execute_activity(check_compliance_activity, ...)` became `compliance_client.execute_operation(ComplianceNexusService.check_compliance, ...)`. Same method shape; durable across the Namespace boundary.

`schedule_to_close_timeout` is the **Nexus Operation's total budget** — covers Nexus Machinery retries, scheduling delay, *and* the underlying Workflow's runtime.

> **Subtle but important.** The endpoint name lives **inside the Workflow** in Python (`create_nexus_client(endpoint=...)`). The Compliance team can rewrite their Workflow, scale it, or move it — as long as the Service contract and Endpoint name stay the same, your code doesn't change.

> **Check.** `uv run mypy payments/workflow.py` should pass.

---

## Module 8 — TODO 5: start the Payments Worker — Checkpoint 2

> **~3 min · Do + check**

Open `payments/worker.py`. Register the Workflow and the remaining Activities. **No special Nexus configuration at the Worker level in Python** — register everything normally:

```python
# payments/worker.py
worker = Worker(
    client,
    task_queue="payments-processing",
    workflows=[PaymentProcessingWorkflow],
    activities=[validate_payment, execute_payment],
)
```

In **T3**, start it:

```bash
uv run python -m payments.worker
```

In **T4**, run the three transactions again:

```bash
uv run python -m payments.starter --txn TXN-A
uv run python -m payments.starter --txn TXN-B
uv run python -m payments.starter --txn TXN-C
```

> **Check.** In the Web UI, switch between `payments-namespace` and `compliance-namespace`. You'll see:
>
> - **`payments-namespace`**: three `PaymentProcessingWorkflow` Executions.
> - **`compliance-namespace`**: three `ComplianceWorkflow` Executions with IDs `compliance-TXN-A`, `compliance-TXN-B`, `compliance-TXN-C`.
>
> Open `TXN-A`'s payment Workflow Event History. You should see `NexusOperationScheduled` → `NexusOperationStarted` → `NexusOperationCompleted`, each tagged with `compliance-endpoint`. The call crossed the boundary durably.

---

## Module 9 — Checkpoint 3: durability across the boundary

> **~4 min · Fail + do + check**

This is the demo that sells Nexus. You'll deliberately kill the Compliance Worker mid-call and watch the payment Workflow survive without any code on your part.

`ComplianceWorkflow` has a `workflow.sleep(timedelta(seconds=10))` inside it — a deliberate 10-second window where the Compliance side is "thinking." That gives you time to break it.

### Break it

In **T4**, start a fresh transaction:

```bash
uv run python -m payments.starter --txn TXN-A-KILL
```

In **T2**, **immediately** hit `Ctrl+C` to kill the Compliance Worker. Do this within the 10-second window.

### Observe

In the Web UI, open `TXN-A-KILL` in `payments-namespace` and refresh.

> **Check.** You should see:
>
> - A **"Pending Nexus Operation"** indicator with a "Started" badge.
> - An attempt counter ticking upward — the Nexus Machinery is retrying delivery, but no Worker is there to receive it.
> - The payment Workflow itself is **healthy** — durably waiting, not crashed.

### Recover

In **T2**, restart the Compliance Worker:

```bash
uv run python -m compliance.worker
```

Refresh the Web UI. Within a few seconds the pending operation transitions to `NexusOperationCompleted` and the payment Workflow finishes.

> **Check.** Open `compliance-TXN-A-KILL` in `compliance-namespace`. There is **only one Workflow Execution** — no duplicate Compliance check ran, even though the call was retried while the Worker was dead. That's because the handler used `start_workflow` with a deterministic ID, which is idempotent.

**At-least-once delivery + deterministic IDs = exactly-once business effect**, surviving Worker outages, network partitions, and Temporal Server hops. That's the property that makes Nexus durable.

<details>
<summary>What if the Compliance Worker never comes back?</summary>

The Nexus Operation retries until its `schedule_to_close_timeout` (10 minutes in Module 7) is reached, then fails with a `NexusOperationError` in the caller Workflow — handle it the same way you'd handle any Activity failure. The point: the failure surfaces as an *exception on your terms*, not a crash or silent data loss.

</details>

---

## Module 10 — Wrap-up

> **~1 min**

You transformed a single-Worker, single-Namespace monolith into two independent Workers in two Namespaces, deployable separately, owned by different teams — with one shared typed contract and one durable cross-Namespace call.

```diff
- compliance_result = await workflow.execute_activity(check_compliance_activity, ...)
+ compliance_result = await compliance_client.execute_operation(
+     ComplianceNexusService.check_compliance, ...,
+ )
```

**One line in your Workflow. Same durability guarantees.**

### When to reach for Nexus vs. alternatives

A quick decision rule:

- **Activity** — a side-effect or external call that needs retries and timeouts but doesn't span teams.
- **Child Workflow** — a sub-routine *owned by the same team, in the same Namespace*. Lower overhead than Nexus.
- **Nexus** — calls into another team's Namespace, across deployment boundaries, or across SDK languages.

**Heuristic:** "Same team / same Namespace" → Child Workflow. "Different team / different Namespace" → Nexus.

### Going further

- **Human-in-the-loop reviews** (synchronous Nexus Operations + Workflow Updates) — see the [Nexus async + sync patterns guide](https://docs.temporal.io/develop/python/nexus#develop-nexus-service-operation-handlers).
- **Cancellation propagation** — cancel the caller Workflow, Nexus propagates across the boundary. See [Cancel a Nexus Operation](https://docs.temporal.io/develop/python/nexus#canceling-a-nexus-operation).
- **Nexus on Temporal Cloud** — cross-Namespace calls with mTLS and allowlists. See [Nexus on Temporal Cloud](https://docs.temporal.io/develop/python/nexus#nexus-calls-across-namespaces-temporal-cloud).

---

## Appendix — Local setup (optional)

> Setup time here is **not counted** in the ~30 min tutorial budget. Instruqt is the recommended path because it avoids environment-setup variance.

If you can't use Instruqt and want to run locally:

```bash
git clone https://github.com/temporalio/edu-nexus-code.git
cd edu-nexus-code/python
uv sync
```

Open **four terminals** in this directory:

| Terminal | Role                  |
|----------|-----------------------|
| **T1**   | Temporal dev server   |
| **T2**   | Compliance Worker     |
| **T3**   | Payments Worker       |
| **T4**   | Starters (CLI)        |

In **T1**, start the dev server with both Namespaces pre-created:

```bash
temporal server start-dev \
  --namespace payments-namespace \
  --namespace compliance-namespace \
  --ui-port 8233
```

Open `http://localhost:8233`. You should see the Namespace dropdown showing `default`, `payments-namespace`, and `compliance-namespace`. From here, return to **Module 1**.

**Requirements:** Python 3.10+, [`uv`](https://docs.astral.sh/uv/), [Temporal CLI](https://docs.temporal.io/cli) v1.3.0+, `temporalio>=1.14.1`.

---

*Tutorial code: <https://github.com/temporalio/edu-nexus-code/tree/main/python>*
*Questions? Join us in [Temporal Community Slack](https://t.mp/slack).*
