# CDC Technical How-To Index

This file stores compact notes for common CDC change types. If a workflow grows into a command-heavy or fragile procedure, move it into a dedicated skill or script and link it here.

| Goal | Edit locations | Checklist | Validation |
|---|---|---|---|
| Add or change a registered object | `registry/`, then the matching implementation under `content/` or `compat/` | Use `CreateDelightCore.REGISTRATE`; keep ids under `createdelightcore`; update tags, lang, models, recipes, loot, and tabs through existing providers when possible. | `./gradlew runData --no-daemon` after provider changes, then `./gradlew build --no-daemon`. |
| Add or change datagen output | `data/`, `src/generated/resources/` | Wire providers through existing datagen entry points; treat generated resources as source; avoid hand-editing generated output when the provider owns it. | `./gradlew runData --no-daemon`; inspect generated diff. |
| Add a mixin | `mixin/<target-mod>/`, `src/main/resources/mixins.createdelightcore.json` | Verify the target against dependency versions in `build.gradle`; keep accessors and behavioral patches small; add the class name to the mixin JSON. | `./gradlew build --no-daemon`; run client/server when the target path is runtime-sensitive. |
| Add hand-written datapack or asset resources | `src/main/resources/assets/createdelightcore/`, `src/main/resources/data/` | Put resources in the owning namespace; do not duplicate datagen-owned files; keep metadata placeholders compatible with `gradle.properties`. | `./gradlew build --no-daemon`; run client for visual/resource changes when practical. |
| Add Fruit Delight custom jelly / jello fruit | `registry/CDBlocks.java`, `compat/fruitsdelight/`, `mixin/fruitsdelight/` | Register the jelly bottle, jelly block, and jello block; register synthetic fruit through `CustomFDFruits`; keep mixins generic; do not append `FruitType.values()` or hand-write jelly / jams tag JSON. | `./gradlew runData --no-daemon`, then `./gradlew build --no-daemon`. |

## Entry Template

| Goal | Edit locations | Checklist | Validation |
|---|---|---|---|
| `<task>` | `<paths>` | `<short checklist>` | `<commands/checks>` |
