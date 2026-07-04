# Lessons Learned

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
