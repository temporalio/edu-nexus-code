# Decoupling Temporal Services with Nexus Code

This repository contains the code that goes along with our [`Decoupling Temporal Services with Nexus`](https://learn.temporal.io/tutorials/nexus/nexus-sync-tutorial/) tutorial. Please reference that tutorial to see how to use this repository.

See the [Nexus documentation](https://docs.temporal.io/nexus) to explore more.

## What you'll learn

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