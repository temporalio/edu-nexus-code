# Step 7 — Pacing & Quality Audit + Handoff

## Pacing audit

Core workshop target is **30 minutes**. The table below is the full extended walkthrough timing; use the core path for delivery and keep the rest as optional follow-up.

| # | Module                                   | Passive | Active | Modalities                | Failure exercise |
|---|------------------------------------------|---------|--------|---------------------------|------------------|
| 0 | Get the lab running                       | 1 min   | 3 min  | Read, Do, Check           | —                |
| 1 | The problem: shared blast radius          | 3 min   | 0      | Read                      | —                |
| 2 | Nexus building blocks                     | 4 min   | 1 min  | Read, Check               | —                |
| 3 | Checkpoint 0: run the monolith            | 0       | 6 min  | Do, Check                 | —                |
| 4 | TODO 1: Service contract                  | 1 min   | 4 min  | Read, Do, Check           | —                |
| 5 | TODO 2: Handlers                          | 3 min   | 7 min  | Read, Do, Explore         | —                |
| 6 | TODO 3: Compliance Worker + Checkpoint 1  | 0       | 5 min  | Do, Check                 | —                |
| 7 | Checkpoint 1.5: Endpoint                  | 1 min   | 2 min  | Read, Do, Check           | —                |
| 8 | TODO 4: Stub swap                         | 1 min   | 4 min  | Read, Do, Check           | —                |
| 9 | TODO 5: Payments Worker + Checkpoint 2    | 0       | 8 min  | Do, Check                 | —                |
| 10| Checkpoint 3: durability (kill-worker)    | 1 min   | 7 min  | Fail, Do, Check, Explore  | **Yes**          |
| 11| Human review path                         | 1 min   | 5 min  | Do, Check                 | —                |
| 12| Quiz + Nexus vs. Child Workflow guide     | 3 min   | 5 min  | Read, Check               | —                |
| 13| What you built + what's next              | 2 min   | 0      | Read                      | —                |
| **Total (extended path)** |                  | **21 min** | **57 min** | —                  | 1 of 14          |

**Core-path timing:** 8 min passive + 22 min active = **30 min total** (73% active), clearing the 60% target with margin.

**Three-minute-rule violations:** Module 1 has 3 min of passive content with no active step. Acceptable because Module 0 ended with a "Do" (start the dev server) and Module 2 ends with a quick-check. If you want to tighten this, fold Module 1's third paragraph into Module 2 as a sidebar.

**Single-modality concepts:** Module 1 is read-only. As above — acceptable for a problem-framing section sandwiched between hands-on steps.

**Failure-exercise count:** 1 of 14 modules. The Java tutorial is similar (the kill-worker demo is the only true failure exercise). The structure justifies it — the *first half* of the tutorial is a guided transformation where failure exercises would be confusing. The durability proof at the end is the right place for the one big failure.

## Terminology audit

Cross-referenced against `_shared/terminology.md`.

| Term used                | Disambiguated correctly? | Notes                                                                      |
|--------------------------|--------------------------|----------------------------------------------------------------------------|
| Nexus Service            | ✅                       | Always "Nexus Service" on first use, never bare "service"                  |
| Nexus Operation          | ✅                       |                                                                            |
| Nexus Endpoint           | ✅                       | "Endpoint" used standalone only after Nexus Endpoint introduced; never confused with namespace endpoint |
| Nexus Task Queue         | ✅                       | "Task Queue" qualified everywhere a Nexus handler polls                    |
| Workflow Execution       | ✅                       | Used for running instances; "Workflow Definition" implied by code blocks   |
| Workflow Update          | ✅                       | Used for the `@workflow.update review` primitive — primitive, not software |
| Activity Execution       | ✅                       | Module 3 specifically calls out the "Activity Execution" framing            |
| Schedule-To-Close Timeout| ✅                       | Always typed; bare "timeout" never used                                    |
| Event History            | ✅                       | Capitalized, never "history" alone                                         |
| Namespace                | ✅                       | Capitalized; no confusion with Kubernetes namespace                        |
| Worker                   | ✅                       | "Worker" (process) — no Worker Versioning ambiguity in tutorial            |

**No ambiguous terms left unresolved.** Product names cased correctly throughout (Temporal, Python, Nexus).

## Completeness check

| Learning outcome (Step 1)                                                                                    | Where addressed       | Verified? |
|--------------------------------------------------------------------------------------------------------------|-----------------------|-----------|
| 1. Register a Nexus Endpoint using the Temporal CLI                                                          | Module 7              | ✅        |
| 2. Define a Nexus Service contract in Python using `@nexusrpc.service` and `nexusrpc.Operation` type hints     | Module 4 + quiz Q3    | ✅        |
| 3. Implement sync and async Nexus Operation handlers in Python                                               | Module 5 + quiz Q4    | ✅        |
| 4. Replace an Activity call with a durable Nexus call in a Workflow                                          | Module 8 + Module 11 (reuse pattern) | ✅ |
| 5. Diagnose Nexus operations in the Web UI Event History                                                     | Modules 9, 10         | ✅        |
| 6. Decide when Nexus is the right tool vs. Child Workflows or shared Activities                              | Module 1 + Module 12 decision guide + Module 12 scenarios | ✅ |

Every outcome has at least one exercise with explicit success criteria.
Every checkpoint has a way to recover (solution branches per Step 6 spec).

## Open questions / things to confirm before publishing

1. **Python Nexus SDK version pinning.** Tutorial code uses `nexus.client().get_workflow_handle_for(...)` and `nexus.WorkflowRunOperationContext` — confirm these signatures against the version of `temporalio` you pin in `pyproject.toml`. Pin to `>=1.14.1` per the docs, but verify against the actual version when shipping.
2. **The pre-provided `ComplianceWorkflow.review` Update method shape.** Tutorial assumes `execute_update(ComplianceWorkflow.review, input)` returns `ComplianceResult`. Verify by running `solution/complete` end-to-end before publishing.
3. **Diagram production.** Five `<!-- DIAGRAM: ... -->` placeholders in `TUTORIAL.md`. User mentioned wanting to improve diagrams — these are flagged inline with what each should depict.
4. **Module 1's "what fails without Nexus" comparison table** uses words like "wrap" and "shared." Have a Temporal SME confirm this isn't misleading for users coming from monolith backgrounds.

## Handoff

**Status:** Ready for `/edu:content-reviewer` to run a technical-accuracy + voice pass.

**Suggested reviewer focus areas:**
- Python Nexus API surface accuracy — every code block in `TUTORIAL.md` should be runnable as-is on `temporalio>=1.14.1`
- Voice consistency with the existing Java tutorial on learn.temporal.io
- The Nexus vs. Child Workflow decision guide (Module 12) — new content, not ported from Java, deserves extra scrutiny
- The "kill-the-worker" exercise wording in Module 10 — the timing-sensitive step needs to be unambiguous

**After reviewer pass:** open a PR against `temporalio/edu-nexus-code` adding the `/python` folder and a sibling PR against the docs repo (wherever `learn.temporal.io/tutorials/nexus/nexus-sync-tutorial-java/` lives) for the prose.
