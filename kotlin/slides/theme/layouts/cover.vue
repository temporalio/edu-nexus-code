<!--
  ABOUTME: Cover / title layout. Big hero title + speaker block over selectable backdrop.
  ABOUTME: 5 hero variants map to the assets shipped in assets/ — same artwork the PPT uses.

  Frontmatter usage:

    ---
    layout: cover
    variant: planet-teal    # planet-teal | planet-purple | rex | grid | glow
    ---

    # Big Hero Title
    ## Subtitle line
    Speaker · Month YYYY
-->
<script setup lang="ts">
import TemporalLogo from '../components/TemporalLogo.vue'
import BackgroundLayer, { type BackgroundVariant } from '../components/BackgroundLayer.vue'

withDefaults(defineProps<{ variant?: BackgroundVariant }>(), {
  variant: 'planet-teal',
})
</script>

<template>
  <div class="slidev-layout cover">
    <BackgroundLayer :variant="variant">
      <div class="cover-inner">
        <TemporalLogo class="cover-logo" />
        <slot />
      </div>
    </BackgroundLayer>
  </div>
</template>

<style scoped>
.cover {
  padding: 0;
  position: relative;
}
.cover :deep(.bg-layer)::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, rgba(11, 16, 32, 0.15) 0%, rgba(11, 16, 32, 0.55) 100%);
  pointer-events: none;
}
.cover-inner {
  position: relative;
  height: 100%;
  padding: 3.5rem 3.5rem 3rem;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  box-sizing: border-box;
}
/* BRAND RULE: The Temporal logo (plus mark / wordmark) MUST NEVER be rendered in
   Temporal mint green. White-on-dark or black-on-light only. Mint green is reserved
   for accent text, links, and UI affordances - never the mark itself. Do not "fix"
   this back to var(--temporal-green). */
.cover-logo {
  width: 44px;
  height: 44px;
  color: #ffffff;
}
.cover :deep(h1) {
  font-size: 3rem;
  font-weight: 200;
  letter-spacing: -0.02em;
  color: #ffffff;
  max-width: 18ch;
  margin: 0;
}
.cover :deep(h2) {
  font-weight: 300;
  color: var(--temporal-lavender);
  font-size: 1.3rem;
  margin: 0.4rem 0 0;
}
.cover :deep(p) {
  color: var(--temporal-text);
  font-size: 0.95rem;
  line-height: 1.7;
}
.cover :deep(strong) {
  color: #ffffff;
  font-weight: 500;
}
</style>
