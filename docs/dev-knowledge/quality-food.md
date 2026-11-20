# Quality Food 品质机制

CDC 在多种加工机器上补齐或纠正 Quality Food 的品质写入。本页记录当前规则和注意事项；仓储转换的逐路径调查见 [食物存储转换品质修复](../../doc/food-quality-storage-fix.md)，铜工作盆流体品质见 [兼容补丁台账](compatibility-patches.md) 与 [铜工作盆产出流体品质修复](../../doc/copper-basin-fluid-quality-fix.md)。作物收割与生命质校准器走 `QualityFoodMixin`、`QualityHarvestAutomationContext`，不在本页展开。

## 必须记住的 API 行为

Quality Food 的 `QualityUtils.applyQuality(ItemStack, Quality)` 实际调用三参数重载，默认 `canUpgrade = false`：

- 目标品质无效（包括 `Quality.NONE`）或物品不是有效食物时，直接返回，不会“写成无品质”。
- 默认模式下，只要产物**已经带有品质**，就不会改写。
- 即使传入 `canUpgrade = true`，也不会把更高品质降到更低品质。

因此：要覆盖上游已经写上的品质，或把金星降回铁星、把有星清成无星，必须先去掉产物上的 `quality_food` 标签（`ItemStack.removeTagKey` 或删除 `QualityUtils.QUALITY_TAG`），再调用 `applyQuality`。`Quality.NONE` 在清标签后不要再调用 `applyQuality`，清掉即为无星。

配方的 `getResultItem` 和 Create 的 `RecipeApplier.applyRecipeOn` 可能返回或改写**共享结果模板**。必须先 `copy()`，禁止直接对模板 `setCount` 或写品质，否则上一份金星会串到下一份铁星或无星。

在物品流体能力自身的 `getFluid/getFluidInTank` 内，不要调用 QFF 的 `applyEmptyingQuality`：该 helper 会再次读取物品的流体能力，导致递归溢出。此处直接用 `QualityUtils.getQuality(container)` 与 QFF `withQuality(fluid, quality)`；未满大酒瓶的 `SakeFluid` 已含品质，保留其原值。

妖归使用的 L2Library 2.4.25 `BaseTank.fill(stack, EXECUTE)` 会 `shrink` 调用者传入的流体；测试或后续计算需要传副本，或在入罐后用 `getFluidInTank(0).copy()` 获取实际批次，不能继续把已耗空的变量当原料。

Quality Food 的 `RecipeApplierMixin` 会在 `applyRecipeOn` 之后对每个结果调用 `applyQuality(result, List.of(input), player)`。这条路径会按原料品质权重再随机掷一次铁/金/钻石（各档独立判定，后面掷中的会盖住前面的），并且改的是共享模板。

CDC 的 `QualityFoodMixin.applyQualityFromIngredientsMixin` 会拦截上述掷骰入口：任一有效原料没有有效品质时，清掉产物品质并取消掷骰；全部有效原料都已有品质时则放行上游掷骰，所以铁星原料有机会出金星。

## 即时效果与品质

QF 2.4.3 的 `FoodUtils.modifyEffect` 在品质调整后统一丢弃 `duration <= 0` 的效果，未区分即时效果。`quality_food.FoodEffectQualityMixin` 仅将即时效果的时长检查阈值改为 -1，使零时长即时效果保留原时长；非即时效果的正时长要求、负等级过滤、品质无效与黑名单早退、QF 的等级和概率计算均沿用原逻辑。补丁只在 QF 存在时加载。

森罗受此问题影响的原始数据包括佳丽私酿的瞬间治疗、教父的至高点、螺丝起子的倒立和幽匿特调的尖啸攻击，招牌鸡尾酒继承的零时长即时效果也经过同一入口。Tavern 的三个自定义即时技能不读取效果等级，修复后仍使用原技能伤害规则。招牌鸡尾酒原生以整秒保存药水时长，不足一秒的非即时效果仍可能被截为零，并继续遵守正时长校验。对应回归使用真实酒效映射，覆盖四品质、签名鸡尾酒 NBT、实际瞬间治疗，以及持续效果和有害效果的 QF 规则；升级 QF 后复核或移除条件见[兼容补丁台账](compatibility-patches.md)。

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
| 妖怪归家简单发酵 | 允许掷级 | `mixin/youkaishomecoming/{FermentationTankQualityMixin,SimpleFermentationRecipeAccessor}` | 扣料前保存实际物品与流体，调用一次 QFF `rollBasinQuality(items, fluids)`；本批物品与流体产物共用结果，复制后清除模板残留品质，无星批次不串星。 |
| 妖怪归家原生酒瓶能力与装瓶 | 原样继承 | `mixin/youkaishomecoming/`；`compat/alcohol/YoukaiBottleQuality` | 原生装瓶在交互前保存流体快照，确认罐内减少后，在合并玩家库存前写品质，整罐排空后也能继承。大瓶未满时品质保存在 `SakeFluid` 内，满瓶重建物品时需把品质写到酒瓶；已删除的五款葡萄酒及对应大瓶不接入品质。 |
| 森罗酒馆酒桶与基酒 | 先酿造，初始品质封顶金，之后单次陈酿升级 | `compat/kaleidoscopetavern/`；`mixin/kaleidoscopetavern/`；KJS `Kaleidoscope Tavern/tags.js` | 基础酿造等待配方的 `unit_time`，原生配方为 2400 tick；期间仅保存待产出记录，实际输出槽为空，物品与流体能力均无成品可取，完成时才一次性生成，完成后普通／铁／金再陈酿 30／60／90 分钟，仅升一级。两个阶段随原生存档及更新包同步，开盖暂停，Jade 显示相应剩余时间。基酒排除醋和西瓜汁，直接使用 QF 品质与基础酒效档；铁起可调酒。六果汁压榨在比较前继承水果品质并模拟接收，CGT 瓶液互转桥接 QF／QFF 根品质；莫洛托夫沿用原规则。 |
| 森罗醋与鸡尾酒 | 按实际原料计算 QF 品质，药效统一经过 QF | 同上；`ShakerRecipeQualityMixin`、`ShakerFallbackQualityMixin`、`ShakerPourQualityMixin`、`CocktailDropsQualityMixin` | 无配方醋保留 16 瓶及原生扣料规则，2400 tick 酿造完成时才生成可取成品，不额外陈酿。鸡尾酒配方、摇动时长及无配方兜底分别仅掷级一次，保留签名杯颜色／效果；倒杯使用 QF 位置品质，普通档使用 `NONE_PLAYER_PLACED`。Mojito 倒杯及手动摆放补存实际成品，两条 `Block.getDrops` 最终入口在原生生成后统一补品质；摆放、取回与破坏沿用 QF 位置存储和清理；创造栏通过 `TavernCreativeTabs` 的 Forge 内容构建事件保序替换，基酒、醋及全部 14 种鸡尾酒统一钻品质。 |
| 魔法炼药锅酿造与装排液 | 酿造允许掷级；装排液原样继承 | `mixin/ironsspellbooks/AlchemistCauldronQualityMixin`、`AlchemistRecipeQualityMixin` | 酿造复用 QFF `rollBasinQuality(items, fluids)`，同时计入配料与流体；匹配只忽略品质，消耗实际批次 NBT；输出模板先复制，无品质输入清除模板残留品质。 |
| 恶魂酒醪桶 | 按实际食材允许掷级 | `mixin/dungeonsdelight/MonsterPotWortMixin` | 仅 `createdelight:alcohol_preparation/ghastly_spirits`：六槽各需并消耗 4 份，容器槽 7 必须有空桶，内部餐食槽 6 与输出槽 8 为空才开工；先复制结果模板、按扣料前输入掷级，避免缺桶或堵输出时损失原料。 |

## 外部酒与酵母

六款外部成品由 KJS 的 `createdelight:tavern_fermented_drinks` 标签明确接入 `TavernQuality.canAge/isBatchProduct`，复用已有待产出记录、初酿金品质上限、普通／铁／金 30／60／90 分钟单次陈酿和存档恢复；不扩大原生 `isBarrelProduct/isQualityWine`，因此外部物品不写 `BrewLevel`，仍使用原效果。标签不赋予外部酒调酒器支持。

父包在 `kubejs/server_scripts/Kaleidoscope Tavern/recipe.js` 逐条覆盖 24 条原生酒桶配方，保留原 ID、材料、基液、瓶器和 2400 tick 工时，显式加入干酵母。酵母沿用普通原料的产量计算，每瓶一份，满额 16 瓶需 16 份；不增加独立酵母标签、槽位、存档字段或 CDC 缺料等待。开盖时投料，盖盖子后沿用原生配方匹配、扣料与醋兜底；醋和压榨不要求酵母，原生 JEI 已将酒桶各原料显示为 16 份。

原生酒桶按 `hashCode` 偏移每 97 个世界 tick 检查一次配方，手动连续调用 `tick` 不会推进世界时间；相关 GameTest 使用 `succeedWhen` 随真实 tick 重试，并在每次回调内安装／恢复临时配方，避免随机漏过检查窗口或跨 tick 污染其他用例。

`MonsterPotWortJeiMixin` 仅对恶魂酒醪配方复制 JEI 候选物品并显示每槽 4 份，避免原生 `Ingredient.getItems()` 的单件预览低报成本；不修改配方模板或其他怪物锅配方，且仅在客户端与 JEI 存在时加载。

三条 `farmers_spell:alchemist_cauldron/{amethyst_tequila,catacombs_wine,ice_venom_wine}` 仅在配方实际输出对应 `createdelight:*_wort` 且流体／配料匹配时，把 `serverTick` 的 `cooktimes > 100` 阈值改为 19（20 tick），其他配方保留 100；品质继续走上表炼药路径。恶魂酒醪每 1L 前置 80 tick，三魔法酒醪每 250mB 前置 20 tick；四款后续满 4L 初酿 2880 tick，与前置累计共 3200 tick。内容配方与颜色由父仓维护。

`FireAleContainerMixin` 仅对 Iron's Spells 烈焰麦酒将 `finishUsingItem` 的原生 `returnItem` 替换为 B&C 酒杯，沿用原方法的单杯／堆叠余物处理；需两模组同时存在。

2026-11-19：生产迁移的外部酒 NBT／批次恢复、怪物锅四倍材料／空桶／堵输出／品质、烈焰麦酒堆叠余物测试已编译；缺酵母等待已撤回，相关断言改为开盖不酿造、盖盖子后普通酵母原料满额产出。Java 17 构建及 refmap 校验通过。首轮迁移两次 `runGameTestServer` 均在启动时无法读取 `mixins.createdelightcore.json`，新增断言未执行；主／测试资源已在 `MOD_CLASSES`，源文件与构建副本一致，构建 JAR 内配置和 refmap 静态校验正常，更具体的类加载原因尚未确定。真实游戏回归待补，不能沿用此前酒类测试的通过结果。

森罗酒桶的 Jade 与交互提示首行剥除 QFF 的 `with_quality` 名称包装并保留自定义名，品质只在独立行显示；待产出模板在酿造完成前尚未赋品质，直接显示 `ItemStack.getHoverName()` 会被 QFF 自动冠以“普通”，与批次品质冲突。

森罗六种果汁及桶沿用原生注册，保留 QF／QFF 支持标签。Forge `FluidBucketWrapper` 通用品质往返由 QFF 负责修复；CDC 不注册替代桶、不添加果汁桶 OEI 映射或专用桶 Mixin。六果汁四档品质的原生桶往返已通过当前本地 QFF 构建回归，CDC 继续保留该回归测试。

## 龙头与 Create 置物台

`BarrelTapDepotMixin` 在原生 `canTapExtract` 的酿造、成品及配方容器检查之后识别龙头正下方的 `create:depot`，并在 `transform` 中将一次出酒交给 `TavernTapDepot`。开阀和结束出酒的时机继续由森罗龙头控制，不增加 tick 轮询；只在森罗与 Create 同时加载时应用。

置物台持物必须匹配当前酒桶配方的 `carrier`，一次消耗一个容器和一瓶酒。通过公开的 `TransportedItemStackHandlerBehaviour` 加工回调取得真实持物：单个容器原位变为成品，堆叠容器用 `convertToAndLeaveHeld` 留下余瓶，酒进入 Create 原有产物缓冲；其标脏、同步和存档由 Create 负责。只有 `DepotBehaviour.isOutputEmpty()` 时才能出酒，否则 Create 的加工溢出路径会把余物掉落到世界。开阀后到出酒前再次检查容器及缓冲，阻塞或空提取不扣容器；置物台分支无法接酒时仍允许原生地面容器检查。

实际产出复用 `extractOneOutput`，继承现有初酿禁取、QF 品质和外部酒 NBT 规则；交付末瓶后才调用 `resetIfOutputEmpty`，避免清掉批次后丢失末瓶品质。`TavernTapDepotGameTests` 覆盖原生四档品质、外部酒与玻璃瓶、堆叠余瓶、缓冲阻塞及存档、容器中途变化、初酿开关盖和末瓶清理；运行结果见交付记录，编译不等同游戏测试通过。2026-11-20 的 Java 17 `clean build` 与 refmap 校验通过，`runGameTestServer` 仍在读取 Mixin 配置资源时失败，新增用例未执行，日志位于父包 `tmp-opencode/tap-depot/`；手动及红石开阀的实际游戏回归待补。

## 给新机器补品质时

1. 先分清产出从哪来：共享 `getResultItem` 模板、`RecipeApplier.applyRecipeOn`，还是 `rollResults` 一类新建堆叠。
2. 再决定策略：玩家应看到“和输入一样”（原样继承），还是“按合成转换再掷一次”（允许掷级）。
3. 原样继承且产物可能已被写入品质时：`copy()` → 清除 `quality_food` → `applyQuality(产物, 输入品质)`；输入无星则只清除、不再写入。
4. 不要指望 `applyQuality(产物, 输入品质)` 覆盖或降档已有品质；也不要用 `applyQuality(..., true)` 来降档。
5. 需要输入快照时，在扣料或槽位被清空之前 `copy()`。
6. 新 mixin 登记到 `src/main/resources/mixins.createdelightcore.json`；可选模组用 `CombatMixinPlugin` 按模组 ID 条件加载。
7. 验证至少覆盖：单一品质连续产出仍是该品质；金星之后再出铁星或无星不串味；若加工分多步，确认品质是在哪一步被改掉的。
