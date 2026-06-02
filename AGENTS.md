# CDC MOD KNOWLEDGE BASE

Create Delight Core (CDC) is the custom Forge Java mod for Create-Delight Remake.

## STACK

- Minecraft: `1.20.1`
- Forge: `47.4.10`
- Java toolchain: `17`
- Build: Gradle wrapper + ForgeGradle `[6.0,6.2)` + Parchment mappings
- Mod id: `createdelightcore`
- Main class: `src/main/java/io/github/jasonsimpart/createdelightcore/CreateDelightCore.java`
- Version source: `gradle.properties` -> `mod_version`

## STRUCTURE

| Path | Purpose |
|------|---------|
| `src/main/java/io/github/jasonsimpart/createdelightcore/registry/` | Registries for items, blocks, fluids, tabs, recipes, tags |
| `src/main/java/io/github/jasonsimpart/createdelightcore/content/` | Blocks, items, fluids, recipes, events, contraption logic |
| `src/main/java/io/github/jasonsimpart/createdelightcore/compat/` | Integration logic for JEI, Jade, CMR, Create Metallurgy, etc. |
| `src/main/java/io/github/jasonsimpart/createdelightcore/mixin/` | Compatibility and behavior patches for Minecraft and other mods |
| `src/main/java/io/github/jasonsimpart/createdelightcore/data/` | Data generation providers and language handlers |
| `src/main/resources/assets/createdelightcore/` | Textures, models, blockstates, OptiFine CIT assets |
| `src/main/resources/data/` | Hand-written datapack resources for CDC and compat namespaces |
| `src/generated/resources/` | Generated assets/data included by `sourceSets.main.resources` |
| `libs/` | Local flatDir dependency jars used by Gradle |

## COMMANDS

```bash
./gradlew build --no-daemon
./gradlew runData --no-daemon
./gradlew runClient --no-daemon
./gradlew runServer --no-daemon
```

- Build output in this parent checkout: `build/libs/CDC-mod-src-1.20.1-<mod_version>.jar`.
- Use the non-`-all` jar for the modpack unless packaging tooling explicitly says otherwise.
- `jar` is finalized by `reobfJar`, so `build` produces the reobfuscated runtime jar.

## DEVELOPMENT RULES

- Keep `gradle.properties` `mod_version` aligned with the Create-Delight-Core jar used by the parent modpack.
- When updating the parent submodule pointer, verify the CDC source version and packaged CDC mod version are the same.
- Registrations flow through `CreateDelightCore.REGISTRATE` and the classes in `registry/`.
- New mixin classes must also be listed in `src/main/resources/mixins.createdelightcore.json`.
- Mixin targets are version-sensitive; verify against the exact dependency versions declared in `build.gradle`.
- Access transformers live at `src/main/resources/META-INF/accesstransformer.cfg` and are wired in `build.gradle`.
- Datagen writes to `src/generated/resources`; rerun `./gradlew runData --no-daemon` after changing providers.
- `src/generated/resources` is source for this project because the Gradle build includes it.
- Resource metadata expands placeholders from `gradle.properties` in `META-INF/mods.toml` and `pack.mcmeta`.
- Do not edit parent packwiz, CI workflow, or root knowledge files from inside this submodule task.

## NOTES

- `build.gradle` uses network Maven repositories plus local `libs/`; dependency resolution may fail if Maven hosts are blocked.
- The JEI primary Maven is intentionally disabled in `build.gradle` because it can return HTTP 523 in some regions.
- Central Kitchen is documented in `build.gradle`: switch it to `compileOnly` only when needed for `runData`, then restore implementation.
