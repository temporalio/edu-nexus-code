# Changelog

## Unreleased — 2026-05-15

Brings the theme to parity with the 2026 Temporal Slidey Slides PowerPoint
template. **Contains breaking changes** — see below. Version stays at `0.1.0`
until Mason cuts a release.

### Breaking changes

- **`TemporalFooter`** no longer hard-codes its left-side text. It now reads
  from `themeConfig.footer` in deck frontmatter. To preserve previous
  behavior, set in your deck:

  ```yaml
  themeConfig:
    footer: "Replay 2026 | Your Workshop"
  ```

- **`WorkshopToc`** no longer hard-codes its section list. It now reads from
  `themeConfig.toc` in deck frontmatter:

  ```yaml
  themeConfig:
    toc:
      - id: arch
        label: Architecture
      - id: ex1
        label: Exercise 1
  ```

  The Tailscale workshop section list has been relocated to
  `example/workshop.md`.

### Added — layouts (22 new, 29 total)

`subsection`, `image-right`, `image-left`, `two-cols-header`, `eyebrow-hero`,
`qa`, `cta-banner`, `checklist`, `feature-card`, `release-stages`, `profile`,
`quote`, `big-stat`, `timeline`, `code-explain`, `chart`, `comparison`,
`success-story`, `feature-grid`, `cta-icons`, `architecture`, `model-diagram`.

Existing layouts retained: `cover`, `default`, `section`, `exercise`,
`two-cols`, `toc`, `end`.

### Added — components

- `ReleaseStageChip` — pill chip for `pre-release` / `public-preview` /
  `general-availability` stages
- `Checkmark` — inline SVG check used by checklist layouts
- `ProfileAvatar` — circular avatar with gradient placeholder fallback
- `QuoteAttribution` — name + role block for quote/profile layouts
- `TimelineRail` — horizontal year/month rail primitive
- `FeatureBlock` — title + body primitive for feature grids
- `BackgroundLayer` — variant → class mapping for cover hero backdrops

### Added — cover variants

The `cover` layout now accepts a `variant` prop selecting one of the five
hero artworks shipped in `assets/`:

- `planet-teal` (default)
- `planet-purple`
- `rex`
- `grid`
- `glow`

`teal` and `purple` are short aliases for the two planet variants.

### Added — example decks

A new `example/` directory ships full demos:

- `example/slides.md` — full reference deck, one slide per layout
- `example/workshop.md` — minimal workshop deck (Tailscale relocation home)
- `example/feature-launch.md` — themed product-launch mini-deck
- `example/customer-story.md` — themed customer success mini-deck

Excluded from the npm package (`example/` is not in the `files` array).

### Changed — palette migrated to PPT-exact

- `--temporal-purple`: `#7C3AED` → **`#5D24BD`** (PPT accent2)
- `--temporal-yellow`: `#FEE084` → **`#FECB2F`** (PPT accent4)
- **Added** `--temporal-blue: #444CE7` (PPT accent1, hyperlinks)
- **Added** `--temporal-red: #ED360E` (PPT accent5)
- Link color migrated from mint green to brand blue
- Mermaid palette updated in lockstep

### Asset-blocked (skeletons ship today; final art lands when design delivers)

These layouts work today with `.asset-pending` placeholders. See `spec.md` §5
for the request list:

- `architecture` (before/after diagrams): pending SVG illustrations
- `model-diagram` (Temporal Model circles): pending SVG
- `cta-icons` (Zoom/Slack/Newsletter icons): pending brand-approved icon set
- `big-stat` ribbon graphic: pending
- `success-story` customer-logo treatment: pending the slot spec

### Brand rule (unchanged, non-negotiable)

The Temporal logo MUST NEVER be rendered in Temporal mint green
(`#59FDA0` / `var(--temporal-green)`). White on dark, or black on light, only.
Any layout that surfaces `TemporalLogo` enforces this in its scoped CSS.
