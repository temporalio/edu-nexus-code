<!--
  ABOUTME: Configurable agenda / workshop TOC. Renders the section list from deck frontmatter.
  ABOUTME: Active row (matching `current` prop) is highlighted in mint; rest read muted.

  Frontmatter example:

    ---
    theme: temporal
    themeConfig:
      toc:
        - id: arch
          label: Architecture
        - id: ex1
          label: Exercise 1
        - id: wrap
          label: Wrap-up
    ---

  Layout usage:

    ---
    layout: toc
    current: ex1
    ---

  Works for both workshop TOCs and corporate-deck agendas — the section list is
  fully driven by the consuming deck via `themeConfig.toc`.
-->
<script setup lang="ts">
defineProps<{ current?: string }>()
</script>

<template>
  <ol class="workshop-toc">
    <li
      v-for="(section, index) in ($slidev.themeConfigs?.toc ?? [])"
      :key="section.id"
      :class="{ active: current === section.id }"
    >
      <span class="number">{{ String(index + 1).padStart(2, '0') }}</span>
      <span class="label">{{ section.label }}</span>
    </li>
  </ol>
</template>

<style scoped>
.workshop-toc {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 0.55rem;
}
.workshop-toc li {
  display: flex;
  align-items: baseline;
  gap: 1.25rem;
  padding: 0.1rem 0;
  color: var(--temporal-text-muted);
  font-weight: 300;
  font-size: 1.5rem;
  transition: color 150ms ease;
}
.workshop-toc li .number {
  font-variant-numeric: tabular-nums;
  font-size: 1.05rem;
  letter-spacing: 0.1em;
  color: var(--temporal-text-muted);
  opacity: 0.65;
}
.workshop-toc li.active {
  color: #ffffff;
}
.workshop-toc li.active .number {
  color: var(--temporal-green);
  opacity: 1;
}
.workshop-toc li.active .label {
  font-weight: 400;
}
</style>
