<!--
  ABOUTME: Temporal Model diagram (Developers / Productivity / Experience / Cash / Applications).
  ABOUTME: Asset-blocked on the circle SVG per spec §5.9. PPT slide 54.

  Frontmatter usage:
    ---
    layout: model-diagram
    title: The Temporal Model
    caption: How durable execution flows from developers to outcomes
    ---

    ::image::

    ![alt](/path/to/temporal-model.svg)
-->
<script setup lang="ts">
import TemporalFooter from '../components/TemporalFooter.vue'

defineProps<{
  title?: string
  caption?: string
}>()
</script>

<template>
  <div class="slidev-layout model-diagram bg-grid">
    <h1 v-if="title" class="title">{{ title }}</h1>
    <figure class="figure">
      <div class="image-slot">
        <slot name="image">
          <div class="asset-pending">
            Temporal Model circle diagram · spec §5.9
          </div>
        </slot>
      </div>
      <figcaption v-if="caption">{{ caption }}</figcaption>
    </figure>
    <TemporalFooter />
  </div>
</template>

<style scoped>
.model-diagram {
  display: flex;
  flex-direction: column;
  height: 100%;
  gap: 0.8rem;
}
.title {
  margin: 0;
  font-size: 2.2rem;
  font-weight: 200;
  letter-spacing: -0.02em;
  color: var(--temporal-text-strong);
}
.figure {
  margin: 0;
  flex: 1 1 auto;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 0.6rem;
  min-height: 0;
}
.image-slot {
  width: 100%;
  flex: 1 1 auto;
  min-height: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}
.image-slot :deep(img),
.image-slot :deep(svg) {
  max-width: 100%;
  max-height: 100%;
}
figcaption {
  color: var(--temporal-text-muted);
  font-size: 0.95rem;
}
</style>
