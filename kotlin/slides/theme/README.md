# slidev-theme-temporal

A [Slidev](https://sli.dev) theme matching the Temporal 2026 brand. Inter
typography, PPT-exact mint / purple / blue palette, planet and starfield
backgrounds, and 29 layouts mirroring the official Temporal Slidey Slides
corporate deck.

## Prerequisites

- **Node** 18.0 or newer — Slidev requires modern Node. Install via
  [nvm](https://github.com/nvm-sh/nvm) (`nvm install --lts`) or
  [Homebrew](https://brew.sh) (`brew install node`).
- **pnpm** — package manager. `npm install -g pnpm` or
  `brew install pnpm`.
- **just** (optional but recommended) — recipe runner used by this repo.
  `brew install just` or see [casey/just](https://github.com/casey/just).
  All `just` recipes have an equivalent raw `pnpm dlx` command if you
  prefer to skip it.

You don't need to install Slidev itself. The recipes use `pnpm dlx
@slidev/cli` which fetches Slidev on demand and caches it.

## Quickstart — scaffold or augment a deck

From a fresh clone of this repo, drop a Slidev deck into any directory:

```bash
git clone git@github.com:temporalio/slidev-theme-temporal.git
cd slidev-theme-temporal
just new ~/decks/my-deck
```

That drops three things into the target directory:

- `slides.md` — minimal starter deck whose `theme:` field points at this
  repo by relative path
- `justfile` — adds `slides`, `slides-build`, `slides-pdf`, `slides-clean`
  recipes (or creates a fresh justfile if the directory doesn't have one)
- `.gitignore` — adds `node_modules`, `dist`, `.slidev`, `*.pdf` entries
  (or creates one if missing)

No `package.json`, no `pnpm install`. The deck doesn't need its own
`node_modules` — Slidev runs via `pnpm dlx` and resolves the theme directly
from disk by the relative path in `slides.md`.

Then:

```bash
cd ~/decks/my-deck
just slides            # opens http://localhost:3030
```

### Dropping a deck into an existing project

`just new` is designed to be **additive**, so you can point it at a
directory that already has code in it (a service repo, a docs project,
whatever) and just add slides:

```bash
just new ~/code/my-service
```

If the target already has a `justfile`, the Slidev recipes get appended to
it (preserving your existing recipes and `default:` setting). If it has a
`.gitignore`, missing entries get added. `slides.md` is the only file
created from scratch — if one already exists at the target, the command
refuses rather than clobber it. Same protection applies if a `slides`
recipe already exists in the target's `justfile`.

### Starting from one of the example decks

Pass a starter name to seed from an example instead of the minimal shell:

```bash
just new ~/decks/my-deck reference        # full reference deck — every layout
just new ~/decks/my-deck workshop         # workshop deck w/ exercise layout + TOC
just new ~/decks/my-deck feature-launch   # product-launch flow
just new ~/decks/my-deck customer-story   # customer-success flow
```

## Install — wire the theme into an existing deck

If you already have a Slidev deck and want to add this theme:

### Option 1: Git dependency (recommended)

```bash
pnpm add github:temporalio/slidev-theme-temporal
```

Then in your `slides.md` frontmatter:

```yaml
---
theme: temporal
themeConfig:
  footer: "My Deck Title"
  toc:
    - id: intro
      label: Introduction
    - id: arch
      label: Architecture
    - id: wrap
      label: Wrap-Up
---
```

Pin to a tag or commit for reproducibility:

```bash
pnpm add github:temporalio/slidev-theme-temporal#v0.1.0
```

### Option 2: Copy files in

```bash
pnpm dlx tiged temporalio/slidev-theme-temporal theme
```

Then reference by path:

```yaml
---
theme: ./theme
---
```

## What's included

### Layouts (29)

**Title & navigation**

- `cover` — title slide with 5 hero-art variants (`variant: planet-teal` |
  `planet-purple` | `rex` | `grid` | `glow`; `teal` / `purple` are short aliases)
- `section` — full-bleed section divider
- `subsection` — section divider with `SECTION` eyebrow
- `toc` — workshop / agenda TOC, driven by `themeConfig.toc`
- `end` — closing slide

**Content**

- `default` — title + body
- `two-cols` — two-column layout
- `two-cols-header` — two-column with shared overall title + column eyebrows
- `image-right` / `image-left` — text + image columns
- `eyebrow-hero` — eyebrow + giant heading
- `code-explain` — code block alongside prose

**Product / features**

- `feature-card` — feature name + Big Picture / What You'll Get / Later +
  release-stage chip
- `release-stages` — three-stage release explainer
- `comparison` — OSS vs. Cloud two-card with feature lists
- `feature-grid` — N-column feature card grid (Security / Support / Scale)
- `eyebrow-hero` — TEMPORAL CLOUD-style hero

**People & quotes**

- `profile` — avatar + name + role + company
- `quote` — pull-quote with attribution and avatar
- `success-story` — customer story (logo + challenges + solution columns)

**Data & visuals**

- `big-stat` — huge number + caption
- `timeline` — horizontal year/month rail
- `chart` — image slot + caption
- `architecture` — image slot + caption, before/after variant
- `model-diagram` — image slot for the Temporal Model circle diagram

**Calls to action**

- `cta-banner` — single-action CTA (e.g. startup credits)
- `cta-icons` — multi-column CTA with icon-led blocks

**Workshop**

- `exercise` — workshop exercise card with timer chip
- `qa` — minimal Q&A divider
- `checklist` — "Temporal for X" tick-list grid

### Components

- `TemporalLogo` — inline Temporal plus mark
- `TemporalFooter` — persistent footer reading from `themeConfig.footer`
- `WorkshopToc` — configurable agenda/TOC reading from `themeConfig.toc`
- `ReleaseStageChip` — pill chip for release stages
- `Checkmark` — inline SVG check
- `ProfileAvatar`, `QuoteAttribution` — building blocks for `profile` / `quote`
- `TimelineRail` — horizontal year/month rail primitive
- `FeatureBlock`, `BackgroundLayer` — utility primitives

### Setup

- Custom `temporal` Shiki theme for fenced code blocks (one theme — the brand is dark)
- Mermaid palette tuned to the 2026 brand colors

## Configuration

The theme reads two top-level fields under `themeConfig` in your deck's
frontmatter:

```yaml
---
theme: temporal
themeConfig:
  footer: "Replay 2026 | My Workshop"       # left-side footer text
  toc:                                       # workshop / agenda sections
    - id: arch
      label: Architecture
    - id: ex1
      label: Exercise 1
    - id: wrap
      label: Wrap-Up
---
```

The footer's right side (page indicator) is automatic. The TOC layout reads
`themeConfig.toc` and highlights the row whose `id` matches the `current`
prop on the slide.

## Example decks

The `example/` directory ships four reference decks:

- `example/slides.md` — full reference, one slide per layout
- `example/workshop.md` — workshop deck demonstrating `exercise` + `toc`
- `example/feature-launch.md` — product feature launch
- `example/customer-story.md` — customer success story

Run any of them with:

```bash
pnpm dlx @slidev/cli example/slides.md
```

## Asset status

Several layouts ship as **skeletons** until the design team delivers final
artwork — see `spec.md` §5 for the request list. These layouts work today
and render brand-aligned placeholders:

- `architecture` (before / after Temporal) — pending SVG illustrations
- `model-diagram` (Temporal Model circles) — pending SVG
- `cta-icons` icon set — pending brand-approved Zoom / Slack / Newsletter icons
- `big-stat` ribbon graphic — pending
- `success-story` customer-logo slot spec — pending

Cover artwork is **done** — the five backgrounds in `assets/` cover every
cover variant the PPT uses.

Layouts marked with the `.asset-pending` placeholder make the gap visible at
runtime.

## Brand rule

**The Temporal logo MUST NEVER be rendered in Temporal mint green**
(`#59FDA0` / `var(--temporal-green)`). White on dark, or black on light,
only. Mint green is reserved for accent text, links, code highlights, and UI
affordances — never the logo itself. This is a non-negotiable brand rule.
Every layout that surfaces `TemporalLogo` enforces this in its scoped CSS.

## Development

This theme is verified visually — there's no test suite. The workflow is to
run one of the bundled example decks and eyeball every layout against the
2026 PowerPoint template (`Temporal Slidey Slides Optimized v2026.pptx`).

### Run the example decks

```bash
just dev                  # example/slides.md — full reference deck
just dev workshop         # example/workshop.md
just dev feature-launch   # example/feature-launch.md
just dev customer-story   # example/customer-story.md
just dev path/to/your.md  # any other path
```

Without `just` installed, the equivalent is `pnpm dlx @slidev/cli example/slides.md`.
Run `just --list` to see every recipe.

### What each example deck exercises

| File | What to look at |
|---|---|
| `example/slides.md` | **Every layout in the theme**, one slide each, in PPT order. The canonical smoke test — if anything regresses visually, you'll catch it here. |
| `example/workshop.md` | `exercise` layout + configurable `WorkshopToc`. Use this to verify workshop-shaped decks still work. |
| `example/feature-launch.md` | `feature-card`, `release-stages`, `comparison`, `cta-banner` — the product-launch flow. |
| `example/customer-story.md` | `success-story`, `big-stat`, `quote`, `profile` — customer-narrative flow. |

### Slidev keys while reviewing

- `→` / `Space` — next slide / next click step
- `←` — previous slide
- `o` — overview (grid of all slides — fastest way to scan for visual regressions)
- `g` — go to slide number
- `d` — dark/light mode toggle
- `Esc` — exit overview / presenter

### Hot reload while editing the theme

Slidev auto-reloads on file changes. Edit any `layouts/*.vue`,
`components/*.vue`, `styles/*.css`, or `setup/*.ts` while `just dev` is
running and the browser refreshes in place.

### Build, export, clean

```bash
just build slides         # → dist/slides/index.html (static SPA)
just pdf slides           # → slides.pdf (Slidev will prompt for Playwright on first run)
just export-png slides    # → dist/png-slides/*.png
just clean                # wipe Slidev caches + dist/
just list-examples        # show available example decks
```

Each `<deck>` arg accepts an example name (`slides`, `workshop`, etc.) or a
direct path.

### Link the theme into an external deck

For testing the theme inside a real deck you're authoring elsewhere:

```bash
cd your-deck
pnpm add link:../path/to/slidev-theme-temporal
```

Then set `theme: temporal` in your deck's frontmatter. Same hot-reload story
applies — edits to the linked theme repo reflect in the running Slidev
process. (`just new` already does all this wiring for you — see
[Quickstart](#quickstart--bootstrap-a-new-deck).)

### Pre-flight checks before opening a PR

No automated tests, so before you commit, run through this list manually:

1. `just dev` and scroll through every slide in overview mode (`o`).
   Visually check that:
   - No layout overflows or clips the footer
   - The Temporal logo is **white**, never mint green, on cover and end
     slides (this is the [brand rule](#brand-rule))
   - `.asset-pending` placeholders only appear in the layouts known to be
     asset-blocked (see [Asset status](#asset-status))
2. `just dev workshop` and confirm the configurable TOC renders the
   sections supplied by `themeConfig.toc` in that file's frontmatter.

## License

MIT. See [LICENSE](./LICENSE).
