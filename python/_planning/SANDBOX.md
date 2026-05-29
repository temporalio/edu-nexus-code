# Step 6 — Sandbox & Instruqt Spec

Target repo: `github.com/temporalio/edu-nexus-code` → `/python` folder
Primary delivery: **Instruqt** · Local fallback: **`uv` + devcontainer**

## Repo structure

```
edu-nexus-code/
├─ java/                          # (already exists, untouched)
└─ python/
   ├─ README.md                   # quickstart + which-tutorial-this-is pointer
   ├─ pyproject.toml              # uv-managed deps
   ├─ uv.lock
   ├─ .python-version             # "3.11"
   ├─ .env.example                # only TEMPORAL_ADDRESS (defaults to localhost:7233)
   ├─ .devcontainer/
   │  └─ devcontainer.json        # Python 3.11 + Temporal CLI pre-installed
   ├─ shared/
   │  ├─ __init__.py
   │  └─ nexus_service.py         # TODO 1 — student fills in decorators
   ├─ monolith/                   # baseline (Module 3)
   │  ├─ __init__.py
   │  ├─ workflow.py              # PaymentProcessingWorkflow (baseline w/ shared Activity)
   │  ├─ activities.py            # validate_payment, check_compliance_activity, execute_payment
   │  ├─ worker.py                # single-Worker monolith on `payments-processing` queue
   │  └─ starter.py               # `python -m monolith.starter --txn TXN-A`
   ├─ compliance/                 # handler side after decoupling
   │  ├─ __init__.py
   │  ├─ activities.py            # assess_risk
   │  ├─ workflow.py              # ComplianceWorkflow (pre-provided, @workflow.update review)
   │  ├─ nexus_handler.py         # TODO 2 — student writes both handlers
   │  └─ worker.py                # TODO 3 — student adds nexus_service_handlers=[...]
   ├─ payments/                   # caller side after decoupling
   │  ├─ __init__.py
   │  ├─ activities.py            # validate_payment, execute_payment (Compliance gone)
   │  ├─ workflow.py              # TODO 4 — Activity stub → Nexus stub
   │  ├─ review_caller.py         # pre-provided ReviewCallerWorkflow
   │  ├─ worker.py                # TODO 5 — register caller workflows
   │  ├─ starter.py               # `python -m payments.starter --txn TXN-A`
   │  └─ review_starter.py        # `python -m payments.review_starter --txn TXN-B --approve true`
   ├─ scripts/
   │  ├─ create_namespaces.sh     # idempotent — used by Instruqt setup + local README
   │  └─ create_endpoint.sh       # idempotent — runs the `nexus endpoint create` command
   └─ tests/
      ├─ test_contracts.py        # nexusrpc service shape, dataclass round-trip
      ├─ test_compliance_workflow.py   # WorkflowEnvironment, real Update path
      ├─ test_payments_workflow.py     # WorkflowEnvironment + mock Nexus client
      └─ test_e2e_nexus.py             # full handshake on a real dev server (skipped by default)
```

## Solution branches

Per workshop feedback ("Iterative, progressive exercise structure (demo 1 → demo 5) was well received"), keep the parity of one branch per checkpoint so learners can recover at any point.

| Branch                  | What's complete                                          |
|-------------------------|----------------------------------------------------------|
| `main`                  | Starter state — Module 0 setup, monolith runs, TODOs 1–5 unfilled |
| `solution/todo-1`       | Service contract decorated (Module 4 done)               |
| `solution/todo-2`       | Handlers implemented (Module 5 done)                     |
| `solution/todo-3`       | Compliance Worker registers handlers (Module 6 done)     |
| `solution/todo-4`       | Payments Workflow uses Nexus stub (Module 8 done)        |
| `solution/todo-5`       | Payments Worker fully wired (Module 9 done)              |
| `solution/complete`     | All TODOs done + tests pass + review path works          |

## Pre-provided complete files (no TODOs inside)

These exist as-is at all stages. They're either Module-3 baseline scaffolding or the human-review machinery from Module 11 that would be a distraction to write from scratch.

- `monolith/*` — entire baseline app
- `compliance/workflow.py` — full `ComplianceWorkflow` with `@workflow.update review`, `wait_condition`, and the deliberate `workflow.sleep(timedelta(seconds=10))` that enables the kill-worker demo
- `compliance/activities.py` — `assess_risk` activity
- `payments/activities.py` — `validate_payment`, `execute_payment`
- `payments/review_caller.py` — thin Nexus-calling wrapper
- `payments/review_starter.py`, `payments/starter.py`, `monolith/starter.py`

## devcontainer

```json
{
  "name": "edu-nexus-code-python",
  "image": "mcr.microsoft.com/devcontainers/python:3.11",
  "features": {
    "ghcr.io/devcontainers/features/common-utils:2": {}
  },
  "postCreateCommand": "curl -sSf https://temporal.download/cli.sh | sh && pip install uv && uv sync",
  "forwardPorts": [7233, 8233],
  "portsAttributes": {
    "8233": { "label": "Temporal Web UI", "onAutoForward": "openBrowser" }
  }
}
```

**Why pre-install everything:** workshop feedback flagged technical setup (Docker rate limits, WiFi, corporate laptops, API keys) as the *single most-cited issue* across five sessions. Devcontainer + Instruqt eliminate the setup variance.

## Instruqt mapping

| Tutorial Module | Instruqt step                       | Initial state         | Auto-checks                                                                          |
|-----------------|-------------------------------------|-----------------------|--------------------------------------------------------------------------------------|
| 0               | "Welcome & setup"                   | `main`                | `temporal --version` passes, dev server reachable on `:7233`                          |
| 1–2             | "The problem & Nexus building blocks" | `main`              | None (read-only)                                                                      |
| 3               | "Checkpoint 0: run the monolith"    | `main`                | Three workflow executions appear in `default` namespace with expected statuses        |
| 4               | "TODO 1: Service contract"          | `main`                | `pytest tests/test_contracts.py::test_service_decorated` passes                       |
| 5               | "TODO 2: Handlers"                  | `solution/todo-1`     | `mypy compliance/nexus_handler.py` passes; `pytest tests/test_compliance_workflow.py` |
| 6               | "TODO 3: Compliance Worker + Checkpoint 1" | `solution/todo-2` | grep stdout for `"Compliance Worker started on: compliance-risk"`                     |
| 7               | "Checkpoint 1.5: create the Endpoint" | `solution/todo-2`   | `temporal operator nexus endpoint list` includes `compliance-endpoint`                 |
| 8               | "TODO 4: Stub swap"                 | `solution/todo-2` + endpoint | `pytest tests/test_payments_workflow.py::test_nexus_call_shape` passes        |
| 9               | "TODO 5: Payments Worker + Checkpoint 2" | `solution/todo-4` | Both namespaces have workflows post-starter run; `NexusOperationCompleted` event seen |
| 10              | "Checkpoint 3: durability"          | `solution/todo-5`     | Manual check — Pending Nexus Operation banner appears, then completion after restart  |
| 11              | "Human review"                      | `solution/todo-5`     | Review starter exits 0; TXN-B workflow shows `Completed` in `payments-namespace`      |
| 12              | "Quiz & decision guide"             | `solution/complete`   | Inline quiz answers (Instruqt's built-in quiz mechanic)                                |
| 13              | "Wrap-up"                           | `solution/complete`   | None                                                                                  |

### Instruqt setup scripts (per-track)

- `setup-host`: install Temporal CLI, install `uv`, `uv sync`, start dev server in background, create both namespaces, run `scripts/create_endpoint.sh` for Module 7 onward.
- `cleanup`: stop dev server, no-op.

**Keep environments live for 1–2 weeks post-event** (per workshop feedback) — configure Instruqt track lifespan accordingly.

## Test strategy

Per `AGENTS.md` non-negotiable #6 (majority integration tests on real DB/API, minority unit, plus E2E):

| Layer            | Test                                  | What it exercises                                                       |
|------------------|---------------------------------------|-------------------------------------------------------------------------|
| Unit             | `test_contracts.py`                   | Service decorator present, dataclasses serialize/deserialize round-trip |
| Integration      | `test_compliance_workflow.py`         | Real `WorkflowEnvironment`, `assess_risk` activity, `review` Update     |
| Integration      | `test_payments_workflow.py`           | Real `WorkflowEnvironment` + recorded Nexus client shim                  |
| E2E              | `test_e2e_nexus.py`                   | Full dev server, both namespaces, real endpoint, three transactions    |
| E2E (manual)     | "kill-the-worker" script in `tests/manual/` | Helper that scripts the Module 10 outage demo for CI verification        |

All tests must **fail loudly** if a required env var is unset — never skip. (AGENTS.md non-negotiable #10.)

## Pre-flight checklist for shipping

- [ ] Pre-event email sent **48+ hours before** workshop with Instruqt link + local-clone command (per feedback "Send setup + GitHub link 48hrs early")
- [ ] All API keys pre-provisioned and tested morning-of (no live external API dependency in this tutorial — confirmed)
- [ ] Instruqt environments configured for **1–2 week** post-event lifespan
- [ ] Repo link, slide deck, and tutorial URL ready to share in chat at workshop start
- [ ] AV/sound checked for any video segments (per Replay 2026 Nexus session feedback)
