# Mixin Rules

- Mixin targets are version-sensitive; verify target classes and methods against the dependency versions declared in `build.gradle`.
- New mixin classes must be listed in `src/main/resources/mixins.createdelightcore.json`.
- Keep mixins narrow and prefer accessors or generic hooks when several content entries share one behavior.
- Put target-mod-specific mixins in the matching subpackage, for example `fruitsdelight`, `create`, `farmersdelight`, or `createmetallurgy`.
- Access transformers live at `src/main/resources/META-INF/accesstransformer.cfg`; use them only when a mixin or accessor is not the cleaner option.
- Validate mixin changes with `./gradlew build --no-daemon`; run client or server when the changed injection path is not covered by compilation.
- 修改品质相关 mixin 前先阅读 `docs/dev-knowledge/quality-food.md`。`QualityUtils.applyQuality(stack, quality)` 不会覆盖或降档已有品质，也不能把产物写成 `NONE`；共享配方模板必须先 `copy()`。
