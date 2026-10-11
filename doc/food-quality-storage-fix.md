# 食物存储转换品质修复

## 1. 问题与目标

Quality Food 为食物提供 `NONE`、`IRON`、`GOLD`、`DIAMOND` 等品质。食物被压成块、从块解包，或经过等价的存储转换时，输出应继承实际输入品质；它不应因为再次触发随机品质抽取而升级，也不应在输入栈最后一件物品被消耗后丢失品质。

本次 CDC 修复覆盖四条运行路径：

1. Create 工作盆（Basin）的压块和解包。
2. Create 机械合成器（`RecipeGridHandler`）的普通合成及回退合成路径。
3. Quark 合成器（`CrafterBlockEntity#getResult`）的真实结果生成路径。
4. Vintage Improvements 振动台解包路径。

修复只增加 CDC 的兼容 Mixin 和公共转换工具，没有修改 Quality Food、Create、Quark、Vintage Improvements、Tetra 或其它依赖的源码。

## 2. 版本和发布元数据

| 文件 | 修改内容 |
| --- | --- |
| `gradle.properties` | `mod_version` 从 `2.2.16k` 更新为 `2.2.16l`。 |
| `src/main/resources/META-INF/mods.toml` | 声明 Quality Food `>=2.4.3` 为必需依赖；声明 Quark `>=4.0`、Vintage Improvements `>=0.3.7.8` 为可选依赖，并使用 `AFTER` 加载顺序。 |
| `src/main/resources/mixins.createdelightcore.json` | 登记 Create、Quark、Vintage Improvements 三组新增 Mixin。 |
| `src/main/java/io/github/jasonsimpart/createdelightcore/mixin/CombatMixinPlugin.java` | 新增可选模组判断，只有目标模组和 Quality Food 同时存在时才应用对应 Mixin。 |

`mods.toml` 的 Quality Food 依赖是必需的，因为公共转换工具直接调用 Quality Food 2.4.3 提供的 `ServerConfig`、`StorageRecipeCache` 和 `QualityUtils` API。Quark 与 Vintage Improvements 仍为可选依赖；缺少它们时，伪 Mixin 不会应用。

## 3. 公共品质转换策略

文件：`src/main/java/io/github/jasonsimpart/createdelightcore/content/util/StorageRecipeQuality.java`

### 3.1 `isConversion(Recipe<?> recipe, Level level)`

此方法把两类配方认定为存储转换：

- Quality Food `ServerConfig.isRetainQualityRecipe(...)` 明确声明的保留品质配方。
- Quality Food `StorageRecipeCache.isStorageRecipe(...)` 识别出的存储/解包配方。

这样 CDC 不需要复制或维护整合包的全部压块配方列表，配方识别继续由 Quality Food 负责。

### 3.2 `isExplicitlyExcluded(Recipe<?> recipe)`

此方法只用于非存储配方的随机品质逻辑。`no_quality_recipes` 会阻止随机品质生成，但不能阻止存储转换的确定性继承；因此 Basin 先判断 `isConversion`，再判断该排除列表。

### 3.3 `convert(ItemStack result, Container inputs, Recipe<?> recipe, Level level)`

转换步骤按以下顺序执行：

1. `result.copy()`，绝不修改 Create、Quark 或其它上游返回的结果模板。
2. 仅当配方属于存储转换且结果非空时，移除结果上可能由上游提前写入的 Quality Food 标签。
3. 调用 `QualityUtils.handleConversion(...)`，由 Quality Food 根据输入容器计算确定性继承结果。
4. 返回复制后的结果；非存储配方直接返回复制品。

移除标签的目的包括 `NONE` 情形：如果上游先写入随机品质，`NONE` 输入仍必须保持 `NONE`，不能被上游随机结果污染。

## 4. Create 工作盆修复

文件：`src/main/java/io/github/jasonsimpart/createdelightcore/mixin/create/BasinRecipeMixin.java`

### 4.1 输入快照

在 `BasinRecipe.apply(BasinBlockEntity, Recipe, boolean)` 调用 `IItemHandler.extractItem(...)` 的第一个输入提取点，`quality_food$storeInput` 使用 `@ModifyVariable` 保存实际被匹配并消耗的物品：

- 每个匹配输入保存 `copyWithCount(1)`，避免后续提取修改原栈。
- `LocalIntRef count` 累计消耗数量。
- `LocalDoubleRef weight` 累计 Quality Food 权重。
- `LocalRef<List<ItemStack>> storageInputs` 保存这一轮实际输入快照。

### 4.2 输出转换

在调用 `BasinBlockEntity.acceptOutputs(...)` 的第一个参数上，`quality_food$applyQuality` 使用 `@ModifyArg`：

- 先取出并清空 `storageInputs`，使模拟处理和实际处理、连续批次之间不会复用旧快照。
- 存储配方创建 `SimpleContainer`，将快照传给 `StorageRecipeQuality.convert`。
- 非存储配方保持原有 Quality Food 随机品质逻辑。
- `no_quality_recipes` 只在非存储配方路径生效。

### 4.3 原有特殊逻辑

同一 Mixin 中原有的 Berry Syrup Basin 上下文栈仍保留：`apply` 入口执行 `pushApplyingBasin`，返回时执行 `popApplyingBasin`。本次修复没有改变 Berry Syrup 配方的输入、流体或热量逻辑。

## 5. Create 机械合成器修复

文件：`src/main/java/io/github/jasonsimpart/createdelightcore/mixin/create/RecipeGridHandlerQualityMixin.java`

目标类：`com.simibubi.create.content.kinetics.crafter.RecipeGridHandler`。

Mixin 包装 `tryToApplyRecipe` 中两次 `Optional.map(Function)` 调用，`require = 2`、`expect = 2` 确保普通合成和机械合成回退路径都被覆盖。包装后的函数先调用 Create 原始 mapper，再调用：

```text
StorageRecipeQuality.convert(result, craftingContainer, recipe, level)
```

输入是同一个 `CraftingContainer`，因此九格食物块压制和反向解包都能使用真实输入品质。非存储配方只得到结果副本，不改变原有合成行为。

## 6. Quark 合成器修复

文件：`src/main/java/io/github/jasonsimpart/createdelightcore/mixin/quark/CrafterBlockEntityQualityMixin.java`

目标类：`org.violetmoon.quark.content.automation.block.be.CrafterBlockEntity`。

Mixin 包装 `getResult` 内部对 `CraftingRecipe#getResult(Container, RegistryAccess)` 的调用。原始结果先由 Quark 生成，再使用传入的 `Container`、配方和当前 `Level` 调用公共转换策略。

该位置是 Quark 的真实结果入口，不依赖 GUI 预览或配方模板，因此覆盖实际合成器工作和解包。`@Pseudo` 与 `CombatMixinPlugin` 的双重判断保证 Quark 未安装时不会解析该目标类。

## 7. Vintage Improvements 振动台修复

文件：`src/main/java/io/github/jasonsimpart/createdelightcore/mixin/vintageimprovements/VibratingTableQualityMixin.java`

目标类：`com.negodya1.vintageimprovements.content.kinetics.vibration.VibratingTableBlockEntity`。

振动台的解包过程会先减少输入栈，再生成输出；若输入刚好只剩最后一件，事后读取输入会得到空栈。本 Mixin 在 `process` 中包装第一次 `ItemStack#shrink`（SRG 名 `m_41774_`）：

1. 在原始减少数量调用之前保存 `source.copyWithCount(1)`。
2. 继续调用原始 `shrink`，不改变振动台消耗数量。
3. 在第一次 `ItemStack#copy`（SRG 名 `m_41777_`）生成解包结果的位置，读取保存的快照。
4. 仅对存在解包配方的结果调用 `StorageRecipeQuality.convert`。

因此最后一件输入被消耗后仍能保留品质；没有快照、没有配方或没有 Level 时返回原结果。

## 8. Mixin 条件与依赖保护

`CombatMixinPlugin#shouldApplyMixin` 新增两组判断：

- 类名包含 `.quark.`：要求 `quark` 和 `quality_food` 都已加载。
- 类名包含 `.vintageimprovements.`：要求 `vintageimprovements` 和 `quality_food` 都已加载。

Create 路径依赖必需的 Quality Food，因此不需要额外的运行时跳过。所有新增类均已登记到 `mixins.createdelightcore.json` 的 `mixins` 数组，并使用 `remap = false` 对应已验证的依赖字节码方法名。

## 9. 行为保证

- `NONE` 输入输出仍为 `NONE`。
- `IRON`、`GOLD`、`DIAMOND` 输入在压块、解包和四条机器路径中保持同级品质。
- 混合输入遵循 Quality Food 的既有转换策略，不在 CDC 中重新定义权重或升级规则。
- 输入物品栈、配方模板和上游返回的结果模板不会被修改。
- 连续 Basin 批次不会串用上一批输入的快照。
- Tetra、Quark、Vintage Improvements、Quality Food 源码以及 Hotai 补丁文件均未被修改。

## 10. 验证记录

### 白盒验证

已使用实际安装的 CDC 回调和真实 Quark `getResult` 入口验证：

- 存储配方识别和 `no_quality_recipes` 兼容。
- `NONE`、`IRON`、`GOLD`、`DIAMOND` 四级输入。
- 工作盆压块、解包及连续两批处理。
- Create 机械合成器压块、解包。
- Quark 合成器真实 `getResult` 压块、解包及输出数量。
- 混合品质和 `NONE` 覆盖上游随机结果。
- 振动台最后一件输入消耗后的品质快照。
- 输入栈和配方模板保持不变。

白盒结果文件：`work/cdc-loaded-whitebox4-result.txt`。所有断言均为 `PASS`。

### 构建验证

使用 Java 17 和复制到工作目录的 Gradle 8.8 执行：

```text
clean build --no-daemon --console=plain -Dorg.gradle.native=false
```

结果为 `BUILD SUCCESSFUL`，20 个 Gradle 任务全部执行；`compileTestJava`、`test`、`extendedAeStartupTest` 均通过。正式 reobf JAR：

```text
Create-Delight-Core-1.20.1-2.2.16l.jar
SHA-256: 9E9CA3683929FA98678449EC5651E4C35C47CE265347B7D79DE96FA714EBCDAB
```

### 双端验收

正式 JAR 已安装到客户端和测试服务端，服务端以 6G 内存、`online-mode=false` 启动并监听 `25565`；用户已确认游戏内功能正常。服务端日志中 CDC、Hotai、Quality Food 均完成加载，Tetra 和其它依赖未被替换。

## 11. 提交与 PR 记录

- 目标仓库：`Jasons-impart/Create-Delight-Core`
- 目标分支：`1.20.1`
- 工作分支：`fix/food-storage-quality`
- PR：[#162 修复食品压块和解包时的品质继承](https://github.com/Jasons-impart/Create-Delight-Core/pull/162)
- 版本：`2.2.16l`
- 提交作者和提交者：`lnsanes`

本文件随源码提交到 PR 的 `doc/` 目录。PR 正文中的构建限制说明应以本次正式构建结果为准，不再使用早期增量包或“完整构建受限”的描述。
