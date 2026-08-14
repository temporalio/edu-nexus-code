// ABOUTME: Registers the `temporal` Shiki theme so fenced code blocks use the deck's palette.
// ABOUTME: Single theme — the Temporal brand is dark; no separate light variant.

import temporal from './temporal.json'

export default () => ({
  themes: {
    dark: temporal as any,
    light: temporal as any,
  },
})
