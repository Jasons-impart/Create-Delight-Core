# Quality Food 品质机制

CDC 在多种加工机器上补齐或纠正 Quality Food 的品质写入。本页记录当前规则和注意事项；仓储转换的逐路径调查见 [食物存储转换品质修复](../../doc/food-quality-storage-fix.md)，铜工作盆流体品质见 [兼容补丁台账](compatibility-patches.md) 与 [铜工作盆产出流体品质修复](../../doc/copper-basin-fluid-quality-fix.md)。作物收割与生命质校准器走 `QualityFoodMixin`、`QualityHarvestAutomationContext`，不在本页展开。

## 必须记住的 API 行为

Quality Food 的 `QualityUtils.applyQuality(ItemStack, Quality)` 实际调用三参数重载，默认 `canUpgrade = false`：

- 目标品质无效（包括 `Quality.NONE`）或物品不是有效食物时，直接返回，不会“写成无品质”。
- 默认模式下，只要产物**已经带有品质**，就不会改写。
- 即使传入 `canUpgrade = true`，也不会把更高品质降到更低品质。

因此：要覆盖上游已经写上的品质，或把金星降回铁星、把有星清成无星，必须先去掉产物上的 `quality_food` 标签（`ItemStack.removeTagKey` 或删除 `QualityUtils.QUALITY_TAG`），再调用 `applyQuality`。`Quality.NONE` 在清标签后不要再调用 `applyQuality`，清掉即为无星。

配方的 `getResultItem` 和 Create 的 `RecipeApplier.applyRecipeOn` 可能返回或改写**共享结果模板**。必须先 `copy()`，禁止直接对模板 `setCount` 或写品质，否则上一份金星会串到下一份铁星或无星。

Quality Food 的 `RecipeApplierMixin` 会在 `applyRecipeOn` 之后对每个结果调用 `applyQuality(result, List.of(input), player)`。这条路径会按原料品质权重再随机掷一次铁/金/钻石（各档独立判定，后面掷中的会盖住前面的），并且改的是共享模板。

CDC 的 `QualityFoodMixin.applyQualityFromIngredientsMixin` 会拦截上述掷骰入口：任一有效原料没有有效品质时，清掉产物品质并取消掷骰；全部有效原料都已有品质时则放行上游掷骰，所以铁星原料有机会出金星。

## 两条写入策略

| 策略 | 做法 | 适用 |
|---|---|---|
| 原样继承 | 按当前输入的品质写入产物；输入无星则产物无星，不掷级、不串上一份的品质。 | 冷冻、挤压盆、脱粒机、机械脱模器、仓储转换、石窑；烤箱实际效果也是继承。 |
| 允许掷级 | 调用 `applyQuality(产物, 原料列表, player)`，让 Quality Food（及上述 mixin 放行条件）按权重随机决定产物品质。 | 机械炸锅、油炸锅、煎锅、MBD2 物品产出。 |

原样继承时，若产物进手时**还没有品质**（例如 `getResultItem` 的新副本、`rollResults` 新建堆叠），可以直接 `applyQuality(产物, 输入品质)`。若产物进手时**已经被掷过或残留了品质**（典型是刚走过 `RecipeApplier`），必须先清标签再写；冷冻那种“直接写入”盖不住已经掷出来的金星。

公共实现：

- `content/util/StorageRecipeQuality.convert`：先 `copy()`，认定存储转换后再 `removeTagKey(QUALITY_TAG)`，然后 `QualityUtils.handleConversion`。注释写明：上游若已赋值，`NONE` 也必须保持 `NONE`。
- `content/util/QualityFoodUtil.applyQuality`：掷出目标品质后先删标签再写入。
- `QualityFoodMixin.create_Delight_Core$clearQuality`：删除 `QUALITY_TAG`，标签空则 `setTag(null)`。

## 机器与路径

| 路径 | 策略 | 实现 | 注意 |
|---|---|---|---|
| 冷冻方块 tick | 原样继承 | `mixin/ratatouille/FrozenBlockEntityQualityMixin` | 鼓风机冷冻走 Create `RecipeApplier`（Quality Food 已覆盖），本 mixin 只修方块实体 tick。先 `copy()` `getResultItem` 模板；`@ModifyVariable(ordinal = 1)` 对准输出 `newStack`，`0` 是输入。 |
| 挤压盆 | 原样继承 | `mixin/ratatouille/SqueezeBasinBlockEntityMixin` | `rollResults` 新建产物，不经 `RecipeApplier`。扣料前采样槽 0，`acceptOutputs` 前写入首个产物；输入只有槽 0，不要读槽 1。 |
| 脱粒机 | 原样继承 | `mixin/ratatouille/ThresherBlockEntityMixin` | 产物当时通常尚无品质，直接 `applyQuality(stack, 输入)`。 |
| 机械脱模器 | 原样继承 | `mixin/ratatouille/MechanicalDemolderQualityMixin` | 走 `RecipeApplier`，Quality Food 会先掷级并可能改共享模板。必须 `copy()`、清除 `quality_food`，再写成当前输入品质。测法：铁星烤好的蛋糕模具走机械脱模，胚应仍是铁星；再脱金星后再脱铁星或无星，后面不应被上一份金星污染。烤箱后、脱模前模具仍是铁星，说明问题在脱模这一步。 |
| 仓储转换（工作盆压块/解包、机械合成器、Quark 合成器、振动台解包） | 原样继承 | `StorageRecipeQuality`；`mixin/create/BasinRecipeMixin`、`RecipeGridHandlerQualityMixin`；`mixin/quark/CrafterBlockEntityQualityMixin`；`mixin/vintageimprovements/VibratingTableQualityMixin` | 识别存储配方后确定性继承，不走随机掷级。工作盆需在扣料前保存输入快照。细节见 [食物存储转换品质修复](../../doc/food-quality-storage-fix.md)。 |
| 石窑 | 原样继承 | `mixin/bakeries/StoneKilnBlockEntityMixin`、`StoneKilnShovelItemMixin` | 把输入的 `quality_food` NBT 直接拷到产物，绕过 `applyQuality` 的不覆盖限制。 |
| 烤箱 `BakeData` | 原样继承（效果） | `mixin/ratatouille/BakeDataMixin` | 先把输入整份 NBT（含品质）拷到产物，再调用掷骰接口；因为已经有品质，掷骰实际写不进去。 |
| 机械炸锅、油炸锅、煎锅 | 允许掷级 | `mixin/createbicbit/MechanicalFryerEntityMixin`、`mixin/casualness_delight/DeepFryingPanEntityMixin`、`mixin/refurbished_furniture/FryingPanBlockEntityMixin` | `applyQuality(产物, List.of(输入), null)`。炸锅类需在扣料前 `copy()` 输入，避免空输入。 |
| MBD2 物品产出 | 允许掷级 | `mixin/mbd2/ItemRecipeHandlerQualityMixin`、`content/util/MBD2RecipeQualityContext` | IN 扣料前按机器缓存输入样本，OUT 插入前 `applyQuality(产物, 样本, null)`。 |
| 铜工作盆流体 | 补写被跳过的流体品质 | `mixin/fluidlogistics/CopperBasinQualityMixin` | 铜盆覆写 `acceptOutputs` 后把流体从父类调用里剥掉，Quality Food Fluids 的注入拿到空列表会跳过。见兼容补丁台账。 |

## 给新机器补品质时

1. 先分清产出从哪来：共享 `getResultItem` 模板、`RecipeApplier.applyRecipeOn`，还是 `rollResults` 一类新建堆叠。
2. 再决定策略：玩家应看到“和输入一样”（原样继承），还是“按合成转换再掷一次”（允许掷级）。
3. 原样继承且产物可能已被写入品质时：`copy()` → 清除 `quality_food` → `applyQuality(产物, 输入品质)`；输入无星则只清除、不再写入。
4. 不要指望 `applyQuality(产物, 输入品质)` 覆盖或降档已有品质；也不要用 `applyQuality(..., true)` 来降档。
5. 需要输入快照时，在扣料或槽位被清空之前 `copy()`。
6. 新 mixin 登记到 `src/main/resources/mixins.createdelightcore.json`；可选模组用 `CombatMixinPlugin` 按模组 ID 条件加载。
7. 验证至少覆盖：单一品质连续产出仍是该品质；金星之后再出铁星或无星不串味；若加工分多步，确认品质是在哪一步被改掉的。
