---
slug: the-shared-contract
id: iv77vxllxvtr
type: challenge
title: 2. The Shared Contract
teaser: Write the interface both teams depend on. Two annotations, one file.
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
- id: 0kexznfd37ia
  title: Temporal UI
  type: service
  hostname: workshop
  path: /
  port: 8233
- id: foqsemrm4t7k
  title: Exercise
  type: service
  hostname: workshop
  path: /?folder=/root/workshop/exercise/src/main/kotlin
  port: 8080
- id: nvgfbgah0mkc
  title: Terminal
  type: terminal
  hostname: workshop
  workdir: /root/workshop/exercise
- id: hfkgmprivak3
  title: Payments Worker
  type: terminal
  hostname: workshop
  workdir: /root/workshop/exercise
- id: ndy6exb6bcyc
  title: Compliance Worker
  type: terminal
  hostname: workshop
  workdir: /root/workshop/exercise
- id: l9axgqwrj1g7
  title: Solution
  type: service
  hostname: workshop
  path: /?folder=/root/workshop/solution/src/main/kotlin
  port: 8081
difficulty: basic
timelimit: 900
enhanced_loading: null
---

# One Interface, Two Teams

A Nexus Service is an interface both teams compile against. Payments builds a stub
from it. Compliance implements a handler for it. Neither team sees the other's code.

It lives in `shared/` on purpose. Neither team owns it alone.

# Write It

Click the [button label="Exercise" background="#444CE7"](tab-1) tab, open
`shared/nexus/ComplianceNexusService.kt`, and follow the TODO comments.

The editor saves as you type. There is no save button.

# One Rule That Bites

The Nexus runtime validates every method in the interface when a Worker starts.
Annotate `checkCompliance` and forget `submitReview`, and the Worker dies with:

```bash,nocopy
Missing @Operation annotation
```

You are not calling `submitReview` until challenge 5. It still needs the annotation
today.

# Compile It

Click the [button label="Terminal" background="#444CE7"](tab-2) tab:

```bash,run
./gradlew --offline compileKotlin
```

This should pass. `ComplianceNexusServiceImpl.kt` still has `TODO(...)` bodies, and
those type-check fine because `TODO()` returns `Nothing`. They throw at runtime
instead, which is challenge 3's problem.

Click **Check** when the annotations are in.

# What You Know Now

- `@Service` marks the contract, `@Operation` marks each callable method.
- The contract is shared. The implementation is not.
- Every method needs `@Operation`, including the ones you have not called yet.
