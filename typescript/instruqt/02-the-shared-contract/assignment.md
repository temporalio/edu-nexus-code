---
slug: the-shared-contract
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
- title: Exercise
  type: service
  hostname: workshop
  path: /?folder=/root/workshop/exercise/src
  port: 8080
- title: Temporal UI
  type: service
  hostname: workshop
  path: /
  port: 8233
- title: Terminal
  type: terminal
  hostname: workshop
  workdir: /root/workshop/exercise
- title: Payments Worker
  type: terminal
  hostname: workshop
  workdir: /root/workshop/exercise
- title: Compliance Worker
  type: terminal
  hostname: workshop
  workdir: /root/workshop/exercise
- title: Solution
  type: service
  hostname: workshop
  path: /?folder=/root/workshop/solution/src
  port: 8080
- title: Monolith Architecture
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
`shared/nexus-service.ts`, and follow TODO 1.

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
src/compliance/nexus-handler.ts(25,81): error TS2345: Argument of type '{}' is not
assignable to parameter of type 'ServiceHandlerFor<{ checkCompliance: ...;
submitReview: ...; }>'.
  Type '{}' is missing the following properties from type
  'ServiceHandlerFor<...>': checkCompliance, submitReview
```

You declared two Operations, and TypeScript immediately went looking for the two handlers
that answer them. It cannot find either, so it refuses to compile.

That is the contract doing its job, and it is worth sitting with for a second. You have
not run anything. No Worker has started. No call has been made. The compiler already knows
Compliance owes Payments two implementations, purely from the shape of the Service.

If you have taken the Java or Kotlin version of this workshop, this is the moment that
differs most. There the Worker starts fine and fails at runtime with
`Missing handlers for service operations`. In TypeScript that class of mistake cannot
reach a running process.

You fix this in challenge 3, by writing the handlers. Leave it red.

Click **Check** when the two Operations are declared.

# What You Know Now

- `nexus.service()` names the contract; `nexus.operation<I, O>()` declares each call.
- The contract is shared. The implementation is not.
- Declaring an Operation obliges someone to handle it, and the compiler enforces that.
