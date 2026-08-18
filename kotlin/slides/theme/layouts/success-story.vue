<!--
  ABOUTME: Customer success story card. PPT slides 55–56 (Dust, ZoomInfo).
  ABOUTME: Logo + intro on top; "challenges" and "solution" two-column body.

  Frontmatter usage:
    ---
    layout: success-story
    eyebrow: SUCCESS STORY
    companyName: ZoomInfo
    headline: AI-driven sales product to predict the next best customer
    logo: /path/to/logo.svg          # optional; falls back to placeholder
    ---

    Optional intro paragraph (renders below the headline).

    ::challenges::
    - High scale: billions of signals
    - Complex, fragile architecture

    ::solution::
    - Temporal orchestrates RAG for Account Overviews + Summaries
    - More reliable; reduced LLM costs
-->
<script setup lang="ts">
import TemporalFooter from '../components/TemporalFooter.vue'

defineProps<{
  eyebrow?: string
  companyName: string
  headline?: string
  logo?: string
}>()
</script>

<template>
  <div class="slidev-layout success-story bg-grid">
    <header class="story-header">
      <div class="logo-slot">
        <img v-if="logo" :src="logo" :alt="companyName" />
        <div v-else class="asset-pending">Customer logo · spec §5.14</div>
      </div>
      <div class="header-text">
        <p v-if="eyebrow" class="eyebrow">{{ eyebrow }}</p>
        <h1 class="company">{{ companyName }}</h1>
        <p v-if="headline" class="headline">{{ headline }}</p>
        <slot />
      </div>
    </header>

    <div class="story-grid">
      <section class="cell">
        <h3>Challenges</h3>
        <slot name="challenges" />
      </section>
      <section class="cell">
        <h3>Solution</h3>
        <slot name="solution" />
      </section>
    </div>

    <TemporalFooter />
  </div>
</template>

<style scoped>
.success-story {
  display: flex;
  flex-direction: column;
  height: 100%;
  gap: 1rem;
}
.story-header {
  display: grid;
  grid-template-columns: 10rem 1fr;
  gap: 1.5rem;
  align-items: center;
}
.logo-slot {
  width: 10rem;
  min-height: 5rem;
  display: flex;
  align-items: center;
  justify-content: center;
}
.logo-slot img {
  max-width: 100%;
  max-height: 5rem;
}
.eyebrow {
  margin: 0 0 0.2rem;
  font-size: 0.75rem;
  letter-spacing: 0.2em;
  text-transform: uppercase;
  color: var(--temporal-green);
}
.company {
  margin: 0;
  font-size: 2rem;
  font-weight: 200;
  letter-spacing: -0.02em;
  color: var(--temporal-text-strong);
}
.headline {
  margin: 0.4rem 0 0;
  color: var(--temporal-text);
  font-size: 1.05rem;
}
.story-grid {
  flex: 1 1 auto;
  min-height: 0;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1.5rem;
}
.cell {
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
  padding-top: 0.7rem;
  border-top: 1px solid var(--temporal-border-hi);
}
.cell h3 {
  margin: 0;
  font-size: 0.95rem;
  font-weight: 500;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: var(--temporal-lavender);
}
.cell :deep(p),
.cell :deep(li) {
  color: var(--temporal-text);
  font-size: 0.95rem;
  line-height: 1.45;
}
</style>
