# Instruqt: things that cost us hours

Hard-won findings from building the Kotlin Nexus track. Most of these produce a
symptom that looks like something else, which is why they were expensive. Written
symptom-first so you can find your problem quickly.

Everything here was verified against a real sandbox, not inferred from docs.

---

## Tabs

### A service tab shows "Please wait" forever

**Cause:** the port is not listed under `ports:` in `config.yml`. Instruqt's proxy
cannot reach an undeclared port and returns HTTP **572** (target host connection
failed).

```yaml
containers:
- name: workshop
  ports:
  - 8233   # Temporal UI
  - 8080   # code-server
  - 8090   # architecture diagram
```

**Every port any tab points at must be here.** This is not in the Instruqt docs
for container sandboxes; it is a comment in `temporal-ai-agents-python-v4`.

### An embedded app connects but renders blank until you refresh

**Cause:** Instruqt loads *every* service tab's iframe when the challenge starts,
including tabs that are not visible. A hidden tab's iframe is 0x0, and apps that
compute layout at boot cannot start in it. The browser console says so:

```
ERR Unable to figure out browser width and height
The Web Worker Extension Host did not start in 60s
```

**Fix: make that tab the FIRST tab.** Instruqt renders the first tab in the
`tabs:` list as the active one, so its iframe has real dimensions at load. This
fixed code-server for us. Confirmed working.

Only one tab can be first, so a second embedded app (our Solution editor) may
still need a refresh. Accept it, or serve both views from one instance.

### Use ONE editor tab, not an Exercise tab and a Solution tab

This is the fix for the 0x0 iframe problem above rather than a workaround for it.

Two editor tabs means two code-server iframes, and Instruqt boots every service tab's
iframe at challenge start including the hidden ones. Only the first tab has real
dimensions, so the second reliably needs a manual refresh. Making the Solution tab
position 5 does not help; it just moves which one is broken.

Point a single tab at the directory that contains BOTH trees:

```yaml
- title: Exercise
  type: service
  path: /?folder=/root/workshop      # holds exercise/ and solution/
  port: 8080
```

One iframe, nothing hidden, no refresh. It also drops the tab count from seven to six.

Three things move with it, and two fail silently:

- **`tab-N` is a position.** Removing a tab renumbers every tab after it. Re-verify
  every `[button label="..."](tab-N)` by label, not by arithmetic.
- **File paths in assignments become ambiguous.** With `solution/` visible, "open
  `payments/workflows.ts`" matches two files, and a learner editing the solution copy
  watches their changes do nothing. Write paths in full:
  `exercise/src/payments/workflows.ts`.
- **Any readiness probe that polls `?folder=` must use the new path.** Ours still
  pointed at the old folder, so setup would have reported the editor not ready forever.

The cost is that source sits two levels deeper in the tree. Worth it; the refresh bug
was hit by every attendee, the extra clicks are hit once.

### Feedback is lab chrome, not a tab you add

Do not add a Feedback entry to every challenge's `tabs:` list. It renumbers every tab
after it and breaks every `[button](tab-N)` in the track, for a tab Instruqt already
provides.

Turn it on in `track.yml` instead:

```yaml
lab_config:
  feedback_tab_enabled: true    # Feedback tab in the lab UI
  feedback_recap_enabled: true  # prompt at the end of the track
```

Verified against `temporal-community/ai-agents-workshop-v4`, which sets both and has no
Feedback entry in any challenge's `tabs:`. Because it is chrome, `tab-N` indices are
untouched.

There is no `tab-N` to link to, so a per-challenge call to action references it by name:
"the **Feedback** tab".

### Reordering tabs breaks every button, silently

`tab-N` is a **zero-indexed position, not an id**. Move a tab and every
`[button label="..."](tab-N)` that pointed past it is now wrong. Nothing errors;
learners just land somewhere unexpected.

Verify by **label**, not arithmetic:

```python
titles = re.findall(r'^\s*-?\s*title: (.+)$', tabs_block, re.M)
for label, idx in re.findall(r'\[button label="([^"]+)"[^\]]*\]\(tab-(\d)\)', src):
    assert titles[int(idx)].strip() == label.strip()
```

**Appending a tab at the end is free** — existing indices do not move.

### `validate-track.sh` stops checking tab references after the first push

Its counter is `awk '... /^- title:/{n++}'`. Freshly authored tabs start with
`- title:`, but once a push assigns ids they start with `- id:`, so the count hits
0 and the check is skipped entirely. It will also *false-positive* if you add one
new tab to an already-pushed track: it sees 1 tab and calls every other reference
out of range.

Run your own label check. Do not trust a clean `validate-track.sh` to mean the
buttons are right.

---

## Images

### The sandbox boots stale code and you cannot see why

Two opposite failure modes, and it is easy to swap one for the other:

- `:latest` — a cached older image can boot silently.
- `@sha256:...` — the pinned image boots **reliably**, including when it is
  months out of date relative to your source.

We pinned by digest, then edited exercise source six more times and pushed only
the track. Attendees read TODO text from before all of it.

**Rule: any change to content baked into the image requires a rebuild AND a
re-pin.** Only track-definition changes are push-only.

```bash
docker buildx build --platform linux/amd64 -f sandbox/Dockerfile \
  -t ghcr.io/<org>/<image>:latest --push .
docker buildx imagetools inspect ghcr.io/<org>/<image>:latest --format "{{.Manifest.Digest}}"
# paste that digest into config.yml
```

Always diff the new digest against the old one. If it did not change, nothing was
pushed and you are about to ship the old image again.

If the image lives in a **personal** GHCR namespace, an org repo's `GITHUB_TOKEN`
cannot write to it. Do not leave a build job in CI that will fail on every merge:
either move the image to the org, add a PAT, or turn the build off and keep the
manual rebuild in the README. Ours pushes the track only.

### Never pipe `docker build` into anything

```bash
docker buildx build ... | tail -5     # exit code is tail's, always 0
```

A failed build reported success, and the follow-up `imagetools inspect` returned
the *previous* `:latest`, so the "re-pin" was a no-op. Two silent failures in a
row. Use `set -o pipefail`, or redirect to a file and check `$?`.

### Build for `linux/amd64`

An arm64 image (the Apple Silicon default) fails to start with **no logs at all**.

### GHCR packages are created private

Instruqt pulls anonymously, so a private package fails with "Could not find the
image" even though the push succeeded. Flip it to Public in the package settings.
There is no REST endpoint for this; it is a UI action.

---

## code-server (VS Code in the browser)

The native `type: code` tab is described by Instruqt only as "a simple code
editor" with no documented language support. For a language-specific workshop,
code-server is worth the setup. What it needs:

**Port 8080, not 8443.** 8443 is the conventional alternate-HTTPS port and the
proxy could not reach code-server there.

**Workspace Trust off**, seeded into the image. On by default, and an untrusted
folder blocks the workbench from rendering:

```dockerfile
RUN mkdir -p /root/.cs/User && printf '%s\n' \
      '{ "security.workspace.trust.enabled": false }' > /root/.cs/User/settings.json
```

**One instance, `?folder=` per tab.** Point several tabs at one server and let
each select its directory. Fewer hidden iframes booting at once, and it matches
the track that works.

```yaml
- title: Exercise
  type: service
  hostname: workshop
  path: /?folder=/root/workshop/exercise/src/main/kotlin
  port: 8080
```

If you do run multiple instances, give each its own `--user-data-dir`. They
persist the last-opened workspace there, so a shared dir makes the second
instance reopen the first one's folder. Ours served exercise code on the Solution
tab.

**Readiness: do not check `/`.**

```bash
curl -sf http://127.0.0.1:8080/     # WRONG: "/" 302s and -sf treats 3xx as success
```

That passes the instant the listener binds, long before the workbench is up.
`/healthz` is not the answer either: code-server 4.96.4 returns `expired` or
`alive`, never `ready`, and `alive` only means a request arrived recently, which
your own polling causes.

Check the URL the tab actually opens, plus the log:

```bash
[ "$(curl -s -o /dev/null -w '%{http_code}' "http://127.0.0.1:8080/?folder=$F")" = "200" ] \
  && grep -q "Extension host agent started" /tmp/code-server-8080.log
```

---

## Serving static content

`jwebserver` ships with the JDK (18+), so a Temurin-based image needs no extra
package. Our sandbox has no Python.

```bash
nohup jwebserver -b 0.0.0.0 -p 8090 -d /opt/diagrams > /tmp/jwebserver.log 2>&1 &
```

Diagrams and other assets must live **outside** challenge folders. Instruqt parses
every file inside a challenge directory as a lifecycle script.

---

## Lifecycle scripts

### Start things that do not depend on each other, independently

Our setup started code-server *after* a Temporal health check that could
`exit 1`. A slow dev server took the editors down with it. Start independent
services first, and prefer warning over exiting: killing the script abandons the
rest of setup for no benefit.

### Solve scripts destroy the exercise, permanently

The usual solve is `rm -rf exercise/src && cp -R solution/src exercise/src`.
Nothing puts it back, so one Skip leaves the sandbox solved for its whole life:
earlier challenges show later output and every TODO is pre-answered.

Restore in the first challenge's setup, but only when the tree was overwritten
wholesale, so you do not clobber real work:

```bash
if [ -f "$CONTRACT" ] && ! grep -q "TODO" "$CONTRACT"; then
    rm -rf /root/workshop/exercise/src
    cp -R /opt/workshop/exercise/src /root/workshop/exercise/src
fi
```

### Better: have the solve script run `solution/`, not overwrite `exercise/`

The restore guard above patches a self-inflicted wound. The wound is avoidable.

A solve script exists to make Skip work and to let `instruqt track test` prove the
track end to end. Neither needs the learner's tree. Point the script at the
solution instead:

```bash
set -euxo pipefail
cd /root/workshop/<project>/solution
nohup npm run c1:worker > /tmp/solve-c1-worker.log 2>&1 &
sleep 15
timeout 120 npm run c1:client -- "..."
```

Skip then cannot destroy anyone's work, no restore guard is needed, and the
sandbox never ends up permanently solved. It also removes the reason to keep a
read-only `/opt/workshop` and stage a writable copy at track setup — the image can
bake straight to `/root/workshop`.

The one rule this creates: **do not add an overwriting solve script to a track
built this way** without also adding a pristine copy to restore from. The two
designs are each coherent and must not be mixed.

Used by the TypeScript AI Agents track in
`temporal-community/ai-agents-workshop-v4`. The Kotlin and TypeScript Nexus tracks
here still use the overwrite-plus-guard shape.

### A solve script must fail loudly

`set -euxo pipefail`, and no trailing `exit 0`. A lenient solve script breaks two
things at once, and it hides both.

The TypeScript AI Agents track shipped all four with `set -uo pipefail` (no `-e`),
`cd ... || exit 0` on the way in, and `exit 0` on the way out. Every failure inside
was discarded.

**It makes `instruqt track test` worthless for the only thing it can prove.** The
run reported `Running solve OK` for all four challenges. That was compatible with
every agent call failing, because the script's exit code never depended on them.
The genuinely verified steps in that run were the ones that fail the harness
directly - image boot, Terraform, the LiteLLM mint, per-challenge setup and
cleanup.

**It makes Skip actively harmful.** Skip runs the solve script. A learner clicks
it, the script fails internally, Instruqt sees `exit 0` and advances them, and
they land in the next challenge with an environment that is not in the state it
expects and nothing on screen saying so.

Keep `|| true` on `pkill` - "no such process" is not an error - and nowhere else.

One gotcha comes with strictness, and it bites immediately. **`yes y | cmd` under
`pipefail` is a guaranteed status 141.** `yes` never stops on its own, so it always
outlives its consumer and dies of SIGPIPE, and pipefail promotes that to a script
failure even when `cmd` exited 0. Feed the prompt by redirect instead, which keeps
`yes` out of the foreground pipeline:

```bash
timeout 180 npm run c2:client -- "..." < <(yes y)
```

This cost a full test run to find. Worth reading the exit code before assuming the
challenge is broken: with `pipefail` the status is the LAST command to exit
non-zero, so a rightmost client that genuinely failed reports its own code. A bare
141 from a `yes` pipeline means the opposite of a failure - the client succeeded.

The trade-off is real and worth stating: strict mode means a transient failure
makes Skip fail visibly instead of advancing someone quietly into a broken
sandbox. That is the right side to err on. A visible failure gets retried; a
silent one gets discovered mid-challenge, in front of a room.

### `pkill -f "a|b"` does not work on macOS

Alternation is unsupported there. One `pkill` per pattern, or scripts authored on
a Mac ship broken.

---

## Checks

For a **live instructor-led** workshop, gating progression is usually wrong. Skip
runs the solve script, which destroys the learner's work, so a stuck learner's
only escape costs them their code. `edu-standalone-activities` ships `exit 0` on
every challenge for this reason.

Keep a check only where the failure is **silent**. Ours catches a mistyped Nexus
Endpoint, which does not fail the Workflow: the server rejects the command at
report time and the Workflow task retries forever. Without the check that is a
hang with no error anywhere the learner can see.

When you do write one, assert on final state, never on clean first-attempt
output, if anything in the system retries by design.

### `instruqt track test` needs `--skip-fail-check` on an instructor-led track

The command assumes a shape that instant-pass checks do not have. Per challenge it
sets up, **runs the check expecting it to fail** because nothing is solved yet, runs
solve, then runs the check expecting a pass. An `exit 0` check passes that second
step, so the run stops at challenge 1 with:

```
Running check, expecting failure  FAIL
[ERROR] Error verifying check: Check completed successfully, but we expect a fail here.
```

That is the tool working correctly against a track it was not designed for, not a
defect in the track. Pass `--skip-fail-check` and the rest of the protocol - setup,
solve, check-for-pass - runs normally.

Worth knowing because the failure reads like a broken track and invites a hunt for
a problem that is not there. Both tracks in this repo and the TypeScript AI Agents
track went unverified end to end for weeks over this one flag, while the blocker
was recorded as needing credentials it already had.

---

## Push behaviour

`instruqt track push` rewrites `track.yml` and every `assignment.md` **frontmatter**:
comments stripped, keys reordered, `checksum` and `enhanced_loading` injected.
Commit the result as-is; the next push re-mangles a prettified version. Keep
durable explanation in a README, not in those files. `config.yml` is left alone,
which is why the digest pin and its comment survive.

Commit the ids the first push assigns (track id, challenge ids, every tab id).
Pushing from a checkout without them creates a second track.

### A push workflow that fails at startup, with zero jobs and no log

Symptom: the run is `startup_failure`, `gh run view` reports `"jobs": []`, and there
is no log to open anywhere.

Cause: a caller's `permissions:` block is the **ceiling** for every job in the called
reusable workflow, and GitHub validates that ceiling when it loads the workflow,
before any `if:` is evaluated. A disabled job still counts. Granting the caller
`packages: read` while the (disabled) image-build job asks for `packages: write`
kills the whole run before anything can log why.

Grant the ceiling the union of what every job in the called workflow asks for, even
the jobs you have switched off, and say so in a comment. The next person will read
`packages: write` next to `build_image: false` and try to "fix" it.

### Do not let a missing token fail the run

`instruqt track push` needs an `INSTRUQT_TOKEN` repo secret. If the repo does not
have one, every merge goes red and people stop reading CI. Check the token in a step
(the `secrets` context is not available in a job-level `if`), emit a
`::warning title=...::` naming the fix, and skip the push. It starts working by
itself when someone adds the secret.

---

## Assignment wording

### Reading the Event History belongs in every step, not once at the end

Console output is the easy evidence and it is the weaker evidence. A workshop that
proves its point with `console.log` has taught the learner to trust the program's
own account of itself. The Event History is the durable record, it is what they
will actually have in production, and reading it is the skill worth building.

The Saga workshop originally checked the Temporal UI once, in a section near the
end. Reported as: "checking console is good. Checking the Temporal UI is even more
important and must be part of the analysis phase of every relevant activity."

So: after every run that changes behaviour, send them to the UI and say what to
look for. Name the events the way the UI renders them - **Activity Task Failed**,
not `ActivityTaskFailed`.

The highest-value habit to teach is reading for the **absent** event:

> There is no `refundPaymentIfCharged` event at all - the Workflow never scheduled
> one, because it never knew it owed one.

A missing event is invisible in console output and obvious in history, which is
exactly why the UI earns its place at each step rather than one recap at the end.

It also catches conclusions a log line cannot support. In that same workshop the
idempotency fix does NOT remove the compensation from history - the Activity still
runs and still completes, it just does nothing. A learner watching only the console
sees a line disappear and concludes the call was skipped. The history shows them
otherwise, and the real lesson (idempotency is about the effect, not about skipping
the call) is only visible there.

Verify what you claim the history shows by capturing a real one, rather than
describing it from memory:

```bash
temporal workflow show --workflow-id <id> -o json | jq -r '.events[] | .eventType'
```

### The opening card cannot use vocabulary the challenge has not taught yet

Challenge 1's intro card opened with:

> **What breaks when two teams share one Worker?**
>
> A payment cannot execute until Compliance clears it. Both teams' code runs in the
> same process, on the same Task Queue, in the same Namespace.

Three Temporal terms in two sentences, and the card is the first thing a learner reads -
before the challenge that teaches any of them. Worse, the terms were doing the
explaining. "The same Namespace" is the whole point of the workshop and it lands as a
noun the reader cannot cash.

Reported as "riddled with Temporal terminology and doesn't explain the real crux."

Write the opening card for someone in their first year of the job, using words they
already own - function call, one program, one process, deploy, crash. Say what sharing
costs, as consequences:

> - A bug in Compliance's code takes Payments down with it.
> - Compliance cannot ship a fix unless Payments ships at the same time.
> - Nothing in the code marks where one team ends and the other begins.

Then introduce **one** term, and define it in the same breath:

> Temporal gives each team a **Namespace** - a walled-off space of their own to run in.
> Compliance has one. It is empty, because all of their code is running inside Payments'.

The rest of the assignment can use the vocabulary freely. The card that runs before it
cannot, because it is the only text a learner reads with no way to look anything up.

### Name UI elements the way the UI names them

The Web UI renders history event types with spaces: **Activity Task Scheduled**, not
`ActivityTaskScheduled`. A learner told to look for `NexusOperationScheduled` is
scanning for a string that is not on screen. Bold the on-screen label, and save the
camel-case identifier for when they are reading code or JSON.

### A run of `=` inside a fence desynchronises the whole page

A `bash,nocopy` block containing the Worker's startup banner:

```
=========================================================
  Compliance Worker started on: compliance-risk
=========================================================
```

rendered as a collapsible divider, then an `<h1>`, then a code block that swallowed the
next several paragraphs of prose and displayed the following ```` ```bash,nocopy ````
marker as literal text. The raw markdown was correct and `instruqt track validate`
passed.

Instruqt's renderer does not honour the fence around a leading run of `=`. The trailing
run then forms a setext heading out of the lines above it, and every fence after that
point is off by one. Nine other `nocopy` fences in the same track rendered fine, so the
`=` runs are the trigger.

**Never put a line of only `=` in an assignment**, fenced or not. If a program prints a
banner like that, show the meaningful lines and drop the separators. Grep for it before
every publish:

```bash
grep -rn '^ *==*$' */assignment.md
```

### Show the line, do not describe it

"Look at what this Worker registers" makes the learner hunt and guess. Paste the two
lines in a `kotlin,nocopy` fence and then say what they mean. Costs four lines of
assignment, removes all the guessing.

### Implementation steps belong in the TODO, not the assignment

The assignment names the file and the TODO, and says why the change matters. The TODO
comment, three lines from the cursor, says how. When both say how you have two copies to
keep in sync, and the learner reads the same recipe twice.

This does not contradict "Show the line, do not describe it" above, and the difference is
worth stating precisely:

- Code the learner is meant to **read** belongs in the assignment. Pasting it stops them
  hunting through a file for the two lines you meant.
- Code the learner is meant to **write** belongs in the TODO. That is the recipe, and the
  recipe goes next to the cursor.

What it looks like when it leaks, from challenge 4 of the TypeScript track:

```
Then delete the proxy above it and its `complianceActivities` import.
Remove `...complianceActivities` from the `activities` object, and delete its import.
```

Both were already in the TODO comment. Grep for the shape — an imperative aimed at a
named symbol:

```bash
grep -nE '^(Remove|Delete|Add|Replace|Change) `' */assignment.md
```

The assignment keeps the part the TODO cannot carry: why the change matters, and what to
notice once it works.

### One action, one lettered TODO, sited on the code it changes

A TODO that says "replace this call, then delete the declaration above, then write the
function at the bottom" makes the learner hold three locations in their head and scroll
between them. They finish one, lose the list, and re-read the whole block.

Split by action, letter them so they still read as one idea, and put each comment on the
code it acts on:

```
TODO 4a   on the proxy to delete
TODO 4b   on the call site to replace
TODO 4c   on the function to write
```

Letters rather than new numbers when the parts are one concept — here, putting the Nexus
boundary in. New numbers imply separate concepts and inflate the count.

The failure this fixes is real and was reported from a live sandbox: a note reading
"TODO 4(a) deletes this proxy" sat on the proxy while the instructions for 4(a) were
thirty lines away at the call site, so the two halves of one instruction pointed at each
other.

### A long edge label will cover your diagram

Node text and edge labels are drawn in different layers, so a label wider than the gap
between two columns lands on top of the box text with no warning. Measure: our gutter
was 70px and the label was about 300px. Keep labels to roughly the gutter width and put
the sentence in the step caption instead, where there is room for it.

---

## Architecture diagrams

A diagram tab is worth building when the point is *where code runs*, not what it
does. Ours makes one argument: two teams, one process. Everything below exists
because the first version got it wrong.

### Hard requirements

These are not style preferences. Each one was a defect we shipped and had to fix.

1. **No overlapping boxes, ever.** Never hardcode a `top` or guess a
   `min-height`. Render the blocks, measure them, then stack each one from the
   measured height of the one above plus a fixed gap. Containers size themselves
   to their measured contents. Edit a code snippet and the layout re-flows
   instead of one box growing through another.

2. **The control bar must not move as you step.** If it shifts, the user has to
   chase the button with the mouse between clicks. Three separate causes, pin all
   three: fixed bar height; narration that reserves exactly two lines whatever
   the caption length; and a fixed width on any button whose label changes
   ("Walk the flow" becomes "Restart" and drags the arrows sideways).

3. **Step-by-step navigation, not just a static picture.** Left and right arrows,
   a step counter, a phase pill and one sentence of narration. Bind the arrow
   keys too. Each step lights its blocks and edges and fades everything else, so
   the flow reads one hop at a time.

4. **No legend.** A colour-filter legend is noise. The only controls worth having
   are **Zoom −/+, Fit, and Reset view**. Colour is explained by the labelled
   regions themselves, not by a key.

5. **Distinct actors must be visually obvious.** Give each team, service or
   process its own filled, bordered, labelled region, and say what it owns. Two
   regions inside one outline is a whole argument with no prose.

6. **Every block carries the file name and the method signature.** Not a generic
   node label. The filename in the accent colour, the signature under it, and a
   short syntax-highlighted snippet below that.

7. **Syntax highlighting for the actual language.** Kotlin annotations, keywords,
   types, strings and comments each get their own colour, deliberately different
   from the diagram's edge palette so code text does not add to the visual noise.

8. **Self-contained.** One HTML file, no external requests, light and dark
   themes. It is served inside a sandbox with no internet guarantees.

### Plumbing

- Put it in `<lang>/diagrams/`, never inside a challenge folder. Instruqt parses
  every file in a challenge directory as a lifecycle script.
- Serve it with `jwebserver -b 0.0.0.0 -p 8090 -d /opt/diagrams` (ships with the
  JDK; our image has no Python).
- Declare the port in `config.yml` or the tab shows "Please wait" forever.
- **Append the tab at the end.** Inserting it anywhere else renumbers `tab-N` and
  silently breaks every button past the insertion point.
- The file is baked into the image, so editing it needs a rebuild and a re-pin.

### The prompt

Copy this, fill in the four bracketed parts, and hand it over with the source
files attached.

```text
Build a single self-contained HTML architecture diagram for [CHAPTER / TOPIC],
to be served as an Instruqt tab.

What it must show: [THE ONE ARGUMENT, e.g. "two teams sharing one process, and
the Namespace one of them is not using"].

Actors to distinguish: [LIST, e.g. "Payments team, Compliance team, and an empty
compliance-namespace"]. Give each its own filled, bordered, labelled region with
a one-line note on what it owns.

Blocks: one per file that matters. Each shows the file name, the method
signature, and a short snippet with syntax highlighting for [LANGUAGE].

Requirements, all of them non-negotiable:
- Compute the layout: render, measure, then stack with a fixed gap. Nothing may
  overlap, whatever the snippet lengths.
- Step player with left/right arrows, a step counter, a phase pill and one
  sentence of narration per step. Arrow keys bound. Each step highlights its
  blocks and edges and fades the rest.
- The control bar must never change size or position between steps. Fixed bar
  height, narration reserving exactly two lines, fixed width on any button whose
  label changes.
- Controls are Zoom -/+, Fit, Reset view. No legend.
- Clicking a block opens a detail panel: type, file path, what it does, the
  snippet, and a few bullet points.
- Light and dark themes. No external requests of any kind.
- Keep every step caption under about 130 characters so it fits two lines on a
  narrow lab pane.

Then: put it in <lang>/diagrams/, serve it with jwebserver on a new port,
declare that port in config.yml, append the tab at the END of every challenge's
tabs list, rebuild the image and re-pin the digest.
```

Working example: `kotlin/diagrams/monolith-architecture.html`.
