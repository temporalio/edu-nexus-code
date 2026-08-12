# Decoupling Temporal Services with Nexus Code

This repository contains the code that goes along with our [`Decoupling Temporal Services with Nexus`](https://learn.temporal.io/tutorials/nexus/nexus-sync-tutorial/) tutorial. Please reference that tutorial to see how to use this repository.

See the [Nexus documentation](https://docs.temporal.io/nexus) to explore more.

## What You'll Learn

- Register a Nexus Endpoint using the Temporal CLI
- Define a shared Nexus Service contract between teams with `@Service` and `@Operation`
- Implement a synchronous Nexus handler with `@ServiceImpl` and `@OperationImpl`
- Swap a local Activity call for a durable cross-team Nexus call
- Inspect Nexus operations in the Web UI Event History

## Hands-On Exercises

| Directory                                                | Directory Path                     |
| :--------------------------------------------------------| :----------------------------------|
| [Exercise](java/decouple-monolith/exercise)              |  `java/decouple-monolith/exercise` |
| [Solution to Exercise](java/decouple-monolith/solution)  |  `java/decouple-monlith/solution`  |

## Codespaces

You can launch an exercise environment for this tutorial using GitHub Codespaces by following [this](codespaces.md) walkthrough.

## Kotlin Instruqt track

A hands-on lab version of this material, in Kotlin, lives under `kotlin/`. It runs as
an Instruqt track and is built for a live 90 minute workshop with a 40 minute lab block.

```
kotlin/
├── decouple-monolith/
│   ├── exercise/          Gradle KTS, five TODOs open
│   └── solution/          Gradle KTS, complete
├── sandbox/Dockerfile     Temurin 21 + Temporal CLI + pre-warmed Gradle cache
└── instruqt/
    ├── track.yml          no challenges: block, challenges auto-discovered
    ├── config.yml         container `workshop`, 4096 MB
    ├── track_scripts/     setup-workshop, cleanup-workshop
    ├── 01-run-the-monolith/
    ├── 02-the-shared-contract/
    ├── 03-the-compliance-side/
    ├── 04-the-payments-side/
    └── 05-durability-and-human-review/
```

The Java tree under `java/` is unchanged and is not used by the track.

### What the sandbox image bakes in

Eclipse Temurin 21, the Temporal CLI, both Kotlin trees at `/opt/workshop`, and a
warmed Gradle cache. Both trees are compiled at build time, so a broken port fails the
image build rather than the workshop. Lab start stages `/opt/workshop` to
`/root/workshop`, starts the dev server, and creates `payments-namespace` and
`compliance-namespace`.

The Nexus Endpoint is deliberately not created at setup. Attendees create it themselves
in challenge 3.

### Challenges

| # | Slug | Format | Files touched |
|---|------|--------|---------------|
| 01 | run-the-monolith | observe | none |
| 02 | the-shared-contract | code | `ComplianceNexusService.kt` |
| 03 | the-compliance-side | code | `ComplianceNexusServiceImpl.kt`, `ComplianceWorkerApp.kt`, plus the Endpoint CLI |
| 04 | the-payments-side | code | `PaymentProcessingWorkflowImpl.kt`, `PaymentsWorkerApp.kt` |
| 05 | durability-and-human-review | observe | none |

### Why only one challenge has a real check

Four of the five `check-workshop` scripts are `exit 0`. This is a live instructor-led
workshop, so the instructor is the feedback loop and progression is not gated on a
script. Two concrete reasons:

- Skip runs `solve-workshop`, which copies solution files over the learner's work.
  Gating progression pushes a stuck learner toward destroying their own code to move on.
- Check-click latency costs real minutes across a room of 20 in a 40 minute lab block.

Challenge 04 keeps a check because its failure mode is silent. A mistyped Endpoint name
does not fail the Workflow, it hangs. That check is a diagnostic, not a gate: it fails
only when it finds a Workflow stuck retrying its Workflow task, and passes otherwise.
Both directions are tested. It passes on a healthy run with TXN-B parked for review, and
fires with the endpoint-not-found message when the Endpoint is missing.

Solve scripts stay on every challenge. They power Skip and make `instruqt track test` an
end-to-end integration test.

If this track is ever reused self-paced, with no instructor watching, put the checks
back. The git history has them.

Every challenge has six tabs in a fixed order. The `tab-N` buttons in `assignment.md`
are zero-indexed positions, not ids, so reordering tabs means remapping every button.

| Index | Tab | Type |
|-------|-----|------|
| tab-0 | Temporal UI | service, port 8233 |
| tab-1 | Exercise | code |
| tab-2 | Terminal | terminal |
| tab-3 | Payments Worker | terminal |
| tab-4 | Compliance Worker | terminal |
| tab-5 | Solution | code |

### CLI workflow

```bash
# Validate before any push.
cd kotlin/instruqt && instruqt track validate

# First push only: register the slug server-side, then reconcile.
instruqt track create nexus-kotlin-decouple-monolith --title "Decouple a Monolith with Temporal Nexus (Kotlin)"
instruqt track push --force
instruqt track pull            # populates track id and every tab id
git add kotlin/instruqt/ && git commit -m "Pin Instruqt track and tab ids"

# Simulate a learner end to end via the solve scripts.
instruqt track test
```

Build the sandbox image for `linux/amd64`. An arm64 image (the Apple Silicon default)
fails to start on Instruqt with an empty log:

```bash
cd kotlin
docker buildx build --platform linux/amd64 \
  -f sandbox/Dockerfile -t ghcr.io/nadvolod/edu-nexus-kotlin-sandbox:latest --push .
```

**The image is pinned by digest, so any change under `kotlin/decouple-monolith/`
requires a rebuild AND a re-pin.** Pushing the track alone will not ship it: the sandbox
keeps booting the old digest and attendees get stale exercise code. Only changes under
`kotlin/instruqt/` are push-only. Re-pin with:

```bash
docker buildx imagetools inspect ghcr.io/nadvolod/edu-nexus-kotlin-sandbox:latest --format "{{.Manifest.Digest}}"
```

CI does both jobs on merge to `main` via `.github/workflows/build-and-push-kotlin.yml`.

### Known issues and gotchas

- **A wrong Nexus Endpoint name hangs rather than fails.** The server rejects the
  command with `BadScheduleNexusOperationAttributes: endpoint "..." not found` at
  command-report time, outside Workflow code, so no try/catch can see it. The Workflow
  sits `Running` with repeated `WORKFLOW_TASK_FAILED`. Challenge 04's check detects the
  retry loop instead of waiting for a completion that never arrives.
- **Do not check `temporal task-queue describe --task-queue-type nexus` for a poller.**
  The server keeps listing a poller for minutes after the Worker process exits, so any
  check built on it passes on a stale entry from a previous attempt. If you ever add a
  check that has to prove a Nexus handler is registered, boot a Worker and grep its own
  log for the `Nexus Poller` startup line instead. Challenge 03's assignment teaches
  learners to read that same line by eye.
- **`PaymentGateway.executePayment` fails 10% of the time on purpose.** Check scripts
  assert on final Workflow state, never on clean first-attempt output.
- **The review Update is time sensitive.** `submitReview` is a sync Nexus Operation with
  a 10 second budget and the handler Workflow has to be running first. Challenge 05
  polls for `compliance-TXN-B` before reviewing, and retries.
- **Kotlin diverges from `java/` in two deliberate places.** `PaymentStarter` starts the
  three Workflows without blocking, so TXN-B parking for review cannot hold up TXN-C.
  The Workers print their startup banner with `println`, because the Java exercise calls
  `Workflow.getLogger` from `main`, which throws off a Workflow thread.
- **`track.yml` and every `assignment.md` frontmatter are rewritten on push.** Comments
  are stripped and keys reordered. Keep durable explanation here, not in those files.
  `config.yml` is left alone.