<!--
  ABOUTME: Pill chip indicating a feature's release stage (pre-release / public preview / GA).
  ABOUTME: Used by `feature-card` and `release-stages` layouts.

  Visual treatment is a brand-aligned default. Final treatment is asset-blocked
  on design (spec §5.13) — colors here are placeholders sourced from the palette.

  Usage:
    <ReleaseStageChip stage="pre-release" />
    <ReleaseStageChip stage="public-preview" />
    <ReleaseStageChip stage="general-availability" />
-->
<script lang="ts">
export type ReleaseStage = 'pre-release' | 'public-preview' | 'general-availability'
</script>

<script setup lang="ts">
defineProps<{ stage: ReleaseStage }>()

const STAGE_LABELS: Record<ReleaseStage, string> = {
  'pre-release': 'PRE-RELEASE',
  'public-preview': 'PUBLIC PREVIEW',
  'general-availability': 'GENERAL AVAILABILITY',
}
</script>

<template>
  <span class="chip" :class="`chip-${stage}`">{{ STAGE_LABELS[stage] }}</span>
</template>

<style scoped>
.chip {
  display: inline-block;
  font-family: 'Inter', system-ui, sans-serif;
  font-size: 0.72rem;
  font-weight: 500;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  padding: 0.3rem 0.75rem;
  border-radius: 999px;
  border: 1px solid currentColor;
  background: rgba(11, 16, 32, 0.6);
}
.chip-pre-release {
  color: var(--temporal-yellow);
}
.chip-public-preview {
  color: var(--temporal-blue);
}
.chip-general-availability {
  color: var(--temporal-green);
}
</style>
