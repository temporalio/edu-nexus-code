<!--
  ABOUTME: Inline SVG check mark used by checklist and POC layouts.
  ABOUTME: Inherits color via currentColor so any layout can tint it.

  Usage:
    <Checkmark />
    <Checkmark label="Completed" />   <!-- announces to screen readers -->
-->
<script setup lang="ts">
defineProps<{ label?: string }>()
</script>

<template>
  <svg
    class="checkmark"
    viewBox="0 0 24 24"
    fill="none"
    stroke="currentColor"
    stroke-width="2.5"
    stroke-linecap="round"
    stroke-linejoin="round"
    :aria-hidden="label ? undefined : 'true'"
    :aria-label="label"
    :role="label ? 'img' : undefined"
  >
    <polyline points="4 12 10 18 20 6" />
  </svg>
</template>

<style scoped>
.checkmark {
  display: inline-block;
  width: 1.1em;
  height: 1.1em;
  color: var(--temporal-green);
  vertical-align: -0.15em;
}
</style>
