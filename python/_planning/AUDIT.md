# Step 7 — Pacing & Quality Audit + Handoff

## Pacing audit

Modality time per module (rough estimates — refine after a dry run with one learner). **The Instruqt path is the budget — local setup time is excluded** (it lives in the appendix).

| # | Module                                   | Passive | Active | Modalities                | Failure exercise |
|---|------------------------------------------|---------|--------|---------------------------|------------------|
| 0 | Get the lab running (Instruqt)            | 1 min   | 1 min  | Read, Check               | —                |
| 1 | Run the monolith                          | 0       | 3 min  | Do, Check                 | —                |
| 2 | What's wrong with this picture?           | 1 min   | 1 min  | Reflect, Read             | —                |
| 3 | Nexus building blocks                     | 3 min   | 0      | Read                      | —                |
| 4 | TODO 1: Service contract                  | 1 min   | 2 min  | Read, Do, Check           | —                |
| 5 | TODO 2: Async handler                     | 2 min   | 3 min  | Read, Do, Check           | —                |
| 6 | TODO 3: Compliance Worker + Endpoint      | 1 min   | 3 min  | Read, Do, Check           | —                |
| 7 | TODO 4: Stub swap                         | 1 min   | 2 min  | Read, Do, Check           | —                |
| 8 | TODO 5: Payments Worker + Checkpoint 2    | 0       | 3 min  | Do, Check                 | —                |
| 9 | Checkpoint 3: durability (kill-worker)    | 1 min   | 3 min  | Fail, Do, Check, Explore  | **Yes**          |
| 10| Wrap-up + decision guide                  | 1 min   | 0      | Read                      | —                |
| **Total** |                                  | **11 min** | **21 min** | —                  | 1 of 11          |

**Total: ~32 min** — within the 30 min target with normal user variance. **Active ratio: 66%** — clears the 60% feedback target.

**"Do, then understand" flow.** The first hands-on contact with the codebase is Module 1 (run the monolith). The conceptual framing for *why* it's a problem follows in Module 2 as a Socratic "what's wrong here?" reflection — learner observes the running system, predicts the problems, then gets the reveal. Nexus terminology in Module 3 is then just-in-time, immediately before TODO 1.

**Three-minute-rule violations:** None. The longest passive stretch is Module 3 at 3 min, sandwiched between Module 2's reflect-then-reveal and Module 4's hands-on TODO.

**Single-modality concepts:** Module 3 is read-only. Acceptable — it's the just-in-time terminology reference before the transformation arc.

**Failure-exercise count:** 1 of 11 modules. The signature kill-the-worker demo is the right place for the one big failure — earlier modules are guided transformation where failure exercises would be confusing.

**Cut from the original 75-min draft** (preserved in the SANDBOX scope note for a possible Part 2):
- Module 11 (Human review path — sync Nexus operation + Workflow Update)
- Module 12 (Quiz + scenario questions). The decision-guide rule survives as a 3-bullet callout in the Module 10 wrap-up.

## Terminology audit

Cross-referenced against Temporal terminology guidance (e.g., the disambiguation rules tracked by the Temporal Edu team).

| Term used                | Disambiguated correctly? | Notes                                                                      |
|--------------------------|--------------------------|----------------------------------------------------------------------------|
| Nexus Service            | ✅                       | Always "Nexus Service" on first use, never bare "service"                  |
| Nexus Operation          | ✅                       |                                                                            |
| Nexus Endpoint           | ✅                       | "Endpoint" used standalone only after Nexus Endpoint introduced; never confused with namespace endpoint |
| Nexus Task Queue         | ✅                       | "Task Queue" qualified everywhere a Nexus handler polls                    |
| Workflow Execution       | ✅                       | Used for running instances; "Workflow Definition" implied by code blocks   |
| Activity Execution       | ✅                       | Module 3 specifically calls out the "Activity Execution" framing            |
| Schedule-To-Close Timeout| ✅                       | Always typed; bare "timeout" never used                                    |
| Event History            | ✅                       | Capitalized, never "history" alone                                         |
| Namespace                | ✅                       | Capitalized; no confusion with Kubernetes namespace                        |
| Worker                   | ✅                       | "Worker" (process) — no Worker Versioning ambiguity in tutorial            |

**No ambiguous terms left unresolved.** Product names cased correctly throughout (Temporal, Python, Nexus).

## Completeness check

| Learning outcome                                                                                              | Where addressed       | Verified? |
|---------------------------------------------------------------------------------------------------------------|-----------------------|-----------|
| 1. Register a Nexus Endpoint using the Temporal CLI                                                           | Module 6              | ✅        |
| 2. Define a Nexus Service contract in Python                                                                  | Module 4              | ✅        |
| 3. Implement an asynchronous Nexus Operation handler                                                          | Module 5              | ✅        |
| 4. Replace an Activity call with a durable Nexus call                                                         | Module 7              | ✅        |
| 5. Diagnose Nexus operations in the Web UI Event History                                                      | Modules 8, 9          | ✅        |
| 6. Decide when Nexus is the right tool vs. Child Workflows or shared Activities                               | Module 1 + Module 10 decision rule | ✅ |

Every outcome has at least one exercise with explicit success criteria.

Every checkpoint has a way to recover (solution branches per `SANDBOX.md`).

> **Outcome 3 was scoped down from "sync and async handlers" to "asynchronous handler"** to fit the 30-min budget. The sync handler (Workflow Update via `submit_review`) is signposted in *Going further*.

## Open questions / things to confirm before publishing

1. **Python Nexus SDK version pinning.** Tutorial code uses `nexus.WorkflowRunOperationContext` and `workflow.create_nexus_client(...)`. Pin `temporalio>=1.14.1` per the docs and verify against the actual version when shipping.
2. **Diagram production.** Two `<!-- DIAGRAM: ... -->` placeholders in `TUTORIAL.md`. User mentioned wanting to improve diagrams — these are flagged inline with what each should depict.
3. **Module 1's "what fails without Nexus" comparison table** uses words like "wrap" and "shared." Have a Temporal SME confirm this isn't misleading for users coming from monolith backgrounds.

## Handoff

**Status:** Ready for `/edu:content-reviewer` to run a technical-accuracy + voice pass.

**Suggested reviewer focus areas:**
- Python Nexus API surface accuracy — every code block in `TUTORIAL.md` should be runnable as-is on `temporalio>=1.14.1`
- Voice consistency with the existing Java tutorial on learn.temporal.io
- The decision-rule callout in Module 10 (Wrap-up) — new content, not ported from Java
- The "kill-the-worker" exercise wording in Module 9 — the timing-sensitive step needs to be unambiguous

**After reviewer pass:** open a follow-up PR scaffolding the actual `/python` code per `SANDBOX.md`, plus a sibling PR against the docs repo (wherever `learn.temporal.io/tutorials/nexus/nexus-sync-tutorial-java/` lives) for the prose.
