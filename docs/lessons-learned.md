# Lessons Learned

## ForgeGradle userdev 必须优先命中官方 Forge Maven

**日期**: 2026-08-06

**问题**: ForgeGradle 下载 `net.minecraftforge:forge:*:userdev` 时遍历 `build.gradle` 的项目仓库，不使用 `settings.gradle` 的插件仓库。宽泛的 ModMaven 会对该坐标返回 HTTP 200 的 630 字节 HTML 跳转页，ForgeGradle 将其缓存后报 `Invalid patcher dependency`。

**正确做法**: 在项目仓库中把 `maven.minecraftforge.net`（仅 `net.minecraftforge`）放在第三方仓库之前，并从 ModMaven 排除该 group；出现旧缓存时删除 `~/.gradle/caches/forge_gradle/maven_downloader/net/minecraftforge/forge/<version>/` 下的 userdev jar 和 md5，再执行 `./gradlew help --no-daemon` 与 `./gradlew build --no-daemon`。

## 可选 Mixin 的共享接口不能放在 Mixin 包内

**日期**: 2026-07-21

**场景**: CDC 为 Apothic Attributes、Iron's Spells 和 Travel Optics 添加可选战斗兼容，同时让这些模组缺失时 CDC 仍能启动。

### 问题

Mixin 配置声明 `io.github.jasonsimpart.createdelightcore.mixin` 为专用包后，普通目标类或其他代码直接加载该包下的共享接口会触发 `IllegalClassLoadError`；仅使用 `required: false` 也不能证明存在目标模组时注入点一定命中。

### 正确做法

- 可选目标使用 `@Pseudo`，并由 `IMixinConfigPlugin` 在早期加载阶段按模组 ID 决定 Apply/Skip。
- 需要被目标类实现或被普通代码引用的接口放在 `compat/` 等非 Mixin 包下。
- 可选 Mixin 被插件判定为应用后，对关键注入设置 `require = 1`，让目标版本漂移直接在启动验证中暴露。
- 同时测试“目标模组全部存在”和“目标模组全部缺失”两种启动场景。

## Create Basin 动态流体输出需要执行上下文

**日期**: 2026-07-05

**场景**: `createdelightcore:berry_syrup_fluid_mixing` 需要按本次消耗的浆果动态决定 Cosmopolitan 糖浆流体。

### 规则

Create 普通 mixing JSON 只能声明固定 fluid output；如果输出依赖当前 Basin 输入，需要在 `BasinRecipe.apply(...)` 执行期间传入 Basin 上下文，再由自定义 recipe 从 Basin item handler 读取将被消耗的输入。

## 糖浆块直接使用 Cosmopolitan 实现

**日期**: 2026-07-05

**场景**: CDC 糖浆块参考 Cosmopolitan `SyrupBlock` 的薄碰撞面和粘滞逻辑。

### 规则

CDC 糖浆块直接使用 Cosmopolitan 的 `com.gumillea.cosmopolitan.common.block.SyrupBlock` 注册，避免本地行为和参考实现分叉；如果将来改回本地类，再重新评估蹲走速度一致性。

## Ponder 中 AE2 cable bus 连接需要补渲染状态

**日期**: 2026-07-05

**场景**: CDC 给 Ponder 场景展示 AE2 `ae2:cable_bus`。

### 问题

Ponder 使用自己的 `PonderLevel`/schematic world，AE2 cable bus 的 grid 生命周期可能没有正常建立连接，导致 `CableBusContainer#getRenderState()` 里没有邻接连接，视觉上 cable and bus 不会相连。

### 正确做法

在 client mixin 中只针对 `PonderLevel` 修正 `CableBusRenderState`：根据可见邻居补 `connectionTypes`/`cableBusAdjacent`，邻居是 `CableBusBlockEntity` 时直接取 `getCableBus()`，并清掉被 Ponder mask 成空气的方向。

## Fruit Delight 自定义 fruit 必须走 synthetic lookup 层

**日期**: 2026-07-05

**场景**: CDC 为 Fruit Delight 新增自定义 jelly / jello fruit。

### 问题

Fruit Delight 内部部分 jelly / jello 逻辑按 `FruitType` enum ordinal 查数组。如果把 CDC 自定义 fruit 追加进 `FruitType.values()`，可能让这些数组访问越界。

手写 `fruitsdelight:jelly` 或 `forge:jams` tag JSON 也容易和 datagen 结果重复或漂移，因为 `simpleJellyBottleBlock` 已经会为 jelly bottle 贡献这些 tag。

### 正确做法

1. 在 `CDBlocks` 注册三件套：`JellyBottleBlock`、`JellyBlock`、`JelloBlock`。
2. 使用 `simpleJellyBottleBlock("xxx", XxxFood::food, nutrition, saturation, color, effects...)` 注册瓶子。
3. 使用 `simpleJellyBlock("xxx_jelly", "xxx", color)` 和 `simpleJelloBlock("xxx_jello", "xxx", color)` 注册 jelly / jello 方块。
4. 在 `compat/fruitsdelight/` 新增 `XxxFood`，结构参考 `LushConfitureFood`。
5. `XxxFood.FOOD` 返回 `XxxFood.fruit()`、`FoodType.JELLY` 和自己的 `EffectEntry[]`。
6. 用 lazy holder 调用 `CustomFDFruits.register("XXX", jellyCost, color, ...)` 注册 synthetic `FruitType`。
7. `CustomFDFruits.register` 的 fruit name 会写进 Fruit Delight `JellyEffectRoot` NBT；必须稳定、唯一，通常使用全大写。
8. 在 `CustomFDFruits.bootstrap()` 中调用 `XxxFood.bootstrap()`。
9. 保持 mixin 通用：`FDFoodItemMixin` 从 NBT 读 fruit name，`FruitTypeMixin` 通过 `CustomFDFruits` 映射 `getJelly()` / `getJello()`。
10. 修改注册或 provider 后运行 `./gradlew runData --no-daemon` 和 `./gradlew build --no-daemon`。

### 规则

> **新增 Fruit Delight 自定义 fruit 时，只通过 `CustomFDFruits` 注册和查询 synthetic `FruitType`；不要硬编码 mixin，不要追加 `FruitType.values()`，不要手写 jelly / jams tag JSON。**

## 外部莓果优先接入已有 Berrfect 风味 tag

**日期**: 2026-07-06

**场景**: `createdelightcore:berry_syrup_fluid_mixing` 需要让外部模组的 `forge:berries` 参与 Cosmopolitan 糖浆风味计算。

### 规则

优先把外部莓果接入 Cosmopolitan 已有的 Berrfect tag，例如 `forge:fruits/blueberries`；关键兼容项也可以在 CDC 命名空间下用 `data/createdelightcore/berrfect/flavors/*.json` 保留显式 item 风味，避免 tag 单复数不匹配、上游 tag 变动或扩大影响到其它模组物品。

## 虚拟流体显示名要走 fluid 翻译键

**日期**: 2026-07-06

**场景**: CDC syrup 使用 `REGISTRATE.virtualFluid(...)` 注册，JEI tooltip 需要显示“糖浆”而不是“糖浆块”。

### 规则

虚拟流体如果有同名承载方块，`FluidType` 要显式返回 `fluid.<namespace>.<path>` 描述键；否则 `FluidStack#getDisplayName()` 可能落到方块翻译键，显示成块名。

## 双格作物兼容必须归一化下段并同步品质

**日期**: 2026-07-30

**问题**: 收割或生长兼容若把同一 `BlockState` 写入双格作物上下位置，会复制错误的 `DoublePlantBlock.HALF` 并触发整株清除；只给下段写 Quality Food `LevelData` 又会让点击或破坏上段时丢失品质。

**正确做法**: 先归一化到底部坐标，分别写回 `LOWER/UPPER`，并在自然生长与骨粉生长生成上段后把下段 `LevelData` 复制到上段；品质计算仍统一回溯到下段及其下方土壤。

## 第三方配方的 toolNotConsumed 不会自动约束 Create 机械手

**日期**: 2026-07-30

**问题**: `ProcessingRecipeBuilder.toolNotConsumed()` 只保存配方参数，Create `6.0.8` 的 `BeltDeployerCallbacks` 仅通过 `ItemApplicationRecipe.shouldKeepHeldItem()` 决定是否保留机械手工具。

**正确做法**: 对普通 `ProcessingRecipe` 派生的第三方工具配方，在机械手消耗点按明确配方类型跳过手持工具的第二次 `shrink`、`hurtAndBreak` 与 crafting remainder，同时保留传送带原料的第一次 `shrink`。

## Tetra 全息入口分页必须在构造时写入页内坐标

**日期**: 2026-07-31

**问题**: `HoloItemGui` 会在构造时把初始坐标固化进取消选中动画；若分页只在构造后调用 `setX/setY`，从改造详情返回时动画会把入口恢复到未分页坐标并打乱布局。

**正确做法**: 在 `HoloItemGui` 构造调用处把坐标改为页内坐标，再控制各页可见性；升级 Tetra 或 ExtraHoloPage 后复核构造器描述符和 `changeItem` 生命周期。

## Better Combat 零前摇配置受 JAR 下限限制

**日期**: 2026-08-03

**问题**: Better Combat `1.9.0+1.20.1` 的 `ServerConfig.getUpswingMultiplier()` 会把 `upswing_multiplier` 强制夹到至少 `0.2F`，因此整合包配置写成 `0.0` 仍保留前摇。

**正确做法**: 用按 `bettercombat` 模组存在性加载的 `@Pseudo` Mixin 将该常量下限改为 `0.0F`，并保留 `require = 1`；升级 Better Combat 后必须复核方法与常量是否仍匹配。
