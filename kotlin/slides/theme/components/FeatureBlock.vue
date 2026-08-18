<!--
  ABOUTME: Title + body block primitive used by feature-card, feature-grid, cta-icons.
  ABOUTME: Title is optional; default slot is the body.
-->
<script setup lang="ts">
defineProps<{
  title?: string
  eyebrow?: string
}>()
</script>

<template>
  <div class="feature-block">
    <p v-if="eyebrow" class="eyebrow">{{ eyebrow }}</p>
    <h3 v-if="title" class="title">{{ title }}</h3>
    <div class="body">
      <slot />
    </div>
  </div>
</template>

<style scoped>
.feature-block {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}
.eyebrow {
  margin: 0;
  font-size: 0.75rem;
  letter-spacing: 0.18em;
  text-transform: uppercase;
  color: var(--temporal-green);
  font-weight: 500;
}
.title {
  margin: 0;
  color: var(--temporal-text-strong);
  font-weight: 400;
  font-size: 1.4rem;
  line-height: 1.2;
}
.body {
  color: var(--temporal-text);
  font-weight: 300;
  line-height: 1.5;
  font-size: 1.05rem;
}
.body :deep(p) {
  margin: 0.3rem 0;
}
</style>
