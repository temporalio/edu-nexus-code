<!--
  Exercise timer layout: "N min" chip plus heading plus instruction slot.
  `title` is reserved by Slidev for doc metadata, so we use `heading`.
  Example:
    ---
    layout: exercise
    minutes: 15
    heading: Exercise 1
    ---
-->
<script setup lang="ts">
defineProps({
  minutes: { type: [Number, String], default: '' },
  heading: { type: String, default: '' },
})
</script>

<template>
  <div class="slidev-layout exercise bg-glow">
    <div class="exercise-inner">
      <div class="timer">
        <span class="minutes">{{ minutes }}</span>
        <span class="unit">min</span>
      </div>
      <h1 v-if="heading" class="exercise-title">{{ heading }}</h1>
      <div class="exercise-body">
        <slot />
      </div>
    </div>
  </div>
</template>

<style scoped>
.exercise {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 3rem;
  position: relative;
}
.exercise-inner {
  position: relative;
  text-align: center;
  max-width: 30ch;
}
.timer {
  display: inline-flex;
  align-items: baseline;
  gap: 0.45rem;
  color: var(--temporal-green);
  font-weight: 300;
  padding: 0.45rem 1.1rem;
  border: 1px solid rgba(89, 253, 160, 0.35);
  border-radius: 999px;
  margin-bottom: 1.6rem;
}
.minutes {
  font-size: 1.6rem;
  font-weight: 200;
  color: #ffffff;
}
.unit {
  font-size: 0.9rem;
  color: var(--temporal-text);
  text-transform: uppercase;
  letter-spacing: 0.1em;
}
.exercise-title {
  font-size: 2.8rem;
  font-weight: 200;
  letter-spacing: -0.02em;
  color: #ffffff;
  margin: 0 0 1rem;
  line-height: 1.05;
}
.exercise-body :deep(p) {
  color: var(--temporal-text);
  font-size: 1.05rem;
  line-height: 1.55;
  margin: 0.4rem 0;
}
.exercise-body :deep(strong) {
  color: #ffffff;
}
</style>
