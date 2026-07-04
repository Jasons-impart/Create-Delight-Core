# Datagen Rules

- Datagen providers live under `src/main/java/io/github/jasonsimpart/createdelightcore/data/`.
- Generated output in `src/generated/resources/` is source for this project because Gradle includes it in main resources.
- Prefer editing providers over hand-editing generated files when a provider owns the output.
- Language generation belongs in `data/lang`; keep Chinese and English handler behavior aligned when adding translated entries.
- Recipe generation belongs in `data/recipe`; reuse existing provider helpers instead of duplicating JSON by hand.
- After changing providers, run `./gradlew runData --no-daemon` and inspect the generated diff before building.
