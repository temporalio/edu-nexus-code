---
theme: ../
title: Replay 2026 — Tailscale + Temporal Workshop
info: |
  Minimal workshop deck demonstrating the `exercise` layout and
  configurable workshop TOC via `themeConfig.toc`.
themeConfig:
  footer: "Replay 2026 | Tailscale + Temporal"
  toc:
    - id: arch
      label: Architecture
    - id: ex1
      label: "Exercise 1: Hello Tailnet"
    - id: ex2
      label: "Exercise 2: Explore Tailscale"
    - id: agents
      label: AI Agents on Temporal
    - id: ex3
      label: "Exercise 3: Weather Agent"
    - id: ratelimit
      label: Rate Limit Demo
    - id: tsnet
      label: temporal-ts-net and Metrics Watcher
    - id: wrap
      label: Wrap-Up
layout: cover
variant: planet-teal
---

# Replay 2026

## Tailscale + Temporal

Workshop · Replay 2026

---
layout: toc
current: arch
---

---
layout: section
---

# Architecture

---
layout: default
---

# Hello Tailnet

Brief overview of what we'll build in Exercise 1: a Temporal Worker that
talks to peers over a Tailscale tailnet.

---
layout: exercise
minutes: 15
heading: Exercise 1
---

Spin up a Tailscale auth key, register a Temporal Worker, and run a Hello
Tailnet Workflow that pings a peer node by tailnet name.

---
layout: toc
current: ex2
---

---
layout: exercise
minutes: 20
heading: Exercise 2
---

Explore the Tailscale admin console: ACLs, MagicDNS, and how subnet routers
work. Wire your Worker into a private subnet.

---
layout: section
---

# AI Agents on Temporal

---
layout: exercise
minutes: 25
heading: Exercise 3
---

Build a weather agent that calls an LLM, fetches weather data over the
tailnet, and writes the answer back through a Temporal Update.

---
layout: section
---

# Rate Limit Demo

---
layout: default
---

# `temporal-ts-net` and the Metrics Watcher

Wrap-up of the day's tooling: the `temporal-ts-net` library and the metrics
watcher we deployed for observability.

---
layout: end
---

# Thank you

Workshop materials at **github.com/temporalio/replay-2026-tailscale**
