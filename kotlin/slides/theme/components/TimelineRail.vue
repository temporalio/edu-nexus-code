<!--
  ABOUTME: Horizontal timeline rail of year/month nodes. Used by `timeline` layout.
  ABOUTME: Each point: { label, date }. Empty array renders an empty rail (no error).
-->
<script setup lang="ts">
defineProps<{
  points: Array<{ label: string; date: string }>
}>()
</script>

<template>
  <div class="rail">
    <div class="rail-line" aria-hidden="true"></div>
    <ol class="nodes">
      <li v-for="(p, i) in points" :key="i" class="node">
        <span class="dot" aria-hidden="true"></span>
        <time class="date">{{ p.date }}</time>
        <span class="label">{{ p.label }}</span>
      </li>
    </ol>
  </div>
</template>

<style scoped>
.rail {
  position: relative;
  padding: 1.5rem 0 0.5rem;
}
.rail-line {
  position: absolute;
  top: 2.2rem;
  left: 5%;
  right: 5%;
  height: 1px;
  background: linear-gradient(
    90deg,
    transparent 0%,
    var(--temporal-green) 8%,
    var(--temporal-green) 92%,
    transparent 100%
  );
  opacity: 0.6;
}
.nodes {
  list-style: none;
  padding: 0;
  margin: 0;
  display: grid;
  grid-auto-flow: column;
  grid-auto-columns: minmax(0, 1fr);
  gap: 1rem;
  align-items: start;
}
.node {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.4rem;
  text-align: center;
}
.dot {
  width: 0.65rem;
  height: 0.65rem;
  border-radius: 999px;
  background: var(--temporal-green);
  box-shadow: 0 0 0 4px rgba(89, 253, 160, 0.18);
  margin-bottom: 0.3rem;
}
.date {
  font-variant-numeric: tabular-nums;
  letter-spacing: 0.08em;
  color: var(--temporal-text-strong);
  font-weight: 500;
  font-size: 0.95rem;
}
.label {
  color: var(--temporal-text);
  font-size: 0.85rem;
  font-weight: 300;
  line-height: 1.35;
}
</style>
