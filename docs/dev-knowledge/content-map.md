# CDC Content Map

This file records implemented CDC features: what exists, how it behaves, where it lives, and its current status.

| Area | Visible or runtime behavior | Implementation outline | Main locations | Related notes | Status |
|---|---|---|---|---|---|
| Core registrations | CDC blocks, items, fluids, menus, recipe types, tags, and tabs register under `createdelightcore`. | Registries use `CreateDelightCore.REGISTRATE` and focused classes in `registry/`; content implementations live under `content/`. | `src/main/java/io/github/jasonsimpart/createdelightcore/registry/`, `src/main/java/io/github/jasonsimpart/createdelightcore/content/` | `src/main/java/io/github/jasonsimpart/createdelightcore/registry/AGENTS.md` | Active pattern. |
| Data generation | Generated assets/data are shipped with the mod because `src/generated/resources` is in main resources. | Providers are wired through `CDCoreDatagen` / `CDCoreDatagenEvent`; language uses `data/lang` handlers; recipe providers live under `data/recipe`. | `src/main/java/io/github/jasonsimpart/createdelightcore/data/`, `src/generated/resources/` | `src/main/java/io/github/jasonsimpart/createdelightcore/data/AGENTS.md` | Run after provider changes. |
| Compatibility patches | CDC integrates with JEI, Jade, Create Metallurgy, CMR, Fruit Delight, and many mod-specific behavior hooks. | Non-mixin integration belongs in `compat/`; invasive behavior changes belong in `mixin/` and must be listed in the mixin config. | `src/main/java/io/github/jasonsimpart/createdelightcore/compat/`, `src/main/java/io/github/jasonsimpart/createdelightcore/mixin/`, `src/main/resources/mixins.createdelightcore.json` | `src/main/java/io/github/jasonsimpart/createdelightcore/compat/AGENTS.md`, `src/main/java/io/github/jasonsimpart/createdelightcore/mixin/AGENTS.md` | Version-sensitive. |
| Fruit Delight custom fruit support | CDC can add jelly / jello fruit behavior without appending unsafe `FruitType.values()` entries. | Custom fruit lookup goes through `CustomFDFruits`; food classes live in `compat/fruitsdelight`; mixins stay generic and read stable fruit names from NBT. | `src/main/java/io/github/jasonsimpart/createdelightcore/compat/fruitsdelight/`, `src/main/java/io/github/jasonsimpart/createdelightcore/mixin/fruitsdelight/` | `docs/lessons-learned.md` | Known pitfall documented. |

## Entry Template

| Area | Visible or runtime behavior | Implementation outline | Main locations | Related notes | Status |
|---|---|---|---|---|---|
| `<feature>` | `<what changes at runtime>` | `<how it works>` | `<paths>` | `<docs or none>` | `<planned/active/needs validation>` |
