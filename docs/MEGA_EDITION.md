# Mega Edition inventory

## Shapes

The studio generates **720 selectable shape definitions** at runtime:

- 480 2D variants: 30 geometric families × 16 parameter variants.
- 240 3D variants: 12 geometric families × 20 extrusion/rim variants.
- Every selected shape is rendered to a real PNG border asset when exported.
- A shape can be assigned to a whole group or to one individual key.

Groups:

1. Writing/letter keys
2. Top functional bar
3. Bottom row
4. Functional keys
5. Action/send keys
6. Spacebar
7. Special/popup keys

Individual key overrides have higher priority than group rules in the generated `theme.txt`.

## Images

The editor supports:

- keyboard background image;
- individual key image;
- group image for each of the seven groups.

The images are embedded in the exported ZIP and referenced by ordered FUTO border match rules.

## Colors

Each group supports solid or gradient fill, two fill colors and a text color. The keyboard background supports solid or gradient fill as well.

## Fonts

The APK bundles 80 Noto font files for local preview/export:

- 40 Arabic-oriented Noto variants;
- 40 Latin-oriented Noto variants.

The user can also import a TTF/OTF file from the device. The selected font is previewed using the actual font file and embedded in the exported ZIP.

## FUTO format limitation

FUTO's observed `theme.txt` format exposes a general `scale_text` option rather than separate Arabic/Latin export scales. The studio therefore provides independent Arabic/Latin scale controls in the live preview while exporting the supported FUTO-wide `scale_text` value.
