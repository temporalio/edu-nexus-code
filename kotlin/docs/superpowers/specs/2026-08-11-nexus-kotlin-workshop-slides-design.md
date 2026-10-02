# Slidev deck: Decouple a Monolith with Temporal Nexus (Kotlin)

Date: 2026-08-11
Status: approved design, not yet built

## Goal

A Slidev deck for the 90-minute live workshop backed by the Instruqt track
`nexus-kotlin-decouple-monolith`. The deck is the connective tissue between five
hands-on challenges, not a standalone talk.

Nikk authors every slide's content. Claude teaches the Temporal concepts in depth,
supplies Slidev scaffolding and paste-ready syntax without teaching it, and reviews.
Claude does not write slide content.

## Constraints

- 90-minute session, 40 minutes of it lab. Deck owns ~37 speaking minutes.
- Audience knows Temporal (Workflows, Activities, Workers, Task Queues) and is new
  to Nexus. Zero durable-execution primer.
- Kotlin on the Temporal **Java** SDK.
- No existing deck. Nikk has never used Slidev and does not want to spend the
  session learning it — Slidev is a means, the Temporal concepts are the point.
- Pacing reality: `track.yml` budgets 120 min of challenge time into a 40-minute
  live block. Most attendees finish C1-C2 and get partway into C3. Segment 3 is
  therefore the last content many of them hear before falling back on the
  Solution tab, and is weighted accordingly.

## Architecture

**Interleaved segments**, one short slide block before each challenge.

**Spine: question-first.** Each segment opens with the framing question already
written in that challenge's Instruqt `notes` block, and the slides answer it. The
notes and the slides reinforce rather than duplicate.

**Secondary spine: one evolving diagram.** A single system diagram in five states,
rendered in Mermaid (not hand-authored SVG). Boxes and arrows only. It carries the
one idea the workshop is about.

## Segment map

Times are speaking minutes; bracketed numbers are lab minutes.

| # | Before | Slides | Min | Concept it carries |
|---|---|---|---|---|
| 0 | — | 10 | 11 | Open: the 3 AM blast radius. What you will have built. Then the concept block: one definition-plus-code slide per Nexus concept. |
| 1 | C1 | 2 | 3 | Shared Worker = shared failure domain. Diagram state 1: monolith. |
| | | | [10] | *Lab: observe the coupling* |
| 2 | C2 | 3 | 4 | Why not HTTP. State 2: contract inserted. Recall, not first exposure. |
| | | | [8] | *Lab: write the contract* |
| 3 | C3 | 3 | 5 | The duplicate-Workflow failure. Three Worker registrations. State 3: handler + endpoint. |
| | | | [15] | *Lab: implement the handler* |
| 4 | C4 | 2 | 3 | Who names what. TXN-B changed. State 4: call crosses the namespace line. |
| | | | [10] | *Lab: swap the stub* |
| 5 | C5 | 2 | 4 | Handler disappears; caller waits instead of failing. State 5: handler dark, arrow parked. |
| | | | [7] | *Lab: break it* |
| 6 | — | 3 | 5 | Recap table (reuse the one in C5). When *not* to reach for Nexus. |

Total ≈25 slides, ~35 speaking min + 40 lab = 75, leaving ~15 for Q&A and slippage.

### The concept block

The lab assumes vocabulary it never defines. Rather than distribute the teaching, the
deck front-loads it: **one definition-plus-code slide per Nexus concept**, all six in
Segment 0, each showing real Kotlin from the solution tree.

| Slide | Concept | Code shown |
|---|---|---|
| 1 | Service | `@Service interface ComplianceNexusService` |
| 2 | Operation | The two `@Operation` methods, and the validate-everything rule |
| 3 | Endpoint | The `temporal operator nexus endpoint create` command |
| 4 | Caller | `Workflow.newNexusServiceStub` and the call site |
| 5 | Handler | `@ServiceImpl` / `@OperationImpl` |
| 6 | Sync vs async | `WorkflowRunOperation` beside `OperationHandler.sync` |

A `section` divider opens the block.

Namespace and Task Queue are taught inside the Endpoint slide, in their new role as the
two halves of an address — the reframing the audience needs even though they know both
terms. Caller and Handler get named on slides 4 and 5; the lab uses both words from C3
onward without ever introducing them.

**Registry is cut.** C2's notes name it as one of the four Nexus pieces and no challenge
ever touches it.

**This relocates time rather than adding it.** Segment 0 goes 5 → 11 minutes, and the
segments that were teaching these concepts shrink to match: Segment 2 loses its
contract-in-Kotlin slide, Segment 3 loses the two handler-shape slides and the Endpoint
slide, Segment 4 loses the stub slide. Those segments become recall and application
rather than first exposure.

**Known tradeoff, accepted:** attendees meet `WorkflowRunOperation` roughly 40 minutes
before they write it, and Segment 3 drops from 10 minutes to ~5. Segment 3 keeps the
duplicate-Workflow failure slide, which is the part that most needs to be adjacent to
the lab.

### Deliberate weightings

- **The concept block owns a third of speaking time.** Every Nexus idea gets a slide
  with real code before anyone opens the editor.
- **Segment 2 keeps "why not HTTP."** The room accepts durability on faith but will
  not accept a new primitive on faith. That slide protects Q&A.
- **Segment 3 keeps the duplicate-Workflow failure slide** even though sync-vs-async
  is taught up front. It is the mistake that reaches production, and it needs to be
  adjacent to the moment they write the handler.

## Temporal concepts to teach (Claude → Nikk, before each segment)

**This is the primary deliverable.** Ordered by segment. Each is taught to the depth
where Nikk can answer an unscripted attendee question about it, not just narrate a
slide. For each: what it is, the misconception attendees arrive with, the failure it
causes in production, and how it shows up in this specific codebase.

1. Blast radius of a shared Worker process, including Worker task-slot starvation as
   coupling with no bug in it. Namespace as isolation unit, and the fact that the
   monolith already has two Namespaces and gets no isolation from them.
2. Why an HTTP call between teams becomes hand-written retry/timeout/callback
   infrastructure. What Nexus gives instead.
3. Service / Operation / Endpoint / Registry — what each one is and who owns it.
4. Async handler (`WorkflowRunOperation`) vs sync handler (`OperationHandler.sync`);
   Workflow-ID binding and why a retried Operation re-attaches instead of starting a
   duplicate; the 10-second sync budget.
5. Endpoint as the only piece of Nexus outside the code; Namespace + Task Queue
   targeting and the silent failure when the Task Queue has no poller.
6. Caller-side stub; why the Workflow names the contract and the Worker names the
   Endpoint; `scheduleToCloseTimeout` as the outage budget.
7. Operation durability across handler-Worker outage: pending, not failed.
8. When not to use Nexus.

## Existing assets

**Theme:** `github:temporalio/slidev-theme-temporal`, installed as a git dependency
(`pnpm add`), not vendored. Provides layouts `cover`, `default`, `section`,
`exercise`, `two-cols`, `toc`, `end`; a `temporal-dark` Shiki theme; and Mermaid
defaults already matched to the Temporal palette.

The `exercise` layout is the lab-handoff slide: a workshop card with a countdown
timer (`minutes:`), play/pause/restart, persisted in localStorage so it survives
slide navigation and stays synced between presenter and audience views. Use it for
all five lab handoffs.

**Reference deck:** `temporalio/workshop-nexus-intro-instruqt/slides` — Mason
Egger's 3.5-hour Replay 2026 Nexus workshop. Read as reference only. Chapter
overlap with our segments:

| Their chapter | Our segment |
|---|---|
| `ch01-why-nexus` | 1 |
| `ch02-service-contract` | 2 |
| `ch03-sync-handler`, `ch05-async-operations` | 3 |
| `ch04-caller-workflow` | 4 |
| `ch06-updates`, `ch07-lifecycle` | 5 |

**Decision: fresh deck, theme only.** Nikk authors all 23 slides against the segment
map. Their chapters are read for reference and individual framings may be borrowed
deliberately, but nothing is copied wholesale. Writing the explanation is what
teaches the concept — that is the point of this exercise.

Conventions worth adopting from the reference deck: one markdown file per segment
pulled in via `src:`, `<v-clicks>` for progressive builds, ` ```mermaid {scale: 0.55} `
for diagrams, and thick presenter notes in HTML comments (speaker script annotated
build by build, optional material marked skippable).

## Slidev support (not taught)

Claude supplies, Nikk does not study:

1. Project scaffold wired to the theme, plus the dev-server command.
2. Paste-ready snippets for the patterns this deck needs: code blocks with
   per-click line highlighting, `two-cols`, Mermaid blocks, presenter notes,
   `exercise` layout frontmatter.
3. The five Mermaid diagram states as working blocks Nikk can edit.
4. Export/deploy command at the end.

Nikk writes all prose, code choices, and slide ordering.

## Out of scope

- Rewriting the Instruqt assignments.
- A conference-ready standalone version of the talk.
- Custom theming. Reuse `theme-temporal` as-is.

## Next step

`writing-plans` → an implementation plan Nikk executes segment by segment, with
Claude teaching the concept and the mechanics before each one and reviewing after.
