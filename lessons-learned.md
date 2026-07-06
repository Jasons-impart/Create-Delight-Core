# Lessons Learned

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
