<!--
  ABOUTME: Background-class wrapper used by cover.vue and any layout needing a hero backdrop.
  ABOUTME: Maps a `variant` prop to one of the `.bg-*` classes in styles/layout.css.

  Recognized variants (one per asset shipped in assets/):
    planet-teal  | planet-purple  | rex  | grid  | glow  | plain
    teal | purple                  (short aliases for planet-teal / planet-purple)
-->
<script lang="ts">
export const BACKGROUND_VARIANTS = [
  'planet-teal',
  'planet-purple',
  'rex',
  'grid',
  'glow',
  'plain',
] as const

export type BackgroundVariant = (typeof BACKGROUND_VARIANTS)[number] | 'teal' | 'purple'
</script>

<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(defineProps<{ variant?: BackgroundVariant }>(), {
  variant: 'planet-teal',
})

const bgClass = computed(() => {
  switch (props.variant) {
    case 'teal':
      return 'bg-planet-teal'
    case 'purple':
      return 'bg-planet-purple'
    default:
      return `bg-${props.variant}`
  }
})
</script>

<template>
  <div class="bg-layer" :class="bgClass">
    <slot />
  </div>
</template>

<style scoped>
.bg-layer {
  position: relative;
  width: 100%;
  height: 100%;
}
</style>
