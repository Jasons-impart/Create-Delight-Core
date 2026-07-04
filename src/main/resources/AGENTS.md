# Resource Rules

- `src/main/resources/assets/createdelightcore/` owns CDC textures, models, blockstates, GUI textures, and OptiFine CIT assets.
- `src/main/resources/data/` owns hand-written datapack resources for CDC and compatibility namespaces.
- Do not duplicate files that are generated into `src/generated/resources/`; edit the provider instead.
- `META-INF/mods.toml` and `pack.mcmeta` expand placeholders from `gradle.properties`, so keep placeholder names aligned with Gradle properties.
- New mixin classes must be added to `mixins.createdelightcore.json` in this directory.
- Access transformer entries live in `META-INF/accesstransformer.cfg` and are wired in `build.gradle`.
- Validate resource changes with `./gradlew build --no-daemon`; run client for visual assets when practical.
