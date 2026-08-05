# CDC MOD

Create Delight Core（`createdelightcore`）是 Create-Delight Remake 的 Forge 1.20.1 自定义模组。

## Essentials

- Java 17；Forge `47.4.16`；ForgeGradle `[6.0,6.2)`；Parchment mappings。
- 入口：`src/main/java/io/github/jasonsimpart/createdelightcore/CreateDelightCore.java`。
- 版本唯一来源：`gradle.properties` 的 `mod_version`；其必须以数字开头。
- 构建：`./gradlew build --no-daemon`；数据生成：`./gradlew runData --no-daemon`。
- 运行客户端/服务端：`./gradlew runClient --no-daemon` / `./gradlew runServer --no-daemon`。
- 产物：`build/libs/CDC-mod-src-1.20.1-<mod_version>.jar`；向父整合包提供非 `-all` 的 reobf jar。

## Where to work

- 注册：`src/main/java/io/github/jasonsimpart/createdelightcore/registry/`
- 内容与逻辑：`src/main/java/io/github/jasonsimpart/createdelightcore/content/`
- 兼容、Mixin、数据生成：同级的 `compat/`、`mixin/`、`data/`
- 资源与手写数据：`src/main/resources/`；生成资源：`src/generated/resources/`

进入 `registry/`、`compat/`、`mixin/`、`data/` 或 `src/main/resources/` 前，先阅读其中的 `AGENTS.md`。稳定约束留在此文件；实现地图和 how-to 在 `docs/dev-knowledge/`，历史问题在 `docs/lessons-learned.md`，避免重复记录。

## Hard rules

- 注册经 `CreateDelightCore.REGISTRATE` 与 `registry/` 中的类完成。
- 新增 Mixin 必须同步登记到 `src/main/resources/mixins.createdelightcore.json`。
- `src/generated/resources/` 是主资源集的一部分，不能当作可忽略构建产物。
- `META-INF/mods.toml` 与 `pack.mcmeta` 从 `gradle.properties` 展开占位符。
- 父仓更新 CDC 子模块指针时，必须核对源码版本与打包的 CDC jar 一致。
- CDC 任务不得顺带编辑父整合包的 Packwiz、CI 或根知识库文件。

## Git and maintenance

- `1.20.1` 是受保护分支：从最新 `origin/1.20.1` 新建英文或拼音分支，走 PR 合并，禁止直接推送。
- 提交信息用中文，并在正文说明改动、原因、影响与验证；提交前检查 diff 并运行与改动匹配的验证。
- 为避免上下文膨胀，此文件保持为入口和硬约束；领域细节写入对应目录的 `AGENTS.md` 或上述知识文档。

## Dependency note

`build.gradle` 同时使用远程 Maven 与 `libs/` 本地依赖；网络受阻时依赖解析可能失败。JEI 主 Maven 被有意禁用；Central Kitchen 仅在需要 `runData` 时按 `build.gradle` 注释临时调整，并在完成后恢复。
