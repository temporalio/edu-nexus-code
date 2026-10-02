# Step 6 — Sandbox & Instruqt Spec

Target repo: `github.com/temporalio/edu-nexus-code` → `/python` folder
Primary delivery: **Instruqt** · Local: optional appendix (see `TUTORIAL.md`)

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
   │  └─ devcontainer.json        # Python 3.11 + Temporal CLI pre-installed (local-only)
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
   │  ├─ workflow.py              # ComplianceWorkflow (pre-provided, includes 10s workflow.sleep)
   │  ├─ nexus_handler.py         # TODO 2 — student writes the async handler
   │  └─ worker.py                # TODO 3 — student adds nexus_service_handlers=[...]
   └─ payments/                   # caller side after decoupling
      ├─ __init__.py
      ├─ activities.py            # validate_payment, execute_payment (Compliance gone)
      ├─ workflow.py              # TODO 4 — Activity stub → Nexus stub
      ├─ worker.py                # TODO 5 — register caller workflows
      └─ starter.py               # `python -m payments.starter --txn TXN-A`
```

**Scope note:** The human-review path (sync Nexus operation + Workflow Update) and the `submit_review` operation are **not** in the main tutorial. They're flagged in *Going further* as a follow-on. Keeping the main path tight to fit the ~30 min Instruqt budget. If we add a Part 2 later, the files (`payments/review_caller.py`, `payments/review_starter.py`, the `@workflow.update review` method on `ComplianceWorkflow`, and the `submit_review` handler) all slot in cleanly without changing the existing structure.

## Solution branches

Per workshop feedback ("Iterative, progressive exercise structure (demo 1 → demo 5) was well received"), keep one branch per checkpoint so learners can recover at any point.

| Branch                  | What's complete                                          |
|-------------------------|----------------------------------------------------------|
| `main`                  | Starter state — Module 0 setup, monolith runs, TODOs 1–5 unfilled |
| `solution/todo-1`       | Service contract decorated (Module 4 done)               |
| `solution/todo-2`       | Handler implemented (Module 5 done)                      |
| `solution/todo-3`       | Compliance Worker registers handler + Endpoint created (Module 6 done) |
| `solution/todo-4`       | Payments Workflow uses Nexus stub (Module 7 done)        |
| `solution/todo-5`       | Payments Worker fully wired (Module 8 done)              |
| `solution/complete`     | All TODOs done + durability demo verified                |

## Pre-provided complete files (no TODOs inside)

- `monolith/*` — entire baseline app
- `compliance/workflow.py` — full `ComplianceWorkflow` with the deliberate `workflow.sleep(timedelta(seconds=10))` that enables the kill-worker demo
- `compliance/activities.py` — `assess_risk` activity
- `payments/activities.py` — `validate_payment`, `execute_payment`
- `payments/starter.py`, `monolith/starter.py`

## devcontainer (local path only)

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

**Why Instruqt is the recommended path:** workshop feedback flagged technical setup (Docker rate limits, WiFi, corporate laptops, API keys) as the *single most-cited issue* across five sessions. Instruqt eliminates that variance entirely. The devcontainer above exists only for learners who can't use Instruqt.

## Instruqt mapping

| Tutorial Module | Instruqt step                              | Initial state         | Auto-checks                                                                          |
|-----------------|--------------------------------------------|-----------------------|--------------------------------------------------------------------------------------|
| 0               | "Welcome & setup"                          | `main`                | `temporal --version` passes, dev server reachable on `:7233`                          |
| 1               | "Run the monolith"                         | `main`                | Three workflow executions appear in `default` namespace with expected statuses        |
| 2               | "What's wrong with this picture?"          | `main`                | Self-check via the reveal `<details>` — no Instruqt auto-check (reflection step)      |
| 3               | "Nexus building blocks"                    | `main`                | None (read-only)                                                                      |
| 4               | "TODO 1: Service contract"                 | `main`                | `mypy shared/nexus_service.py` passes                                                 |
| 5               | "TODO 2: Handler"                          | `solution/todo-1`     | `mypy compliance/nexus_handler.py` passes                                             |
| 6               | "TODO 3: Worker + Endpoint"                | `solution/todo-2`     | grep stdout for `"Compliance Worker started on: compliance-risk"`; `endpoint list` includes `compliance-endpoint` |
| 7               | "TODO 4: Stub swap"                        | `solution/todo-3`     | `mypy payments/workflow.py` passes                                                    |
| 8               | "TODO 5: Payments Worker + Checkpoint 2"   | `solution/todo-4`     | Both namespaces have workflows post-starter run; `NexusOperationCompleted` event seen |
| 9               | "Checkpoint 3: durability"                 | `solution/todo-5`     | Manual check — Pending Nexus Operation banner appears, then completion after restart  |
| 10              | "Wrap-up"                                  | `solution/complete`   | None                                                                                  |

### Instruqt setup scripts (per-track)

- `setup-host`: install Temporal CLI, install `uv`, `uv sync`, start dev server in background, create both namespaces. (The Endpoint is created by the learner in Module 6 — don't pre-create it.)
- `cleanup`: stop dev server, no-op.

**Keep environments live for 1–2 weeks post-event** (per workshop feedback) — configure Instruqt track lifespan accordingly.

## Pre-flight checklist for shipping

- [ ] Pre-event email sent **48+ hours before** workshop with Instruqt link (per feedback "Send setup + GitHub link 48hrs early")
- [ ] Instruqt environments configured for **1–2 week** post-event lifespan
- [ ] Repo link and tutorial URL ready to share in chat at workshop start
- [ ] AV/sound checked for any video segments (per Replay 2026 Nexus session feedback)
