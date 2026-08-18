<!--
  ABOUTME: Sub-section divider. Eyebrow "SECTION" label above a centered heading.
  ABOUTME: Use between major sections inside a larger section, when `section` is too heavy.

  Frontmatter:
    ---
    layout: subsection
    eyebrow: SECTION              # optional, defaults to "SECTION"
    ---
    # Sub-section Title
-->
<script setup lang="ts">
withDefaults(defineProps<{ eyebrow?: string }>(), {
  eyebrow: 'SECTION',
})
</script>

<template>
  <div class="slidev-layout subsection bg-planet-purple">
    <div class="subsection-inner">
      <p class="eyebrow">{{ eyebrow }}</p>
      <slot />
    </div>
  </div>
</template>

<style scoped>
.subsection {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 3rem;
  position: relative;
}
.subsection::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, rgba(11, 16, 32, 0.35) 0%, rgba(11, 16, 32, 0.55) 100%);
  pointer-events: none;
}
.subsection-inner {
  position: relative;
  text-align: center;
  max-width: 22ch;
}
.eyebrow {
  margin: 0 0 0.8rem;
  font-size: 0.85rem;
  letter-spacing: 0.24em;
  text-transform: uppercase;
  color: var(--temporal-green);
  font-weight: 500;
}
.subsection :deep(h1) {
  font-size: 2.6rem;
  font-weight: 200;
  letter-spacing: -0.02em;
  color: #ffffff;
  margin: 0;
  line-height: 1.1;
}
.subsection :deep(p) {
  color: var(--temporal-text);
  margin-top: 1rem;
}
</style>
