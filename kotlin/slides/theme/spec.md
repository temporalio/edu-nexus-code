# Audit: `Temporal Slidey Slides Optimized v2026.pptx` vs. `slidev-theme-temporal`

## 1. Deck inventory (60 slides)

| # | Slide(s) | PPT layout name | Type / purpose |
|---|---|---|---|
| Meta | 1–2 | BLANK | Template usage instructions — not part of theme |
| Cover variants | 3–8 | TITLE_2_2_1 family (6 variants) | Title slide with **6 different hero artwork backgrounds** (images 10, 14, 18, 19, 22, 24, 49 — planets/space/abstract) |
| Agenda | 9, 14 | BLANK_2 | Numbered grid agenda (`00`–`04` cards) |
| Section dividers | 10–13 | SECTION_HEADER, BLANK_2 | Big section title, sub-section variant |
| Simple content | 15–16 | TITLE_AND_BODY_3(_1) | Title + body paragraph, with/without extra block |
| Content + image | 17–18 | ONE_COLUMN_TEXT_1(_1) | Text left / image right and inverse |
| Two-column text | 19–24 | CUSTOM_7 family | Two-column body, with header variants and uppercase eyebrow |
| Eyebrow + big heading | 25–28 | BLANK_1 | "TEMPORAL CLOUD / Cost Effectiveness" style + checklist variants |
| Checklists | 25, 26, 49 | BLANK_1 | "Temporal for X" tick-list grid |
| Feature release cards | 29–31 | BLANK_2 | Feature name + Big Picture/What You'll Get/Later + **PRE-RELEASE / PUBLIC PREVIEW / GENERAL AVAILABILITY chip** |
| Big stat | 32 | TITLE_ONLY_1_1_1_1 | Huge number + caption (with image51 ribbon graphic) |
| Person/profile card | 33 | CUSTOM_4 | Avatar + Company / Name / Job Title |
| Quote cards | 34–35 | CUSTOM_4 | Avatar + quote + attribution |
| Tables | 36–37 | CUSTOM_1_2, BLANK_1 | Styled comparison tables |
| Charts | 38–40 | BLANK_1 | Image-based chart placeholders (3 variants) |
| Timelines | 41, 43 | BLANK_1_2(_3) | Horizontal year/month timeline |
| Release stages | 42 | BLANK_1_2_3 | 3-step PRE-RELEASE → PUBLIC PREVIEW → GA explainer |
| Code example | 44 | BLANK_1 | Code block + side explanation |
| Architecture | 45–46 | BLANK_1 | "Before / After Temporal" service diagrams (image-based) |
| Multi-column CTA | 47 | TITLE_AND_BODY_3 | "Join the Conversation" — 3 icon columns (Zoom / Slack / Newsletter) |
| Startup credits | 48 | TITLE_2_2 | "$6,000 in free Cloud Credits" CTA |
| POC checklist | 49 | BLANK_1 | "Start a POC" two-column tick-list |
| OSS vs Cloud comparison | 50–53 | CUSTOM_7_1_1_1_1_1 family | Two-card OSS / Temporal Cloud comparison with feature list, 4 variants |
| Temporal model diagram | 54 | BLANK_1 | "Developers / Productivity / Experience / Cash / Applications" circle diagram |
| Customer success story | 55–56 | CUSTOM_7_2_1 | Logo + Challenges / Solution columns |
| Temporal Cloud intro | 57 | TITLE_AND_BODY_3 | Hero/logo-led intro |
| Enterprise readiness | 58 | TITLE_AND_BODY_3 | 3-column SECURITY / SUPPORT / SCALE feature grid |
| Q&A | 59 | BLANK_2 | Centered "Q&A" |
| Close | 60 | CUSTOM | Logo-only close |

## 2. Current theme inventory (snapshot of starting state)

This is the state of the theme today. The target state after this spec is implemented is captured in §6 (new layouts/components) and §3 (palette/de-workshopify decisions).

**Layouts (7):** `cover`, `default`, `section`, `exercise`, `two-cols`, `toc`, `end`
**Components (3):** `TemporalLogo`, `TemporalFooter`, `WorkshopToc`
**Assets (5):** `glow.webp`, `grid.webp`, `planet-purple.webp`, `planet-teal.webp`, `rex.webp`
**Setup:** Shiki `temporal-dark`, Mermaid palette
**Styles:** `layout.css`, `code.css` (Inter + Noto Sans Mono, mint/purple/lavender palette)

**Sample/demo deck: none.** No `example.md`, `slides.md`, or demo slides exist in the repo. A complete sample deck mirroring the PPT will be built in `example/` (see §7).

## 3. Theme cleanup decisions (existing-theme issues, resolved)

These are problems with what's already in place — not deck gaps. Decisions adopted:

1. **De-workshop-ify the theme — ADOPTED.** `TemporalFooter.vue:18` is hard-coded to `Replay 2026 | Tailscale + Temporal`, and `WorkshopToc.vue:9-18` hard-codes the Tailscale workshop section list. Both become configurable:
   - `TemporalFooter` gains a `text` (or `deckTitle`) prop, sourced from Slidev frontmatter (e.g., `themeConfig.footer`). The footer page indicator stays automatic.
   - `WorkshopToc` becomes a **generic configurable TOC component** (renamed `AgendaList` or kept as `WorkshopToc` with a `sections` prop). Section list is supplied by the deck via frontmatter (e.g., `themeConfig.toc: [{ id, label }, …]`). The current Tailscale list moves out of the theme and into the consuming deck's frontmatter. **Workshop TOC stays as a theme feature — it's useful — just no longer Tailscale-specific.**

2. **Match the PPT palette exactly — ADOPTED.** Replace and add CSS variables:
   - `--temporal-purple`: `#7C3AED` → **`#5D24BD`** (PPT accent2)
   - `--temporal-yellow`: `#FEE084` → **`#FECB2F`** (PPT accent4)
   - **Add** `--temporal-blue: #444CE7` (PPT accent1, hyperlinks)
   - **Add** `--temporal-red: #ED360E` (PPT accent5)
   - `--temporal-green` stays `#59FDA0` (matches PPT accent6)
   - `--temporal-magenta` stays `#FF6BFF` (matches PPT accent3)
   - Update the Mermaid palette in `setup/mermaid.ts` to follow these new values.

3. **Align link color to PPT — ADOPTED.** Theme `<a>` color: mint green → **`#444CE7` (brand blue)**, matching PPT hyperlink color. Mint green stays reserved for accent text, code highlights, and decorative UI affordances (and remains forbidden on the logo per §9).

4. **Body font — KEEP Inter.** PPT theme XML declares Arial; that's a print-compatibility fallback. Inter is the canonical digital brand font and stays.

## 4. Layout/component coverage matrix

Every PPT slide type must map to a Slidev layout. ❌ rows below are the build list. The `toc` layout stays — it generalizes from Tailscale-specific to configurable (see §3.1), and serves as the agenda for both workshop and corporate decks.

| PPT slide type | Existing Slidev layout | Status / action |
|---|---|---|
| Cover/title | `cover` | ⚠ basic title, only 1 background variant (planet-teal). Add 5 more variants to match PPT's 6 hero artworks (see §5 assets). |
| Section divider | `section` | ✅ |
| Sub-section divider | `section` | ⚠ add `subsection` variant with `SECTION` eyebrow label |
| Agenda / TOC | `toc` + `WorkshopToc` | ⚠ generalize: `WorkshopToc` becomes a config-driven `AgendaList`/`WorkshopToc` (see §3.1). Keeps the numbered-grid look from PPT slides 9 & 14. |
| Simple content | `default` | ✅ |
| Content + image (L/R) | (use `two-cols`) | ❌ add dedicated `image-right` / `image-left` layouts |
| Two-column text | `two-cols` | ⚠ add `two-cols-header` variant with shared overall title + uppercase-eyebrow column headers |
| Eyebrow + big heading | — | ❌ build `eyebrow-hero` |
| Checklist grid ("Temporal for X") | — | ❌ build `checklist` |
| Feature release card | — | ❌ build `feature-card` + `ReleaseStageChip` component |
| Big stat | — | ❌ build `big-stat` |
| Person / profile card | — | ❌ build `profile` |
| Quote card | — | ❌ build `quote` |
| Styled comparison table | (markdown table) | ⚠ extend table styling to match PPT (zebra rows, tier emphasis) |
| Chart placeholder | — | ❌ build `chart` layout (image slot + caption) |
| Timeline | — | ❌ build `timeline` + `TimelineRail` component |
| Release stages 3-step | — | ❌ build `release-stages` |
| Code + explanation | (use `two-cols`) | ❌ build `code-explain` |
| Architecture (before/after) | — | ❌ build `architecture` layout (image slot, before/after variant). Asset-blocked. |
| Multi-column CTA w/ icons | — | ❌ build `cta-icons` (asset-blocked on icons) |
| Startup credits CTA | — | ❌ build `cta-banner` |
| OSS vs Cloud comparison | — | ❌ build `comparison` |
| Temporal model diagram | — | ❌ build `model-diagram` layout (asset-blocked) |
| Customer success story | — | ❌ build `success-story` |
| Enterprise readiness 3-column | — | ❌ build `feature-grid` (N-column) |
| Q&A | (use `section`) | ❌ build `qa` (minimal centered variant) |
| End | `end` | ✅ |
| Exercise (workshop) | `exercise` | ✅ — workshop-only; no PPT equivalent, retained for workshop decks |

## 5. Missing design assets (request from design team)

One row per asset. Do **not** extract these from the PPT — get the source files from design. Every row maps to the PPT slide(s) where the asset appears so you can review against the deck 1:1.

**Cover artwork is NOT in this list.** The five backgrounds shipped in `assets/` (`planet-teal.webp`, `planet-purple.webp`, `rex.webp`, `grid.webp`, `glow.webp`) are the canonical cover artworks the PPT uses. They map to the cover `variant` prop in `layouts/cover.vue`.

| # | Asset | PPT slide(s) | PPT image ref | Notes for design |
|---|---|---|---|---|
| 1 | "Before Temporal" architecture diagram | 45 | embedded | Multi-service tangle illustration. SVG preferred; Mermaid fallback possible. |
| 2 | "After Temporal" architecture diagram | 46 | embedded | Cleaned-up version. SVG preferred. |
| 3 | "Temporal Model" circle diagram | 54 | embedded | Developers / Productivity / Experience / Cash / Applications. SVG preferred. |
| 4 | Big-stat ribbon graphic | 32 | `image51.png` | Decorative ribbon behind the stat number |
| 5 | Communications icon set (Zoom, Slack, Newsletter) | 47 | embedded | Brand-approved icon set, SVG |
| 6 | Checkmark icon | 25, 26, 49 | `✓` glyph | Confirm if there's an official SVG; otherwise we ship our own |
| 7 | Release-stage chips: `PRE-RELEASE`, `PUBLIC PREVIEW`, `GENERAL AVAILABILITY` | 29, 30, 31, 42 | embedded | Exact colors, padding, corner radius, text treatment |
| 8 | Customer logo treatment (placeholder + slot spec) | 55, 56 (Dust, ZoomInfo) | embedded | Not the customer logos themselves — the layout slot's treatment (size, position, padding) |
| 9 | Avatar / portrait placeholder | 33, 34, 35 | embedded | Design's preferred placeholder for profile + quote cards |

Brand decisions (palette, link color, font) that previously lived here as "confirmations" are now resolved in §3.

## 6. Layouts and components to build

Build out **all** of the following — both layouts and components. Items marked *(asset-blocked)* can have placeholder/skeleton implementations now and pick up the final asset once design delivers (see §5).

**Layouts:**

- `subsection` — section with eyebrow `SECTION` label
- `image-right` / `image-left` — text + image columns
- `two-cols-header` — two-col with shared overall title + uppercase-eyebrow column headers
- `eyebrow-hero` — eyebrow + giant heading ("TEMPORAL CLOUD / Cost Effectiveness")
- `checklist` — "Temporal for X" tick grid
- `feature-card` — feature name + Big Picture / What You'll Get / Later + release-stage chip
- `big-stat` — huge number + caption *(asset-blocked on ribbon graphic, see §5.10)*
- `profile` — avatar + name + role *(asset-blocked on avatar placeholder, see §5.15)*
- `quote` — quote + attribution + avatar *(asset-blocked on avatar placeholder)*
- `timeline` — horizontal year/month rail
- `release-stages` — 3-step pre-release → preview → GA
- `code-explain` — code block + adjacent prose
- `architecture` — illustration slot + caption (before/after variant) *(asset-blocked, §5.7–8)*
- `model-diagram` — illustration slot for the Temporal Model circles *(asset-blocked, §5.9)*
- `comparison` — OSS vs Cloud two-card with feature list
- `chart` — image slot + caption
- `cta-icons` — multi-column CTA with icon-led blocks *(asset-blocked on icon set, §5.11)*
- `cta-banner` — single-action CTA ("$6,000 credits / apply at…")
- `success-story` — logo + challenges + solution columns
- `feature-grid` — N-column feature card grid (3-col for enterprise-readiness)
- `qa` — minimal Q&A divider

**Components:**

- `ReleaseStageChip` — three states (`PRE-RELEASE` / `PUBLIC PREVIEW` / `GENERAL AVAILABILITY`); used by `feature-card` and `release-stages`. *(treatment asset-blocked on §5.13, but a reasonable default ships)*
- `Checkmark` — consistent tick mark used by `checklist` and any "POC checklist" content
- Generic configurable TOC — generalize `WorkshopToc` per §3.1 (rename to `AgendaList` or keep the name with a `sections` prop). Drives both the agenda and TOC use cases.
- `ProfileAvatar` — used by `profile` and `quote`
- `QuoteAttribution` — quote source + role block
- `TimelineRail` — horizontal year/month rail primitive used by `timeline`
- `FeatureBlock` — title + body block used by `feature-card`, `feature-grid`, `cta-icons`
- `BackgroundLayer` — utility that maps the cover `variant` prop to one of the shipped backgrounds (`planet-teal`, `planet-purple`, `rex`, `grid`, `glow`, `plain`)
- `Footer` config — `TemporalFooter` accepts deck-title prop, sourced from frontmatter per §3.1

## 7. Sample-slides deck — decision

**Build it.** Create an `example/` directory containing one or more sample decks that exercise every layout, mirroring the PPT 1:1 so a deck author can copy-paste a slide and adapt it.

- **Location:** `example/` directory at the repo root (excluded from the npm `files` array so it doesn't ship to consumers).
- **Structure:** support multiple example decks so we can demonstrate different "chapters" or use cases. Recommended initial layout:
  - `example/slides.md` — the full reference deck, one example per layout, in PPT order
  - `example/workshop.md` — minimal workshop deck demonstrating `exercise` + configurable `WorkshopToc`/`AgendaList` (the existing Tailscale content moves here, out of the theme itself)
  - `example/<chapter>.md` — additional themed mini-decks as needed (e.g., feature-launch, customer-story, enterprise-pitch)
- **Discoverability:** README links each example with a one-line description.
- **Maintenance contract:** every new layout added to the theme MUST add a corresponding slide in `example/slides.md` in the same PR.

## 8. Decisions resolved

1. **De-workshop-ify the theme — YES.** `TemporalFooter` and `WorkshopToc` move to configurable props sourced from deck frontmatter. The Tailscale-specific content relocates to `example/workshop.md`. Treated as a breaking change — version-bump the theme accordingly. See §3.1 for the mechanics.
2. **Build scope — implement everything.** All layouts and components in §6 will be built. No priority cuts; the goal is parity with the PPT. Asset-blocked layouts ship with skeleton/placeholder implementations and adopt the real assets once design delivers (§5).
3. **Sample deck location — `example/` directory.** Allows multiple per-chapter demo decks (reference deck, workshop deck, themed mini-decks). Excluded from the npm `files` array. See §7 for structure.

## 9. Brand rule reminder

The Temporal logo MUST NEVER be rendered in Temporal mint green (`#59FDA0` / `var(--temporal-green)`). White on dark, or black on light, only. Mint green is reserved for accent text, links, code highlights, and UI affordances — never the logo itself. Any new layouts that surface the logo (covers, end cards, footers, CTA banners) must respect this rule.
