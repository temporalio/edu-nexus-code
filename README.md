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

## Language tracks

Two hands-on lab versions of this material exist as Instruqt tracks, in addition to the
Java tutorial under `java/`:

| Language | Directory | Track slug | Live lab | Sandbox image |
|---|---|---|---|---|
| Kotlin | `kotlin/` | `nexus-kotlin-decouple-monolith` | — | `ghcr.io/nadvolod/edu-nexus-kotlin-sandbox` |
| TypeScript | `typescript/` | `nexus-typescript-decouple-monolith` | [Open](https://play.instruqt.com/temporal/invite/lxlcmj1tat0c) | `ghcr.io/nadvolod/edu-nexus-typescript-sandbox` |

The TypeScript lab is live and launchable by anyone with that invite link. It is the same
invite the deck's closing slide points at, so change both together.

The slide decks are **not** in this repo. They live in
[`temporalio/temporal-devdays-ts`](https://github.com/temporalio/temporal-devdays-ts)
under `decks/`, which is the index and deck library for TypeScript DevDays content.

## Kotlin Instruqt track

A hands-on lab version of this material, in Kotlin, lives under `kotlin/`. It runs as
an Instruqt track and is built for a live 90 minute workshop with a 40 minute lab block.

```
kotlin/
├── decouple-monolith/
│   ├── exercise/          Gradle KTS, TODOs 1 to 10 open
│   └── solution/          Gradle KTS, complete
├── sandbox/Dockerfile     Temurin 21 + Temporal CLI + pre-warmed Gradle cache
├── diagrams/              architecture diagram, served on 8090
└── instruqt/
    ├── track.yml          no challenges: block, challenges auto-discovered
    ├── config.yml         container `workshop`, 8192 MB, ports 8233/8080/8090
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

Every challenge has seven tabs in a fixed order. The `tab-N` buttons in `assignment.md`
are zero-indexed positions, not ids, so reordering tabs means remapping every button.

| Index | Tab | Type |
|-------|-----|------|
| tab-0 | Exercise | service, port 8080 (code-server) |
| tab-1 | Temporal UI | service, port 8233 |
| tab-2 | Terminal | terminal |
| tab-3 | Payments Worker | terminal |
| tab-4 | Compliance Worker | terminal |
| tab-5 | Solution | service, port 8080 (code-server) |
| tab-6 | Monolith Architecture | service, port 8090 (jwebserver) |

Exercise is first on purpose. Instruqt renders the first tab as the active one, so its
iframe has real dimensions at load. code-server cannot lay itself out in a 0x0 iframe,
which is what left the editor blank until a manual refresh. Solution is still position 5
and still boots hidden, so it may still need one refresh.

Both editor tabs share ONE code-server instance and select their directory with
`?folder=`. Instruqt loads every service tab's iframe at challenge start, including
hidden ones, and code-server cannot lay itself out in a 0x0 iframe. Running one
instance instead of two halves that exposure and matches the shape of
`temporal-ai-agents-python-v4`, the one Temporal track known to run code-server through
this proxy successfully.

### Maintenance mode is deliberately OFF

`track.yml` carries no `maintenance:` key. The track is therefore launchable by anyone
with the link rather than by owners only, which is what a live workshop needs.

Two consequences worth knowing:

- `instruqt track push` drops `maintenance: false` from the file, because the serializer
  omits false values. An absent key IS the off state, not a forgotten setting. To turn it
  back on, add `maintenance: true` and push again.
- The `create-instruqt-tutorial` guardrail hook refuses to run any command whose text
  contains `push` while the track is out of maintenance mode. That includes `git push`.
  Shipping changes from here needs a human to run the final git push by hand.

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

CI pushes only the **track** on merge to `main`
(`.github/workflows/build-and-push-kotlin.yml`). It deliberately does not build the
image: the image lives in a personal GHCR namespace that this repo's `GITHUB_TOKEN`
cannot write to, so the build stays a manual step and the digest above is what ships.

**That push is currently a no-op.** This repo has no `INSTRUQT_TOKEN` secret, so the
push job warns and skips rather than failing the run. Until someone adds one (Instruqt
-> Team Settings -> API keys, then a repo secret named `INSTRUQT_TOKEN`), publishing is
manual: `cd kotlin/instruqt && instruqt track push`. Nothing else has to change when the
secret lands.

The caller grants `packages: write` even though it never builds an image. A caller's
`permissions:` block is the ceiling for every job in the called workflow, and GitHub
checks that ceiling when it loads the workflow, before any `if:` runs. A read-only
ceiling therefore fails the entire run at startup with zero jobs and no readable log,
even though `build-image` is disabled.

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

## TypeScript Instruqt track

The same material in TypeScript, under `typescript/`, on the Temporal TypeScript SDK.
Same five challenges, same narrative, same 90 minute shape.

```
typescript/
├── decouple-monolith/
│   ├── exercise/          npm + ts-node, TODOs 1 to 7 open
│   └── solution/          complete
├── sandbox/
│   ├── Dockerfile         node:22-bookworm + Temporal CLI + code-server
│   └── serve-diagrams.mjs static server for the diagram tab
├── diagrams/              architecture diagram, served on 8090
├── .dockerignore          keeps node_modules out of the build context
└── instruqt/
    ├── track.yml, config.yml, track_scripts/
    └── 01-run-the-monolith/ ... 05-durability-and-human-review/
```

**Nexus is GA in the TypeScript SDK as of v1.23.0**, the version this workshop pins —
specifically for calling Operations from Workflows and for Workflow-backed Operation
handlers, which is exactly what the lab builds (SDK release notes, PR #2299).

Two things are easy to get wrong here, so they are stated in the deck and in
`shared/nexus-service.ts` rather than left to be discovered:

- `nexus-rpc` is a **separately versioned** package, still at `0.0.3`, and it does tag
  `service()`, `operation()` and `serviceHandler()` `@experimental`. An attendee hovering
  `nexus.service` will see that and reasonably conclude Nexus is experimental. It is not;
  the contract package is.
- **Standalone Nexus Operations** — running an Operation with no caller Workflow, via
  `client.nexus.createServiceClient()` — *are* still Pre-release and need a special CLI
  build. This workshop does not use them.

### Three lessons that do not survive the port

These are real design differences between the SDKs, not translation choices. Recorded
here for whoever maintains the port. **The workshop itself is standalone and never
mentions another language** — the assignments teach each of these on its own terms, so do
not reintroduce a comparison into attendee-facing material.

- **Challenge 2 ends with a deliberately FAILING typecheck.** `ServiceHandlerFor<Ops>` is
  a non-optional mapped type, so declaring an Operation obliges a matching handler at
  compile time. Kotlin fails at Worker startup with `Missing handlers for service
  operations`; TypeScript refuses to compile:

  ```
  TS2345: Type '{}' is missing the following properties from type
  'ServiceHandlerFor<...>': checkCompliance, submitReview
  ```

  So `02/solve-workshop` deliberately does not typecheck. The build is red until
  challenge 3 fills the handlers in, and that is the lesson, not a defect.

- **Challenge 3's proof of registration inverts.** There is no `Nexus Poller` line to
  look for. A Worker *without* `nexusServices` logs `No Nexus services registered, not
  polling for Nexus tasks`; a correctly wired one logs nothing. Absence is the signal, so
  the assignment tells learners to go looking for it deliberately.

- **The Endpoint moves to the call site.** Java and Kotlin configure it on the Worker via
  `NexusServiceOptions`, which is what makes the Kotlin lesson "notice you do NOT write
  the Endpoint name here." In TypeScript, `wf.createNexusServiceClient({ service,
  endpoint })` takes it inside the Workflow. The decoupling argument still holds one
  level in — the Workflow knows a name, the Registry knows the address — and
  `COMPLIANCE_ENDPOINT` is a single constant in `shared/types.ts`.

### Challenges

Five TODOs, numbered 1 to 5, plus one worked example.

`checkCompliance` in `compliance/nexus-handler.ts` is **given, not a TODO**. It is the
harder of the two Nexus Operations and the one whose shape carries the lesson, so a
learner reads it as a worked example and then writes `submitReview`, which is the
opposite shape on the same Service. That contrast is what challenge 3 is for, and it
lands better from a working example than from two blank functions.

| # | Slug | Format | Files touched | TODOs |
|---|------|--------|---------------|-------|
| 01 | run-the-monolith | observe | none | — |
| 02 | the-shared-contract | code | `shared/nexus-service.ts` | 1 |
| 03 | the-compliance-side | code | `compliance/nexus-handler.ts`, `compliance/worker.ts`, plus the Endpoint CLI | 2, 3 (+ worked example) |

TODO 4 is lettered rather than split into separate numbers, because its three parts are
one idea: put the Nexus boundary in. 4a deletes the Activity proxy, 4b replaces the call
that used it, 4c writes the review caller. Each comment sits on the code it changes.

4b and 4c build the same `createNexusServiceClient` with different numbers — `'10 minutes'`
for `checkCompliance`, `'10 seconds'` for `submitReview` — and that pair is the lesson:
async Operations budget for the whole call including retries, sync Operations must answer
inside the handler deadline.
| 04 | the-payments-side | code | `payments/workflows.ts`, `payments/worker.ts` | 4a, 4b, 4c, 5 |
| 05 | durability-and-human-review | observe | none | — |

### Tabs

Six, not seven. There is **one** editor tab, rooted at `/root/workshop` so both project
folders are in the same file tree:

| Index | Tab | Type |
|-------|-----|------|
| tab-0 | Exercise | service, port 8080 (code-server, shows `exercise/` and `solution/`) |
| tab-1 | Temporal UI | service, port 8233 |
| tab-2 | Terminal | terminal |
| tab-3 | Payments Worker | terminal |
| tab-4 | Compliance Worker | terminal |
| tab-5 | Monolith Architecture | service, port 8090 (`node /opt/serve-diagrams.mjs`) |

A separate Solution tab was removed on purpose. Instruqt loads every service tab's iframe
at challenge start including hidden ones, and code-server cannot lay itself out in a 0x0
iframe — which is why the Kotlin track's Solution tab "may still need one refresh". One
editor instance with one visible iframe removes that failure mode rather than documenting
it.

The cost is that source paths are now two levels deeper, and `solution/` is visible while
a learner works. Both are handled by writing paths in full: assignments say
`exercise/src/payments/workflows.ts`, never `payments/workflows.ts`, so nobody edits the
solution copy by accident. `node_modules` and lockfiles are hidden via `files.exclude`.

The `tab-N`-is-a-position rule still applies: these are zero-indexed positions, not ids,
so reordering means remapping every button.

### What the sandbox image bakes in

`node:22-bookworm`, the Temporal CLI, code-server, both trees at `/opt/workshop` with
`node_modules` installed, and the diagram. Both trees are typechecked at build time, so
a broken port fails the image build rather than the workshop.

Three deliberate differences from the Kotlin image:

- **`memory: 4096`, not 8192.** That budget existed for two JVMs and a Gradle daemon.
- **No language extension is installed.** The Kotlin image pinned a syntax-only extension
  to keep a language server from competing with two JVMs. Here the built-in TypeScript
  service is wanted: it renders the challenge-2 compile error as a live red squiggle.
- **Track setup symlinks `node_modules` instead of copying it.** It is 292 MB per tree,
  so copying both would move ~600 MB before the attendee can type. Consequence:
  `npm install` inside the lab cannot write to `node_modules`. The workshop never needs
  it.

`.dockerignore` is load-bearing, not tidiness. The build context is `typescript/` and the
Dockerfile copies `decouple-monolith/`. Without the ignore the build ships ~600 MB of
host-built `node_modules` that are also the wrong architecture — the host is arm64, the
image amd64, and `@temporalio` ships a native core bridge. Context drops from 584 MB to
0.3 MB.

### Publishing

```bash
cd typescript/instruqt && instruqt track validate
```

Build for `linux/amd64`. An arm64 image fails to start on Instruqt with an empty log:

```bash
cd typescript
docker buildx build --platform linux/amd64 \
  -f sandbox/Dockerfile -t ghcr.io/nadvolod/edu-nexus-typescript-sandbox:latest .
docker buildx imagetools inspect ghcr.io/nadvolod/edu-nexus-typescript-sandbox:latest \
  --format "{{.Manifest.Digest}}"
```

Same rule as Kotlin: **the image is pinned by digest, so any change under
`typescript/decouple-monolith/` requires a rebuild AND a re-pin.** Only changes under
`typescript/instruqt/` ship without one.

**The GHCR package must be public.** A newly published package is private by default and
Instruqt pulls anonymously, which surfaces as `Could not find the image` with nothing
else to go on. Check it without credentials:

```bash
tok=$(curl -s "https://ghcr.io/token?scope=repository:nadvolod/edu-nexus-typescript-sandbox:pull&service=ghcr.io" | jq -r .token)
curl -s -o /dev/null -w '%{http_code}\n' -H "Authorization: Bearer $tok" \
  https://ghcr.io/v2/nadvolod/edu-nexus-typescript-sandbox/manifests/latest
```

200 is public, 403 is private. GitHub exposes no REST endpoint for package visibility, so
flipping it is a web-UI action.

### TypeScript-specific gotchas

- **`proxyActivities` silently types away synchronous Activities.** An Activity that is
  not `async` becomes `NotAnActivityMethod`, and the failure appears at the *call site* in
  the Workflow as `Type 'Symbol' has no call signatures` — which is verbatim the error the
  SDK's own JSDoc quotes. Every Activity here is `async` for that reason.
- **A `setHandler` validator must take the same arguments as its handler.** A zero-arg
  validator makes TypeScript infer `Args = []` and select the wrong `setHandler` overload,
  producing an error that points at the definition rather than at the validator.
- **Workflow type names are camelCase in visibility queries.** Challenge 04's check queries
  `WorkflowType="paymentProcessingWorkflow"`, the exported function name, not a class name.
- **Running the Kotlin and TypeScript workshops against one dev server does not work.**
  Both poll `compliance-risk` in `compliance-namespace`. A stray Kotlin Worker will pick
  up a TypeScript Workflow task and fail it with `Unknown workflow type
  "complianceWorkflow". Known types are [ComplianceWorkflow]`, costing a Workflow-task
  retry. Harmless in a sandbox, confusing locally.
- **`track.yml` and every `assignment.md` frontmatter are rewritten on publish**, exactly
  as on the Kotlin track. Comments are stripped and keys reordered. Keep durable
  explanation here.
