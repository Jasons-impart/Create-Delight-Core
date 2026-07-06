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

## KNOWLEDGE ROUTING

| Knowledge | Location |
|---|---|
| Stable CDC facts and repository-wide constraints | `AGENTS.md` |
| Implemented feature map and current technical how-to notes | `docs/dev-knowledge/` |
| Historical bugs, root causes, and workarounds | `docs/lessons-learned.md` |
| Registry rules | `src/main/java/io/github/jasonsimpart/createdelightcore/registry/AGENTS.md` |
| Datagen rules | `src/main/java/io/github/jasonsimpart/createdelightcore/data/AGENTS.md` |
| Compatibility rules | `src/main/java/io/github/jasonsimpart/createdelightcore/compat/AGENTS.md` |
| Mixin rules | `src/main/java/io/github/jasonsimpart/createdelightcore/mixin/AGENTS.md` |
| Resource and datapack rules | `src/main/resources/AGENTS.md` |

## STRUCTURE

| Path | Purpose |
|---|---|
| `src/main/java/io/github/jasonsimpart/createdelightcore/registry/` | Registries for items, blocks, fluids, tabs, recipes, tags |
| `src/main/java/io/github/jasonsimpart/createdelightcore/content/` | Blocks, items, fluids, recipes, events, contraption logic |
| `src/main/java/io/github/jasonsimpart/createdelightcore/compat/` | Integration logic for JEI, Jade, CMR, Create Metallurgy, Fruit Delight, etc. |
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

- Build output: `build/libs/CDC-mod-src-1.20.1-<mod_version>.jar`.
- Use the non-`-all` jar for the parent modpack unless packaging tooling explicitly says otherwise.
- `jar` is finalized by `reobfJar`, so `build` produces the reobfuscated runtime jar.

## GIT WORKFLOW

- Remote `1.20.1` is protected; never push changes to it directly.
- For every project commit, branch from the latest `origin/1.20.1` using an English or pinyin name such as `codex/fix-recipe-data`.
- Before committing, inspect the diff and run validation appropriate to the changed files.
- Commit messages must be written in Chinese.
- Commit messages must include a detailed body covering what changed, why it changed, impact scope, and validation performed.
- Push the working branch and merge back to `1.20.1` through a pull request.

## HARD RULES

- Keep `gradle.properties` `mod_version` aligned with the CDC jar used by the parent modpack.
- `mod_version` must start with a digit because Forge expands it into `META-INF/mods.toml` and rejects non-numeric-leading versions.
- When updating the parent submodule pointer, verify the CDC source version and packaged CDC mod version are the same.
- Registrations flow through `CreateDelightCore.REGISTRATE` and the classes in `registry/`.
- New mixin classes must also be listed in `src/main/resources/mixins.createdelightcore.json`.
- Datagen writes to `src/generated/resources`; this directory is source because Gradle includes it in main resources.
- Resource metadata expands placeholders from `gradle.properties` in `META-INF/mods.toml` and `pack.mcmeta`.
- Do not edit parent packwiz, CI workflow, or root knowledge files from inside this submodule task.

## NOTES

- `build.gradle` uses network Maven repositories plus local `libs/`; dependency resolution may fail if Maven hosts are blocked.
- The JEI primary Maven is intentionally disabled in `build.gradle` because it can return HTTP 523 in some regions.
- Central Kitchen is documented in `build.gradle`: switch it to `compileOnly` only when needed for `runData`, then restore `implementation`.

## KNOWLEDGE MAINTENANCE

- Keep root `AGENTS.md` at or below 150 lines; move domain details into focused files when it grows.
- Keep any subdirectory `AGENTS.md` at or below 80 lines; each subdirectory file should cover only that domain.
- Do not duplicate facts across knowledge files; link to the owning file instead.
- Put current rules in `AGENTS.md` files, technical how-to notes in `docs/dev-knowledge/`, and historical fixes in `docs/lessons-learned.md`.
- After fixing a non-obvious bug or workaround, add a short entry to `docs/lessons-learned.md`.
- This submodule currently has no knowledge-maintenance scripts; do not reference parent-pack scripts unless matching scripts are added here.
