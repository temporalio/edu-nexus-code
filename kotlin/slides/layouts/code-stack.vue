<!--
  ABOUTME: Code above, definition below, each spanning the full slide width.
  ABOUTME: Both panes auto-shrink to fit, so a slide never scrolls or clips.

  `title` is reserved by Slidev for slide metadata and never reaches a layout as
  a prop, so this uses `heading` — same workaround as the theme's exercise layout.

  Frontmatter usage:
    ---
    layout: code-stack
    heading: Endpoint
    proseMax: 55
    ---

    ::code::

    ```bash
    temporal operator nexus endpoint create --name compliance-endpoint
    ```

    ::default::

    A name pointing at a Namespace and a Task Queue.

    - Callers only need the Endpoint name
    - One Endpoint targets one Namespace and Task Queue

  The definition takes only the height it needs and the code gets everything
  left over, so a one-line definition hands its slack to the code rather than
  reserving empty space. `proseMax` caps the definition as a percentage of the
  area below the heading (default 40) so a long one cannot squeeze the code out.
  Raise it on a bullet-heavy slide.

  Both panes measure themselves after render and step their font size down until
  the content fits, so nothing scrolls or clips. If a pane shrinks past
  legibility, cut content or move it to a second slide — that is a content
  problem, not a layout one. `minCodeSize` and `minProseSize` set the floors in rem.
-->
<script setup lang="ts">
import { nextTick, onBeforeUnmount, onMounted, ref, watch } from "vue";
import TemporalFooter from "../theme/components/TemporalFooter.vue";

const props = withDefaults(
  defineProps<{
    heading?: string;
    proseMax?: number | string;
    minCodeSize?: number;
    minProseSize?: number;
  }>(),
  {
    proseMax: 40,
    minCodeSize: 0.55,
    minProseSize: 0.55,
  },
);

const MAX_CODE_SIZE = 1.25; // matches the theme's default, in rem
const MAX_PROSE_SIZE = 0.95;
const STEP = 0.05;

const codePane = ref<HTMLElement | null>(null);
const prosePane = ref<HTMLElement | null>(null);
const codeSize = ref(MAX_CODE_SIZE);
const proseSize = ref(MAX_PROSE_SIZE);
let resizeObserver: ResizeObserver | undefined;
let mutationObserver: MutationObserver | undefined;
let scheduled = false;
let fitting = false;

function overflowsY(el: HTMLElement) {
  return el.scrollHeight > el.clientHeight + 1;
}

function overflowsX(el: HTMLElement) {
  // Width has to be measured on the <pre>, not the pane: Slidev gives code
  // blocks their own overflow-x, so a long line scrolls inside the pre and
  // never widens its container.
  const pre = el.querySelector("pre");
  return pre ? pre.scrollWidth > pre.clientWidth + 1 : false;
}

async function shrinkToFit(
  el: HTMLElement | null,
  size: { value: number },
  max: number,
  min: number,
  overflows: (el: HTMLElement) => boolean,
) {
  if (!el) return;
  size.value = max;
  await nextTick();
  while (size.value > min && overflows(el)) {
    size.value = Math.round((size.value - STEP) * 100) / 100;
    await nextTick();
  }
}

async function fit() {
  if (fitting) return;
  fitting = true;
  try {
    await Promise.all([
      shrinkToFit(
        codePane.value,
        codeSize,
        MAX_CODE_SIZE,
        props.minCodeSize,
        (el) => overflowsY(el) || overflowsX(el),
      ),
      shrinkToFit(
        prosePane.value,
        proseSize,
        MAX_PROSE_SIZE,
        props.minProseSize,
        overflowsY,
      ),
    ]);
  } finally {
    fitting = false;
  }
}

// Coalesce bursts of observer callbacks into one fit on the next frame.
function scheduleFit() {
  if (scheduled) return;
  scheduled = true;
  requestAnimationFrame(() => {
    scheduled = false;
    fit();
  });
}

onMounted(() => {
  fit();

  const panes = [codePane.value, prosePane.value].filter(
    (el): el is HTMLElement => !!el,
  );

  if (typeof ResizeObserver !== "undefined") {
    resizeObserver = new ResizeObserver(scheduleFit);
    panes.forEach((el) => resizeObserver!.observe(el));
  }

  // A ResizeObserver alone is not enough. Editing a snippet swaps the pane's
  // contents without changing its box, so nothing resizes and the old font size
  // sticks while the new content clips. Watch the subtree for content changes
  // too. Attributes are excluded so our own font-size writes cannot loop.
  if (typeof MutationObserver !== "undefined") {
    mutationObserver = new MutationObserver(scheduleFit);
    panes.forEach((el) =>
      mutationObserver!.observe(el, {
        childList: true,
        subtree: true,
        characterData: true,
      }),
    );
  }
});

onBeforeUnmount(() => {
  resizeObserver?.disconnect();
  mutationObserver?.disconnect();
});

watch(() => [props.proseMax, props.minCodeSize, props.minProseSize], fit);
</script>

<template>
  <div class="slidev-layout code-stack bg-grid">
    <h2 v-if="heading" class="heading">{{ heading }}</h2>
    <div
      class="cs-grid"
      :style="{
        gridTemplateRows: `minmax(0, 1fr) fit-content(${proseMax}%)`,
      }"
    >
      <div
        ref="codePane"
        class="code-pane"
        :style="{ '--cs-code-size': `${codeSize}rem` }"
      >
        <slot name="code" />
      </div>
      <div
        ref="prosePane"
        class="prose-pane"
        :style="{ '--cs-prose-size': `${proseSize}rem` }"
      >
        <slot />
      </div>
    </div>
    <TemporalFooter />
  </div>
</template>

<style scoped>
.code-stack {
  display: flex;
  flex-direction: column;
  height: 100%;
}
.heading {
  margin: 0 0 0.7rem;
  font-size: 1.8rem;
  font-weight: 200;
  color: var(--temporal-text-strong);
}
.cs-grid {
  flex: 1 1 auto;
  min-height: 0;
  display: grid;
  /* Rows are set inline: minmax(0, 1fr) fit-content(<proseMax>%).
     Code claims the slack; the definition takes only what it needs, capped at
     proseMax of the grid height.

     Two traps here. The prose track cannot be minmax(0, auto) — with a zero
     floor its min-content contribution is zero and the greedy 1fr code row
     collapses it. And the cap cannot be max-height on the pane: a percentage
     max-height on a grid item resolves against its own track, so capping an
     auto-sized track at 40% collapses it to 40% of its content. fit-content()
     resolves its percentage against the grid container, which is what we want. */
  gap: 0.8rem;
  overflow: hidden;
}
.code-pane {
  min-height: 0;
  overflow: hidden;
}
.code-pane :deep(pre.shiki),
.code-pane :deep(pre code) {
  font-size: var(--cs-code-size);
  line-height: 1.35;
}
.code-pane :deep(pre) {
  margin: 0;
}

.prose-pane {
  /* No min-height: 0 here — see the grid-template-rows note above. */
  overflow: hidden;
}
.prose-pane :deep(p),
.prose-pane :deep(li) {
  font-size: var(--cs-prose-size);
  line-height: 1.45;
  max-width: 92ch;
}
.prose-pane :deep(p) {
  margin: 0.3rem 0;
}
.prose-pane :deep(p:first-child) {
  margin-top: 0;
}
.prose-pane :deep(ul),
.prose-pane :deep(ol) {
  margin: 0.3rem 0 0;
  padding-left: 1.1em;
}
.prose-pane :deep(li) {
  margin: 0.15rem 0;
}
.prose-pane :deep(li)::marker {
  color: var(--temporal-green);
}
.prose-pane :deep(code) {
  font-size: 0.95em;
}
.prose-pane :deep(strong) {
  color: var(--temporal-text-strong);
}
</style>
