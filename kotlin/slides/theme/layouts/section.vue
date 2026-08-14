<!--
  Section divider. One big ExtraLight phrase centered on the purple-planet bg.
  Use for "Exercise N", "Architecture", "Wrap-Up"-style breaks.
-->
<template>
  <div class="slidev-layout section bg-planet-purple">
    <div class="section-inner">
      <slot />
    </div>
  </div>
</template>

<style scoped>
.section {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 3rem;
  position: relative;
}
.section::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, rgba(11, 16, 32, 0.35) 0%, rgba(11, 16, 32, 0.55) 100%);
  pointer-events: none;
}
.section-inner {
  position: relative;
  text-align: center;
  max-width: 22ch;
}
.section :deep(h1) {
  font-size: 3.4rem;
  font-weight: 200;
  letter-spacing: -0.02em;
  color: #ffffff;
  margin: 0;
  line-height: 1.05;
}
.section :deep(h2) {
  color: var(--temporal-lavender);
  font-weight: 300;
  margin-top: 0.8rem;
}
.section :deep(p) {
  color: var(--temporal-text);
  margin-top: 1rem;
}
</style>
