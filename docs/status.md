## 迁移基线

当前移植基于 `1.20.1` 分支的 `8b26efcb520ad3ad5c04fe8db0290bc1059d2119` 提交；后续 `1.20.1` 分支的新提交，先把现有迁移缺口补齐，再继续跟进。

## 已处理 / 边界已定

| 旧 Core 功能区 | 旧源码入口 | 旧行为 | 当前状态 | 备注 |
| --- | --- | --- | --- | --- |
| 基础材料物品 | `CDItems` | 未炸食材、巧克力模具、锡/青铜材料、钱币 | 已迁移 | 资源和 lang 已迁入；具体配方留 pack/data |
| 冰淇淋球 | `CDItems.iceCreamScoop`, `IceCreamItem` | Alex's Caves 投掷物品，命中用 `ThrownIceCreamScoopEntity` | 已迁移 | 当前已接 Alex's Caves 投掷实体，不是普通雪球 |
| 饮品 / 果酱瓶物品 | `DrinkItem`, `MilkShakeItem`, `JellyBottleItem` | 饮用、返还容器、tooltip/可选效果 | 已迁移 | 当前改为 1.21.1 物品实现和可选药水 tooltip |
| 基础方块 | `CDBlocks` | 锡矿、深板岩锡矿、锡/青铜/锻造钢块、边境碎片 | 已迁移 | 资源、loot table、挖掘等级已迁入 |
| Create 机壳 | `CDBlocks`, `CDCSpriteShifts`, `GlassCassing` | steel/forge steel casing，steel glass/clear glass casing，CT 连接纹理 | 已迁移 | 当前用 1.21.1 Create CT/模型方式实现 |
| 糖浆方块 | `SyrupBlock` | `covered` 状态、薄碰撞、实体减速、sticky | 已迁移 | 当前行为已迁入 |
| 果酱/果冻/果酱瓶方块 | `JellyBlock`, `JelloBlock`, `JellyBottleBlock` | Honey/Slime 类行为、染色模型、tooltip、放置/饮用区别 | 已迁移 | 当前行为已迁入 |
| 花簇 | `FlowerClusterBlock`, `FlowerClusterBlockItem` | 火/冰/雷百合花簇，类 Farmer's Delight 菌落阶段 | 已迁移 | 当前资源和 harvest 主行为已迁入 |
| 月壤/月壤耕地 | `LunaSoilBlock`, `LunaSoilFarmlandBlock` | Northstar 月球促生长，百合转 CDC 花簇，灵质保湿 | 已迁移 | 当前用维度 namespace/tag 避免硬依赖 |
| 幻灵肥料 | `PhantomCompostBlock`, `CDTags.PHANTOM_COMPOST_ACTIVATORS` | 多阶段随机成熟，按周围激活方块/灵质/天光变概率，成熟成月壤 | 已迁移 | 当前行为已迁入 |
| Fan Freezing | `CDRecipeTypes`, `CDFanProcessingTypes`, `FanFreezingRecipe`, JEI category | 自定义 `fan_freezing` recipe type、Create 风扇冷冻、粒子/实体冻结/转化、JEI | 已迁移 | 当前 1.21.1 实现已存在 |
| ShapedRecipe 9x9 | `CDRecipeTypes.register` | 把 shaped crafting size 改到 9x9 | 已迁移 | 当前在 `ModRecipeTypes.register(...)` 调用 `ShapedRecipePattern.setCraftingSize(9, 9)`，属于全局 shaped recipe 尺寸设置 |
| CMR 雪傀儡冷却器流体燃料 | `compat/cmr/*`, `mixin/cmr/*` | 给 Snowman Cooler 加流体槽、流体容器插入、tick 消耗、冷热状态、数据包燃料表 | 已迁移 | 当前刻意只接受 `compat_cooler` 白名单数据，不恢复旧自动推导 lava 等可燃流体容器 |
| CMR / Create Liquid Fuel 燃料表同步 | `CDNetwork`, `SyncFuelMapsPacket`, `ClientFuelCache`, `ForgeEventsHandler` | datapack sync 时把 Blaze Burner / Snowman Cooler 液体燃料表同步到客户端，JEI 刷新 | 已迁移 | 当前用 NeoForge payload 实现；服务端 snapshot 会加载 Blaze Burner drainable 表 |
| Blaze Burner 液体燃料 JEI | `CDJEI`, `JeiCategoryBlazeBurnerFluid` | 展示 Create Liquid Fuel 的 Blaze Burner 流体燃料 | 已迁移 | 当前保留 JEI 分类；Jade Blaze 时间显示是新增可选项，不是旧迁移项 |
| Snowman Cooler 液体燃料 JEI/Jade | `CDJEI`, `JeiCategorySnowmanCoolerFluid`, `compat/jade/*` | 展示冷却器流体燃料和 Jade 冷却器信息 | 已迁移 | 当前 Jade Snowman Cooler 展示已接入 |
| 数据包 reload listener | `ForgeEventsHandler.addReloadListeners` | 加载 `snowman_cooler_fuel` JSON 并同步 | 已迁移 | 路径在当前实现改成 `compat_cooler` 白名单；旧通用 reload 项本身已随 CMR 迁移 |
| Create JEI recipe category 过滤 | `mixin/create/CreateRecipeCategoryMixin` | 隐藏/过滤特定 Create recipe category 结果 | 已迁移 | 当前已有 Create JEI recipe category filtering mixin |
| Waystones + Lightman's Currency | `TeleportHandler`, `MoneyUtil`, `mixin/waystones/WaystoneButtonMixin` | 传送前把 XP cost 换成 LC 钱，按钮显示钱币/金额 | 已迁移 | 当前改走 `WaystonesAPI.resolveRequirements`，UI 和实际传送共用；支持散币、背包钱包、饰品钱包，不读 ATM |
| JEI 中间件隐藏 / 配方展示 | `CDJEI` | 隐藏配方中间件，注册 fan freezing、燃料分类、Create category | 已迁移 | 当前 JEI 插件已覆盖主要项 |
| Jade 插件 | `CDJade`, `CDPlugin`, `CoolerProvider` | 注册 Snowman Cooler provider | 已迁移 | 当前 provider 更完整，含流体槽 |
| AE2 budding quartz 保护/注液 | 旧 Core 无直接等价 | 旧 Core 不做这项 | 已由新实现替代 | 当前是新 Core/pack 迁移项，不属于旧 Core 总账 |
| Lightman's Currency 币种链 | `MoneyUtil`, 旧 pack config | `moneyChain=main` 指向 CDC 钱币 | pack 侧 | `MasterCoinList.json` 属整合包配置，不进 Core；Core 只提供币物品和扣款兼容 |
| 掉落物报告 | `server/ItemEntityEvent`, `CDConfig.disableDropReport/itemThreshold/ignoreStackCount` | 每 5 分钟按区块统计掉落物，超过阈值给在线玩家发报告 | 已迁移 | 当前用 NeoForge server tick event；默认 `6000` tick，支持阈值、stack/entity 统计模式和最大报告区块数配置 |
| TooltipEvent 燃料桶提示 | `event/TooltipEvent` | 旧代码中已注释，说明迁到 JEI，不再重复 tooltip | 不迁移 | 不应恢复；Jade/JEI 展示已覆盖 |
| 纯配方 / 数据包平衡 | `data`, 旧 pack/KubeJS | 机器配方、订单、经济、隐藏原币、战利品等 | pack 侧 | 不塞 Core；Core 只承载必须 Java/能力/mixin 的行为 |

## 剩余待办 / 待确认

| 旧 Core 功能区 | 旧源码入口 | 旧行为 | 当前状态 | 备注 / 下一步 |
| --- | --- | --- | --- | --- |
| Mod 入口、配置、注册总线 | `CreateDelightCore`, `CDConfig` | 注册物品/方块/流体/配方/创物栏，挂 Forge 事件、网络、datagen；配置掉落报告、Waystones 钱、月壤概率、地表雕刻深度、Belt Grinder 黑名单 | 部分迁移 | 当前使用 NeoForge 正规 `DeferredRegister`；掉落报告、Waystones 钱、月壤概率、Belt Grinder 黑名单等已有等价配置，地表雕刻深度等仍未迁 |
| 创造模式标签 | `CDCreativeTabs` | `food` / `fluid` / `coin` / `misc` 四个 tab | 部分迁移 | 当前有 CDC 创物栏/分类，但不必照搬旧 Registrate tab 结构 |
| 自定义流体注册 | `CDFluids`, `SlimeFluidType`, `RadiationFluidType`, `DragonBloodFluidType` | 熔融金属、史莱姆类流体、核废料、虚拟 milkshake/grape juice 等 | 部分迁移 | 主要流体批次已迁入；旧资源里还有部分仅贴图/未注册项需按活动包引用再定 |
| 熔融金属遇水固化 | `MoltenMetalFluidInteraction`, `CMFluidsMixin` | 禁用 Create Metallurgy 默认流体交互，给 CDC/CM 熔融流体注册遇水变对应金属块/Scorchia | 未迁移 | 这是旧 Core 明确行为；后续若需要应做可选 compat，避免硬绑缺失 mod |
| 配方自动化忽略工具 | `CDRecipeTypes.shouldIgnoreInAutomation` | 按 Create automation ignore tag 或 `_manual_only` 判断 | 部分迁移 | 当前 Create/JEI 过滤已有部分实现；是否需要通用 API 待确认 |
| 世界雕刻/地表限制 | `mixin/Minecraft/CarverMixin`, `NoiseChunkMixin`, `CDConfig.surfaceDepthLimit` | 按 surface depth 限制 carver/地形相关行为 | 未迁移 | 1.21.1 worldgen 改动大，需要单独验证目标和副作用 |
| 玩家 tick 修正 | `mixin/Minecraft/PlayerMixin` | 玩家 tick 后执行旧兼容修正 | 待确认 | 需读需求/目标 mod 后再决定，不应盲迁 |
| AE2 终端合成质量保留 | `mixin/ae2/*` | 修改 Crafting Terminal 输出/slot 行为，保留或修正 Quality Food NBT | 未迁移 | 当前只有 AE2 budding quartz 保护和 Spout 互动，和旧 AE2 终端 mixin 不是同一功能 |
| Alex's Caves 酸/饱食袋兼容 | `mixin/alexscaves/*` | 改 Acid vaporize 放块逻辑；调整 Sack of Sating 参数 | 未迁移 | 需确认活动包是否还依赖旧平衡 |
| Bakeries 嗅探兽掉种 | `mixin/bakeries/SnifferEventMixin` | 取消 Bakeries sniffer 掉种事件 | 未迁移 | 目标 mod 是否在 1.21.1 活动包存在待确认 |
| Butchercraft 事件 | `mixin/butchercraft/ButchercraftModEventsMixin` | 修改/取消 Butchercraft 特定事件 | 未迁移 | 待确认目标 mod/API |
| Casualness Delight / Create Bicbit 深炸 | `mixin/casualness_delight/*`, `mixin/createbicbit/*` | 深炸锅/深炸配方输出参数修正 | 未迁移 | 待确认目标 mod 是否存在 |
| Construction Wand | `mixin/constructionwand/WandUtilMixin` | 调整建筑魔杖放置逻辑 | 未迁移 | 待确认 |
| Create Basin / Drain / Blaze Burner item shrink | `mixin/create/BasinRecipeMixin`, `ItemDrainCategoryMixin`, `BlazeBurnerBlockMixin` | Basin/Drain JEI 或配方处理修正；Blaze Burner 插入不直接 shrink 特定物品 | 部分迁移 | Create JEI 过滤与液体燃料链路已迁；Basin/Drain/物品 shrink 细项未逐项恢复 |
| Create Central Kitchen harvester 扩展 | `content/contraption/...CreateDelightCoreHarvesterMovementBehaviorExtensions`, `VineryHarvesterMovementBehaviorExtensions` | 让 Create harvester 采 CDC 花簇和 Vinery 葡萄 | 部分迁移 | CDC 花簇主行为已迁；Vinery harvester 扩展未确认是否仍需要 |
| Create Metallurgy 兼容 | `compat/createmetallurgy/backport/*`, `mixin/createmetallurgy/*` | Crucible alloying backport、gauge cache、Belt Grinder 黑名单、禁默认流体交互 | 部分迁移 | 当前基础 mixin、Belt Grinder 黑名单、JEI 修正已迁；合金 backport/遇水固化未全量恢复 |
| Create Utilities Void Battery | `mixin/CU/VoidBatteryMixin` | 改 Void Battery 容量/输入/输出参数 | 未迁移 | 待确认 Create Utilities 版本和 pack 是否需要 |
| Display Delight 盘子交互 | `mixin/displaydelight/InterationManagerMixin` | 裸手取放物品时保留/修正 NBT 或容器逻辑 | 未迁移 | 待确认 |
| Ecliptic Seasons 作物生长倍率 | `content/util/EclipticSeasonsUtil`, `mixin/eclipticseason/*` | 把 Quality/地块上下文合入季节作物生长概率和 Growth Detector 显示 | 未迁移 | 当前迁移的是 Ecliptic Seasons 区块附件同步时序补丁，不是旧生长倍率功能 |
| Farmer's Delight 派生配方/采收质量 | `mixin/farmersdelight/*` | 取消 DoughRecipeMaker 默认配方；蘑菇菌落/番茄右键收获时设置 Quality Food 上下文 | 未迁移 | 纯配方应 pack 侧；质量上下文如要恢复需连 Quality Food 一起做 |
| Fruits Delight 质量传播 | `content/util/FruitsDelightTreeQualityContext`, `mixin/fruitsdelight/*` | 果树/树叶/果丛/榴莲掉落携带 Quality Food 品质，树苗品质传到果树 | 未迁移 | 大块旧质量食物兼容，建议单独评估 |
| FTB Ultimine 质量传播 | `mixin/ftbultimine/*` | 连锁收获作物时记录 harvest context，掉落物应用 Quality Food | 未迁移 | 依赖 Quality Food 迁移决策 |
| Functional Storage 压缩抽屉 | `mixin/functionalstorage/CompactingUtilMixin` | 查找压缩/解压配方时保留或规范 ItemStack NBT | 未迁移 | 待确认是否仍影响钱币/品质物品 |
| Ice and Fire 装备判定 | `mixin/IAF/*` | Gorgon blindfold / Siren earplugs 支持更多装备来源 | 未迁移 | 目标 API 改动大，待确认 |
| My Nether's Delight 收获质量 | `mixin/mynethersdelight/*` | Powdery Cane/Cannon 右键收获设置 Quality Food 上下文 | 未迁移 | 依赖 Quality Food 迁移决策 |
| Neapolitan 收获/JEI | `mixin/neapolitan/*` | Mint/Strawberry 收获质量上下文；取消 Neapolitan JEI runtime hook | 未迁移 | 待确认目标 mod 和 JEI 问题是否仍存在 |
| Quality Food 核心兼容 | `content/util/QualityFood*`, `mixin/quality_food/*` | 根据 harvest context 给作物掉落/方块数据/工具函数应用品质 | 未迁移 | 旧 Core 最大未迁移块之一；若活动包仍有 Quality Food，应优先单独落地 |
| Ratatouille 机器输出质量 | `mixin/ratatouille/*` | 烤箱、挤压盆、脱粒机输出时保留/应用 Quality Food NBT | 未迁移 | 依赖 Quality Food 迁移决策 |
| Refurbished Furniture 烹饪输出质量 | `mixin/refurbished_furniture/*` | 平底锅/炉灶完成烹饪时处理输出 NBT/品质 | 未迁移 | 依赖 Quality Food 迁移决策 |
| Spice of Life Apple Pie / Carrot | `mixin/solapplepie/*`, `mixin/solcarrot/*` | 修改食物列表/收益更新，避免与特定玩家 capability 或空数据冲突 | 未迁移 | 待确认这些 mod 是否仍在活动包 |
| Trail & Tales Delight | `mixin/trailandtalesdelight/*` | Budding Lantern Fruit 收获质量上下文 | 未迁移 | 依赖 Quality Food 迁移决策 |
| Trader Fresh | `mixin/trader_fresh/RestockEventHandlerMixin` | 修改村民补货交互逻辑 | 未迁移 | 待确认目标 mod |
| TrueUUID | `mixin/trueuuid/SkinRefreshHandlerMixin` | 修改/取消皮肤刷新处理 | 未迁移 | 待确认目标 mod 是否存在 |
| Datagen/lang/tag/loot 生成 | `data/*`, `CDRegistrateTags` | 旧 Registrate datagen、中文 lang provider、实体/damage type/provider | 部分迁移 | 当前资源手写/NeoForge datagen 混合；只按 1.21.1 需要保留 |
| OptiFine CIT 钱币贴图 | `assets/.../textures/optifine/cit` | 钱币随机/变体 CIT 资源 | 待确认 | 当前 1.21.1 pack 是否使用 OptiFine/CIT Resewn 未确认；通常偏 pack 资源 |
