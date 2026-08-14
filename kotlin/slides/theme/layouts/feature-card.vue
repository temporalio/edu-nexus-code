<!--
  ABOUTME: Feature release card. PPT slides 29–31 ("Feature Name" + release-stage chip).
  ABOUTME: Three body slots: "big-picture", "what-youll-get", "later".

  Frontmatter usage:
    ---
    layout: feature-card
    featureTheme: FEATURE THEME
    featureName: Feature Name
    stage: public-preview                # pre-release | public-preview | general-availability
    target: Public Preview target H1 2025
    ---

    ::big-picture::
    Describes the problem this feature solves.

    ::what-youll-get::
    The what's-in-it-for-me bullet.

    ::later::
    What's planned for future releases.
-->
<script setup lang="ts">
import TemporalFooter from '../components/TemporalFooter.vue'
import ReleaseStageChip, { type ReleaseStage } from '../components/ReleaseStageChip.vue'

defineProps<{
  featureTheme?: string
  featureName: string
  stage: ReleaseStage
  target?: string
}>()
</script>

<template>
  <div class="slidev-layout feature-card bg-grid">
    <header class="feature-header">
      <div class="feature-meta">
        <p v-if="featureTheme" class="theme">{{ featureTheme }}</p>
        <h1 class="name">{{ featureName }}</h1>
      </div>
      <ReleaseStageChip :stage="stage" />
    </header>

    <div class="feature-grid">
      <section class="cell">
        <h3>Big Picture</h3>
        <slot name="big-picture" />
      </section>
      <section class="cell">
        <h3>What You'll Get</h3>
        <slot name="what-youll-get" />
      </section>
      <section class="cell">
        <h3>Later</h3>
        <slot name="later" />
      </section>
    </div>

    <p v-if="target" class="target">{{ target }}</p>
    <TemporalFooter />
  </div>
</template>

<style scoped>
.feature-card {
  display: flex;
  flex-direction: column;
  height: 100%;
  gap: 1.2rem;
}
.feature-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 2rem;
}
.theme {
  margin: 0 0 0.25rem;
  font-size: 0.8rem;
  letter-spacing: 0.2em;
  text-transform: uppercase;
  color: var(--temporal-green);
}
.name {
  margin: 0;
  font-size: 2.4rem;
  font-weight: 200;
  letter-spacing: -0.02em;
  color: var(--temporal-text-strong);
}
.feature-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1.5rem;
  flex: 1 1 auto;
  min-height: 0;
}
.cell {
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
  border-top: 1px solid var(--temporal-border-hi);
  padding-top: 0.8rem;
}
.cell h3 {
  margin: 0;
  color: var(--temporal-lavender);
  font-weight: 500;
  font-size: 1rem;
  text-transform: uppercase;
  letter-spacing: 0.1em;
}
.cell :deep(p) {
  color: var(--temporal-text);
  font-size: 1rem;
  margin: 0;
  line-height: 1.45;
}
.target {
  margin: 0;
  text-align: right;
  font-size: 0.85rem;
  color: var(--temporal-text-muted);
  letter-spacing: 0.06em;
}
</style>
