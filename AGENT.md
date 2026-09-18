# Create Delight Core 1.21.1

This repository is the NeoForge 1.21.1 port of Create Delight Core.

## Working rules

- Use normal NeoForge 1.21.1 APIs; do not copy old Forge/Registrate code verbatim.
- Preserve optional-mod safety: a missing optional mod must not crash CDC.
- Resolve dependencies from Maven or CurseMaven, never from local pack jars.
- Prefer public source or sources jars; otherwise inspect only the required classes with `javap` or Vineflower.
- Preserve unrelated worktree changes and do not reset or revert them.

## Local references

- Prism Launcher is installed through APT; its data directory is `/home/halo/.local/share/PrismLauncher`.
- CDPR instance: `/home/halo/.local/share/PrismLauncher/instances/CDPR/minecraft`
- CDPR mods: `/home/halo/.local/share/PrismLauncher/instances/CDPR/minecraft/mods`
- Old pack migration reference (PTTOD): `/home/halo/.local/share/PrismLauncher/instances/CDPR/0488/tmp488/PTTOD`. This is the remaining migration queue: migrate and verify one item, then delete only its corresponding old reference content. Missing content may already have been migrated; this directory is not a complete baseline snapshot.
- Core 1.20.1 source baseline: `/home/halo/gitRepo/JSI/Create-Delight-Core-1.20.1-baseline-8b26efc`. This detached worktree is pinned to `8b26efcb520ad3ad5c04fe8db0290bc1059d2119` (Core `2.2.14`, Minecraft `1.20.1`, Forge `47.4.10`). Keep it intact for comparison; do not advance it to the latest `1.20.1` branch or apply the PTTOD deletion workflow to it.
- Quality Food fork: `/home/halo/gitRepo/JSI/quality_food_j`
- Vineflower: no local checkout has been located on this device; obtain it when needed instead of using the obsolete Desktop path.

## Migration ledger

Use `docs/status.md` as the only migration ledger. Do not create another status or handoff document.

Quality Food harvest, crafting, and machine propagation belongs in the `quality_food` CDPR fork. CDC keeps only its own integration, such as the quality absorber, living-drop cleanup, and Lightman's Currency exchange behavior.

## Verification

Use the complete SDKMAN JDK 21 at `/home/halo/.sdkman/candidates/java/21.0.2-graalce` for this project; both `java` and `javac` are installed there. SDKMAN currently defaults to JDK 25, which is incompatible with the Gradle 8.8 wrapper, and this repository has no `.sdkmanrc` to select JDK 21 automatically. The separate APT directory `/usr/lib/jvm/java-21-openjdk-amd64` contains only a JRE and must not be used as the build JDK. Invoke the wrapper through `bash` because it currently lacks executable permission.

Run at least:

```bash
rtk proxy env JAVA_HOME=/home/halo/.sdkman/candidates/java/21.0.2-graalce bash ./gradlew compileJava
```

Run the same command with `build` instead of `compileJava` when resources or metadata change.
