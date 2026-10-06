# Analysis of the supplied `animal-theme.zip`

The supplied theme is a real FUTO Keyboard theme package and contains 23 files:
- `theme.txt`
- 21 PNG border/icon assets
- `Manosque-Regular.ttf`
- `PetKeys-background.png` (the PNG entry contains WEBP-encoded image data despite its `.png` filename; Android image decoders can still identify it by content in many cases)

## `theme.txt` structure observed

The package uses FUTO Keyboard Theme Configuration format version 1.0 and configuration `version = 2`.

The file contains:
1. Metadata: `name`, `author`, `id`, `version`, `description`.
2. `[options]`: automatic borders, hint centering, roundedness, text/hint scale and weights.
3. `[colors]`: Material-style colors plus FUTO-specific keyboard colors such as `keyboard_surface`, `keyboard_container`, `keyboard_press`, and pressed-state colors.
4. `[options.font]`: a bundled TTF referenced by filename.
5. `[options.background]`: a bundled image, opacity, action-bar opacity and normalized crop rectangle.
6. Ordered `[[matchrules.border]]` rules. The supplied theme demonstrates selectors such as `normal`, `pressed`, `functional`, `action`, `spacebar`, `popup`, row/column selectors and `label` selectors.
7. `[[matchrules.icon]]` rules for delete, emoji, shift and enter.
8. `[[asset.border]]` records describing tint, padding, slicing, gap and target density.
9. `[[asset.icon]]` records describing icon density.

## Editor implementation

FUTO Theme Studio therefore does not create a proprietary theme format. It imports the real ZIP, reads `theme.txt`, lets the user edit the supported properties, generates/uses real image assets, and exports the same core FUTO structure as a ZIP.

The editor's preview is driven by the same editable theme state used by the exporter: colors, gradients, background, shape, radius, spacing, text scale, font, selected key and per-key image assignments.
