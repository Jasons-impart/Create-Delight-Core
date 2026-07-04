# Compatibility Rules

- Non-invasive mod integration belongs in `compat/`; bytecode or target-method patches belong in `mixin/`.
- Keep each mod integration in its existing subpackage such as `jei`, `jade`, `cmr`, `createmetallurgy`, or `fruitsdelight`.
- Check dependency versions in `build.gradle` before changing integration logic because CDC targets exact mod behavior.
- JEI primary Maven is intentionally disabled in `build.gradle`; do not re-enable it as a routine dependency fix.
- For Fruit Delight custom jelly / jello fruit, use `CustomFDFruits` as the synthetic lookup layer; see `docs/lessons-learned.md`.
- When compatibility code changes generated recipes, tags, or language, update the datagen provider and run `./gradlew runData --no-daemon`.
