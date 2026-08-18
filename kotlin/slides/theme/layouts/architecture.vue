<!--
  ABOUTME: Architecture diagram layout. PPT slides 45 (Before) and 46 (After Temporal).
  ABOUTME: Asset-blocked on the SVG illustrations per spec §5.7–8 — `image` slot renders placeholder.

  Frontmatter usage:
    ---
    layout: architecture
    eyebrow: ARCHITECTURE
    title: Before Temporal       # or "After Temporal"
    variant: before              # "before" | "after" — tints the frame
    ---

    ::image::

    ![alt](/path/to/before.svg)

    ::default::

    Optional caption rendered below the diagram.
-->
<script setup lang="ts">
import TemporalFooter from '../components/TemporalFooter.vue'

withDefaults(defineProps<{
  eyebrow?: string
  title?: string
  variant?: 'before' | 'after'
}>(), {
  variant: 'before',
})
</script>

<template>
  <div class="slidev-layout architecture bg-grid" :class="`variant-${variant}`">
    <header v-if="eyebrow || title" class="arch-header">
      <p v-if="eyebrow" class="eyebrow">{{ eyebrow }}</p>
      <h1 v-if="title" class="title">{{ title }}</h1>
    </header>
    <div class="diagram-frame">
      <div class="image-slot">
        <slot name="image">
          <div class="asset-pending">
            Architecture diagram (variant: {{ variant }}) · spec §5.7–8
          </div>
        </slot>
      </div>
    </div>
    <div class="caption"><slot /></div>
    <TemporalFooter />
  </div>
</template>

<style scoped>
.architecture {
  display: flex;
  flex-direction: column;
  height: 100%;
  gap: 0.9rem;
}
.eyebrow {
  margin: 0 0 0.3rem;
  font-size: 0.8rem;
  letter-spacing: 0.2em;
  text-transform: uppercase;
  color: var(--temporal-green);
}
.title {
  margin: 0;
  font-size: 2.2rem;
  font-weight: 200;
  letter-spacing: -0.02em;
  color: var(--temporal-text-strong);
}
.variant-before .title { color: var(--temporal-lavender); }
.variant-after  .title { color: var(--temporal-green); }
.diagram-frame {
  flex: 1 1 auto;
  min-height: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}
.image-slot {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}
.image-slot :deep(img),
.image-slot :deep(svg) {
  max-width: 100%;
  max-height: 100%;
}
.caption :deep(p) {
  margin: 0;
  color: var(--temporal-text-muted);
  font-size: 0.9rem;
  text-align: center;
}
</style>
