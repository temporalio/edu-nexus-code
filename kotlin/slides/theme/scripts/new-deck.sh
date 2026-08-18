#!/usr/bin/env bash
# ABOUTME: Scaffold or augment a directory with a Slidev deck wired to slidev-theme-temporal.
# ABOUTME: Invoked by `just new <target> [starter]`. Safe to point at directories that already have code.

set -euo pipefail

target="${1:?Usage: new-deck.sh <target-path> [starter]}"
starter="${2:-minimal}"

# Resolve the theme repo root (this script lives in <theme>/scripts/).
theme_path="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd -P)"

# --- Validate inputs before writing anything --------------------------------

# Resolve the starter name to the example/ filename it copies from.
# "reference" is the user-facing name for the full reference deck stored at
# example/slides.md (calling it "slides" at the CLI would be confusing).
src_file=""
case "$starter" in
  minimal)        : ;;
  reference)      src_file="$theme_path/example/slides.md" ;;
  workshop)       src_file="$theme_path/example/workshop.md" ;;
  feature-launch) src_file="$theme_path/example/feature-launch.md" ;;
  customer-story) src_file="$theme_path/example/customer-story.md" ;;
  *)
    echo "Unknown starter '$starter'. Use one of: minimal, reference, workshop, feature-launch, customer-story" >&2
    exit 1
    ;;
esac

if [[ -n "$src_file" ]] && [[ ! -f "$src_file" ]]; then
  echo "Starter source $src_file is missing. This is a theme bug — file an issue." >&2
  exit 1
fi

mkdir -p "$target"
target_abs="$(cd "$target" && pwd -P)"
name="$(basename "$target_abs")"

if [[ -e "$target_abs/slides.md" ]]; then
  echo "Refusing to overwrite existing $target_abs/slides.md" >&2
  echo "Remove or rename it, then re-run." >&2
  exit 1
fi

# Check justfile recipe collisions BEFORE we write anything else, so a
# conflict doesn't leave the directory half-modified.
if [[ -f "$target_abs/justfile" ]]; then
  for recipe in slides slides-build slides-pdf slides-clean; do
    if grep -qE "^${recipe}( |:|$)" "$target_abs/justfile"; then
      echo "Existing $target_abs/justfile already defines a '$recipe' recipe." >&2
      echo "Remove or rename it, then re-run — we won't overwrite your existing recipes." >&2
      exit 1
    fi
  done
fi

# Compute the relative path from the target back to the theme repo. Slidev's
# `theme:` field accepts paths, so we use this instead of installing the theme
# into a node_modules — the deck dir doesn't need its own package.json.
rel_theme="$(python3 -c 'import os, sys; print(os.path.relpath(sys.argv[1], sys.argv[2]))' "$theme_path" "$target_abs")"

# --- 1. slides.md -----------------------------------------------------------

if [[ "$starter" == "minimal" ]]; then
  cat > "$target_abs/slides.md" <<MARKDOWN
---
theme: $rel_theme
title: $name
info: |
  Created from slidev-theme-temporal
themeConfig:
  footer: "$name"
  toc:
    - id: intro
      label: Introduction
    - id: body
      label: Main content
    - id: wrap
      label: Wrap-up
layout: cover
variant: a
---

# $name

## Subtitle goes here

Month YYYY

---
layout: toc
current: intro
---

---
layout: section
---

# Section title

---
layout: default
---

# A body slide

Replace this content with your own.

---
layout: end
---

# Thank you

Questions? **temporal.io**
MARKDOWN
else
  # Copy the example deck, rewriting its in-repo `theme: ../` reference to
  # the relative path from the new deck back to the theme repo.
  sed "s|^theme: \.\./\$|theme: $rel_theme|" "$src_file" > "$target_abs/slides.md"
fi

slides_msg="created $target_abs/slides.md"

# --- 2. justfile ------------------------------------------------------------

# Recipes added by this scaffolder. Each one runs Slidev via pnpm dlx so the
# host directory doesn't need a package.json or node_modules.
read -r -d '' slides_recipes <<'RECIPES' || true
# === Slidev recipes (added by slidev-theme-temporal) ===

# Run the Slidev dev server on slides.md.
slides:
    pnpm dlx @slidev/cli slides.md --open

# Build slides.md to a static site at dist/slides/.
slides-build:
    pnpm dlx @slidev/cli build slides.md --out dist/slides

# Export slides.md to slides.pdf (Slidev will prompt to install Playwright on first run).
slides-pdf:
    pnpm dlx @slidev/cli export slides.md --output slides.pdf

# Wipe Slidev caches and built output.
slides-clean:
    rm -rf .slidev dist slides.pdf
RECIPES

if [[ -f "$target_abs/justfile" ]]; then
  # Make sure the existing justfile ends with a newline, then append a blank
  # line as separator before our recipes.
  [[ -n "$(tail -c1 "$target_abs/justfile")" ]] && printf '\n' >> "$target_abs/justfile"
  printf '\n%s\n' "$slides_recipes" >> "$target_abs/justfile"
  justfile_msg="appended Slidev recipes to existing $target_abs/justfile"
else
  cat > "$target_abs/justfile" <<'HEADER'
# justfile for this Slidev deck.
# Run `just --list` to see every recipe.

default: slides

HEADER
  printf '%s\n' "$slides_recipes" >> "$target_abs/justfile"
  justfile_msg="created $target_abs/justfile"
fi

# --- 3. .gitignore ----------------------------------------------------------

gitignore_entries=("node_modules" "dist" ".slidev" "*.pdf")
gitignore_file="$target_abs/.gitignore"

if [[ -f "$gitignore_file" ]]; then
  added=0
  for entry in "${gitignore_entries[@]}"; do
    if ! grep -qxF "$entry" "$gitignore_file"; then
      echo "$entry" >> "$gitignore_file"
      added=1
    fi
  done
  if [[ $added -eq 1 ]]; then
    gitignore_msg="added Slidev entries to $target_abs/.gitignore"
  else
    gitignore_msg="$target_abs/.gitignore already covers Slidev outputs"
  fi
else
  printf '%s\n' "${gitignore_entries[@]}" > "$gitignore_file"
  gitignore_msg="created $target_abs/.gitignore"
fi

# --- 4. Summary -------------------------------------------------------------

cat <<DONE

✓ Slidev deck scaffolded into $target_abs ($starter starter)
  • $slides_msg
  • $justfile_msg
  • $gitignore_msg

The theme is referenced via the relative path $rel_theme — no
node_modules or package.json is needed in this directory; Slidev runs via
pnpm dlx.

Next steps:
  cd $target_abs
  just slides         # opens http://localhost:3030

If you move the theme repo or the deck later, update the 'theme:' line in
slides.md to the new relative path.
DONE
