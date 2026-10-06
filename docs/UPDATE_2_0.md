# FUTO Theme Studio 2.0

- Mobile-first Material 3 visual editor with top navigation.
- Live Arabic and English keyboard previews.
- Separate Arabic/English font selection and preview. The FUTO theme format currently exposes one global `[options.font]`; export therefore selects which language font becomes the FUTO font while keeping the other font available to the Studio.
- 1024 deterministic procedural button shapes: 768 2D + 256 3D.
- Per-group shape selection and per-key shape overrides.
- Per-key and per-group images.
- Solid and gradient colors, light/dark presets, background image, and HEX palette import from a URL.
- ZIP export writes TOML numeric values using `Locale.US`, preventing Arabic decimal digits/separators such as `٠٫٣٦٧` that caused FUTO's TOML parser to reject the previous export.
- ZIP validation before export.
- Lazy shape grid and lighter font library cards to reduce UI stalls.
