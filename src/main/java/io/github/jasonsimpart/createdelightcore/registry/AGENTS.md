# Registry Rules

- Registrations must flow through `CreateDelightCore.REGISTRATE`.
- Keep registry ids stable and under the `createdelightcore` namespace unless compatibility with another namespace explicitly requires resources there.
- Add new registry entries in the focused registry class: `CDBlocks`, `CDItems`, `CDFluids`, `CDRecipeTypes`, `CDTags`, menus, tabs, or block entities.
- Keep object implementation classes under `content/` or `compat/`; registry classes should describe registration, not own complex behavior.
- When registrations affect generated assets, tags, recipes, loot, or language, update the owning datagen provider and run `./gradlew runData --no-daemon`.
- After changing registrations, run `./gradlew build --no-daemon` because Forge registry errors often surface at compile or data-load time.
