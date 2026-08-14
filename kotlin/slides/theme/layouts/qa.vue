<!--
  ABOUTME: Minimal Q&A divider. PPT slide 59 equivalent.
  ABOUTME: Centered headline over the planet-purple backdrop. No footer.
-->
<template>
  <div class="slidev-layout qa bg-planet-purple">
    <div class="qa-inner">
      <slot>
        <h1>Q&amp;A</h1>
      </slot>
    </div>
  </div>
</template>

<style scoped>
.qa {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 3rem;
  position: relative;
}
.qa::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, rgba(11, 16, 32, 0.35) 0%, rgba(11, 16, 32, 0.55) 100%);
  pointer-events: none;
}
.qa-inner {
  position: relative;
  text-align: center;
}
.qa :deep(h1) {
  font-size: 5rem;
  font-weight: 200;
  letter-spacing: -0.02em;
  color: #ffffff;
  margin: 0;
}
.qa :deep(p) {
  color: var(--temporal-text);
  margin-top: 1rem;
  font-size: 1.1rem;
}
</style>
