<!--
  ABOUTME: Three-stage release explainer. PPT slide 42 (PRE-RELEASE → PUBLIC PREVIEW → GA).
  ABOUTME: Each stage gets a slot for its description.

  Frontmatter usage:
    ---
    layout: release-stages
    title: How we release capabilities
    ---

    ::pre-release::
    Self-Hosted: Enable env or Labs mode. Cloud: Invite only for Design Partners.

    ::public-preview::
    New features in Public Preview are available to everyone. Documentation is available.

    ::general-availability::
    Fully developed, tested, and available for use without further anticipated changes.
-->
<script setup lang="ts">
import TemporalFooter from '../components/TemporalFooter.vue'
import ReleaseStageChip from '../components/ReleaseStageChip.vue'

defineProps<{ title?: string }>()
</script>

<template>
  <div class="slidev-layout release-stages bg-grid">
    <h2 v-if="title" class="title">{{ title }}</h2>
    <div class="stages">
      <section class="stage">
        <ReleaseStageChip stage="pre-release" />
        <div class="body"><slot name="pre-release" /></div>
      </section>
      <section class="stage">
        <ReleaseStageChip stage="public-preview" />
        <div class="body"><slot name="public-preview" /></div>
      </section>
      <section class="stage">
        <ReleaseStageChip stage="general-availability" />
        <div class="body"><slot name="general-availability" /></div>
      </section>
    </div>
    <TemporalFooter />
  </div>
</template>

<style scoped>
.release-stages {
  display: flex;
  flex-direction: column;
  height: 100%;
}
.title {
  margin: 0 0 1.4rem;
  font-size: 2rem;
  font-weight: 200;
  color: var(--temporal-text-strong);
}
.stages {
  flex: 1 1 auto;
  min-height: 0;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1.5rem;
}
.stage {
  display: flex;
  flex-direction: column;
  gap: 0.9rem;
  padding-top: 0.5rem;
  border-top: 1px solid var(--temporal-border-hi);
}
.body {
  color: var(--temporal-text);
  font-size: 1rem;
  line-height: 1.5;
}
.body :deep(p) {
  margin: 0.3rem 0;
}
</style>
