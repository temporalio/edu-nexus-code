# Example decks

Reference decks demonstrating every layout in `slidev-theme-temporal`. None
of these files ship in the npm package — they're for development and as
copy-paste starting points.

## Files

- **`slides.md`** — full reference deck. One slide per layout, in PPT order.
  Use this when you want to see every layout side-by-side or copy one into
  your own deck.
- **`workshop.md`** — minimal workshop deck demonstrating the `exercise`
  layout and configurable workshop TOC (`themeConfig.toc` in frontmatter).
  Home of the Replay 2026 / Tailscale workshop content that used to be baked
  into the theme.
- **`feature-launch.md`** — themed mini-deck for product feature launches.
  Showcases `cover` → `eyebrow-hero` → `feature-card` → `release-stages` →
  `comparison` → `cta-banner` → `end`.
- **`customer-story.md`** — themed mini-deck for customer success stories.
  Showcases `cover` → `success-story` → `big-stat` → `quote` → `profile` →
  `end`.

## Running

From the repo root:

```bash
pnpm dlx @slidev/cli example/slides.md
```

Substitute any of the other files to run those decks.

## Maintenance contract

Every new layout added to the theme **must** have at least one slide
demonstrating it in `example/slides.md` in the same change. The reference
deck is the source of truth for "what shapes does this theme support."
