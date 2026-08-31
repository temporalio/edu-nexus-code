---
slug: the-shared-contract
id: ynazxaoijxil
type: challenge
title: 2. The Shared Contract
teaser: Write the interface both teams depend on. One file, two Operations.
notes:
- type: text
  contents: |-
    # How do two teams agree on a call neither one owns?

    Payments needs a compliance decision. Compliance owns the logic and refuses
    to hand over its database.

    An HTTP client would work until the network blips. Then you are writing
    retries, timeouts, and callback infrastructure by hand.
- type: text
  contents: |-
    # The four Nexus pieces

    Service is the contract. Operation is a method on it. Endpoint is the routing
    rule. Registry is where Endpoints live.

    You write the Service now. The other three come next.
tabs:
- id: fsl9wnyvzlt2
  title: Exercise
  type: service
  hostname: workshop
  path: /?folder=/root/workshop
  port: 8080
- id: 7iygieqqyuko
  title: Temporal UI
  type: service
  hostname: workshop
  path: /
  port: 8233
- id: nljg80a3snn1
  title: Terminal
  type: terminal
  hostname: workshop
  workdir: /root/workshop/exercise
- id: ymi443zqbrnc
  title: Payments Worker
  type: terminal
  hostname: workshop
  workdir: /root/workshop/exercise
- id: znmrv8zywaej
  title: Compliance Worker
  type: terminal
  hostname: workshop
  workdir: /root/workshop/exercise
- id: 1wdriwhm5mby
  title: Monolith Architecture
  type: service
  hostname: workshop
  path: /monolith-architecture.html
  port: 8090
difficulty: basic
timelimit: 900
enhanced_loading: null
---

# One Contract, Two Teams

A Nexus Service is a contract both teams compile against. Payments builds a client from
it. Compliance implements a handler for it. Neither team sees the other's code.

It lives in `shared/` on purpose. Neither team owns it alone.

# Write It

Click the [button label="Exercise" background="#444CE7"](tab-0) tab, open
`exercise/src/shared/nexus-service.ts`, and follow TODO 1.

Two Operations. Each is a name paired with `nexus.operation<Input, Output>()`, which
carries no implementation — only the types both teams agree on.

The editor saves as you type. There is no save button.

# Now Break the Build on Purpose

Click the [button label="Terminal" background="#444CE7"](tab-2) tab:

```bash,run
npx tsc --noEmit
```

**This is supposed to fail.** Read what it says:

```bash,nocopy
src/compliance/nexus-handler.ts(29,81): error TS2345: Argument of type
'{ checkCompliance: WorkflowRunOperationHandler<...>; }' is not assignable to
parameter of type 'ServiceHandlerFor<...>'.
  Property 'submitReview' is missing in type
  '{ checkCompliance: WorkflowRunOperationHandler<...>; }' but required in type
  'ServiceHandlerFor<...>'.
```

You declared two Operations, and TypeScript immediately went looking for the handlers
that answer them. One of them, `checkCompliance`, is already written for you. The other
is not, so it refuses to compile and names the one that is missing.

That is the contract doing its job, and it is worth sitting with for a second. You have
not run anything. No Worker has started. No call has been made. The compiler already knows
Compliance owes Payments two implementations, purely from the shape of the Service.

Notice *when* it told you: at compile time, before anything ran. A Service with an
unanswered Operation cannot reach a running Worker, because the code carrying the gap does
not build.

You fix this in challenge 3, by writing the handlers. Leave it red.

Click **Check** when the two Operations are declared.

# What You Know Now

- `nexus.service()` names the contract; `nexus.operation<I, O>()` declares each call.
- The contract is shared. The implementation is not.
- Declaring an Operation obliges someone to handle it, and the compiler enforces that.

---

**Please share your feedback so we can make better content for you.** The **Feedback**
tab takes a few seconds, and it is the only way we find out which parts of this landed.
