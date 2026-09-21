## 迁移基线

当前移植基于 `1.20.1` 分支的 `8b26efcb520ad3ad5c04fe8db0290bc1059d2119` 提交；后续 `1.20.1` 分支的新提交，先把现有迁移缺口补齐，再继续跟进。

该基线对应 Core `2.2.14`、Minecraft `1.20.1`、Forge `47.4.10`。本地源码对照已按精确提交建立独立 detached worktree：`/home/halo/gitRepo/JSI/Create-Delight-Core-1.20.1-baseline-8b26efc`。保留完整基线用于核对，不随 `1.20.1` 分支更新，也不按 PTTOD 的逐项删除流程清理。

## 当前本地参考路径

- Prism Launcher 安装方式：APT；数据目录为 `/home/halo/.local/share/PrismLauncher`。
- CDPR 实例根目录：`/home/halo/.local/share/PrismLauncher/instances/CDPR/minecraft`
- CDPR 活动 mods：`/home/halo/.local/share/PrismLauncher/instances/CDPR/minecraft/mods`
- 旧版整合包迁移对照：`/home/halo/.local/share/PrismLauncher/instances/CDPR/0488/tmp488/PTTOD`。按“迁移完成并验证一个，删除对应旧内容一个”的方式维护剩余迁移队列；缺失内容可能已经迁移，不能当作完整旧包快照。
- Quality Food CDPR fork：`/home/halo/gitRepo/JSI/quality_food_j`
- Vineflower 源码/构建目录：新设备尚未找到本地 checkout，需要时再获取，不再使用旧 Desktop 路径。

换机核对（2026-09-12）：APT 下的 `0488` 为 `1d6e209`，旧设备保留目录 `/home/halo/Desktop/myPrism/PrismLauncher-Linux-Qt6-Portable-11.0.2/instances/CDPR/migration-tmp488` 为 `a74976b`。两处 PTTOD 共有文件内容一致，但 APT 目录额外保留 `config/ftbquests/`、`kubejs/client_scripts/render/infinity_cell.js`、`kubejs/client_scripts/render/render_item_energy_bar.js` 和 `schematics/`。继续使用上述 APT 路径作为迁移队列；处理这些额外内容前，先核对活动包和迁移记录是否已完成，本次未同步或删除旧内容。

当前 CDPR 实例已部署 `quality_food-1.21.1-2.3.6-cdpr.1.jar`；原 `quality_food-1.21.1-2.3.4.jar` 已改名为 `.jar.disabled` 作为可恢复备份（2026-07-24）。修复 Ratatouille 与 Sophisticated Core 版本签名后，CDPR 客户端已成功进入标题界面，日志无 Quality Food `InvalidInjection`、`MixinApplyError` 或 fatal。CDPR fork 以 Quality Food `2.3.6` 为基线，继续保留原 `quality_food` mod id 和数据组件命名空间。

第三方兼容分析优先使用目标 mod 的公开源码或 Gradle `sources` jar；源码不足时先用 `javap` 确认签名，只按需求使用 Vineflower 反编译相关类，不对整个 mods 目录做无目的全量反编译。

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
| ExtendedAE 动态无限源流体元件 | 当前 pack `startup_scripts/mods/ae2/eae/inf_cells.js` | 按存在的无限源流体动态注册 `createdelightcore:<fluid_id>_cell`，类型为 `extendedae:custom_infinity_cell` | pack 侧 / 待确认 | 当前仍留 KJS；不是旧 Core 项，也不是普通静态注册，迁入 CDC 需确认 ExtendedAE API |
| 旧 MBD2 机器链 | PTTOD `ldlib/assets/mbd2`、`server_scripts/mbd2/*` / `mbd2_recipes/*` | MBD2 机器、UI、配方 schema 和运行时事件，包括 `mechanical_craft_encoder` | 迁移中 | 活动包已有 MBD2 21.0.6 / LDLib2 2.2.27；注册归 Core，整合配方归 pack。六种多方块及部件共 24 注册、8 配方类型已迁入；机械合成编码器等单方块机器仍在队列，不能标记整条机器链完成 |
| Mechanical Craft Encoder | PTTOD `server_scripts/mbd2/mechanical_craft_encoder.js` | 红石触发后匹配 `create:mechanical_crafting` 配方并输出带 Create `PackageOrderWithCrafts` 的包裹 | 未迁移 | 属旧 MBD2 机器链；当前活动 pack 的 Applied Create 配方检测不到 `createdelightcore:mechanical_craft_encoder` 会直接跳过相关 pattern provider 配方 |
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
| Vinery 发酵桶替换 | Vinery fermentation barrel / BlockEntity 交互链 | 旧链路依赖 Vinery 自带发酵桶完成葡萄酒发酵 | 计划重做 | 干掉 Vinery 发酵桶依赖；不继续补它的桶兼容，改用 CDC 自己的发酵流程。实现方向是直接飞 Vinery barrel BlockEntity：配方、进度、输入/输出、自动化接口和展示都落到 CDC 侧，Vinery 只作为物品/流体来源或可选数据兼容 |
| Create Metallurgy 兼容 | `compat/createmetallurgy/backport/*`, `mixin/createmetallurgy/*` | Crucible alloying backport、gauge cache、Belt Grinder 黑名单、禁默认流体交互 | 部分迁移 | 当前基础 mixin、Belt Grinder 黑名单、JEI 修正已迁；合金 backport/遇水固化未全量恢复 |
| Create Utilities Void Battery | `mixin/CU/VoidBatteryMixin` | 改 Void Battery 容量/输入/输出参数 | 未迁移 | 待确认 Create Utilities 版本和 pack 是否需要 |
| Display Delight 盘子交互 | `mixin/displaydelight/InterationManagerMixin` | 裸手取放物品时保留/修正 NBT 或容器逻辑 | 未迁移 | 待确认 |
| Ecliptic Seasons 作物生长倍率 / 探测器显示 | CDC `client/eclipticseasons/*`; Quality Food fork `compat/EclipticSeasonsCompat`; 旧 `content/util/EclipticSeasonsUtil`, `mixin/eclipticseason/*` | Growth Detector 粒子显示；玩家采收品质概率按季节生长倍率修正 | 部分迁移 | 客户端探测器显示已在 CDC；Quality Food fork 已按 CDPR 的 Ecliptic Seasons 0.13.9.1 实际 API 恢复玩家采收倍率，并处理甘蔗/番茄藤基部定位。旧自动化倍率与品质上限依赖尚未迁入 1.21.1 的生命质品质收割控制器，待该 CDC 功能迁移时通过兼容接口接入，不能孤立搬入 fork |
| Farmer's Delight 派生配方/采收质量 | `mixin/farmersdelight/*` | 取消 DoughRecipeMaker 默认配方；蘑菇菌落/番茄右键收获时设置 Quality Food 上下文 | 未迁移 | 纯配方应 pack 侧；质量上下文如要恢复需连 Quality Food 一起做 |
| Fruits Delight 质量传播 | Quality Food fork `util/FruitsDelightTreeQualityContext`, `mixin/fruitsdelight/*`, `mixin/l2harvester/*`; 旧 Core 同名实现 | 果树/树叶/果丛/榴莲掉落携带 Quality Food 品质，树苗品质传到果树 | 已在 Quality Food fork 实现 / 启动注入通过 | 已完成标签桥、右键收获、树苗生成树块、双层果丛、L2Harvester 和榴莲 falling block 品质传播；CDPR 整包启动无相关注入错误，仍需实际行为测试 |
| FTB Ultimine 质量传播 | Quality Food fork `mixin/ftbultimine/*`; 旧 Core `mixin/ftbultimine/*` | 连锁右键收获作物时记录 harvest context，给 `ItemCollector` 收集的掉落物应用 Quality Food | 已在 Quality Food fork 实现 / 启动注入通过 | 已适配 `ftb-ultimine-neoforge-2101.1.13.jar` 新包名与调用链；CDPR 整包启动无相关注入错误，仍需实际连锁收获测试 |
| Functional Storage 压缩抽屉 | `mixin/functionalstorage/CompactingUtilMixin` | 查找压缩/解压配方时保留或规范 ItemStack NBT | 未迁移 | 待确认是否仍影响钱币/品质物品 |
| Ice and Fire 装备判定 | `mixin/IAF/*` | Gorgon blindfold / Siren earplugs 支持更多装备来源 | 未迁移 | 目标 API 改动大，待确认 |
| My Nether's Delight 收获质量 | `mixin/mynethersdelight/*` | Powdery Cane/Cannon 右键收获设置 Quality Food 上下文 | 未迁移 | 依赖 Quality Food 迁移决策 |
| Neapolitan 收获/JEI | `mixin/neapolitan/*` | Mint/Strawberry 收获质量上下文；取消 Neapolitan JEI runtime hook | 未迁移 | 待确认目标 mod 和 JEI 问题是否仍存在 |
| Quality Food 核心兼容 | CDPR fork `/home/halo/gitRepo/JSI/quality_food_j`; CDC `compat/qualityfood/*`, `QualityAbsorberItem`; 旧 `content/util/QualityFood*`, `mixin/quality_food/*` | Quality Food 的采收、合成和机器品质传播归 fork；`quality_absorber`、生物掉落清理和 Lightman's Currency 兑换仍属于 CDC | 核心迁移完成 / 待行为测试 | `BlockDataMixin` 不迁（2.3.6 原生实现更正确）；`SpecialContainerMixin` 已改为 fork 直接修复；严格配方规则、成熟度、多格作物、季节倍率及活动模组兼容均已落地。Sophisticated Core 1.4.36/1.4.54 双签名兼容已通过 CDPR 启动验证。旧 1.20.1 生命质自动收割系统不在当前 1.21.1 分支，本轮不列为 Quality Food 待办 |
| Ratatouille 机器输出质量 | Quality Food fork `mixin/ratatouille/*`; 旧 Core 同名实现 | 烤箱、挤压盆、脱粒机输出时保留/应用 Quality Food 品质 | 已在 Quality Food fork 实现 / 启动注入通过 | 已按 `create_ratatouille-1.21.1-1.4.0.jar` 实际字节码适配 Data Component 品质传播；首次启动发现挤压盆 `WrapOperation` 接收者签名过宽，改为 `ModifyArg` 后复测启动无注入错误。旧 `SqueezingRecipeMixin` 是流体数量匹配修复，不属于 Quality Food，继续留在 CDC 迁移范围 |
| Refurbished Furniture 烹饪输出质量 | Quality Food fork `mixin/refurbished_furniture/*`; 旧 Core 同名实现 | 平底锅/炉灶完成烹饪时处理输出品质 | 已在 Quality Food fork 实现 / 启动注入通过 | 已按 `refurbished_furniture-neoforge-1.21.1-1.0.22.jar` 实际字节码适配平底锅和炉灶；CDPR 整包启动无相关注入错误，仍需实际烹饪测试 |
| Spice of Life Apple Pie / Carrot | `mixin/solapplepie/*`, `mixin/solcarrot/*` | 修改食物列表/收益更新，避免与特定玩家 capability 或空数据冲突 | 未迁移 | 待确认这些 mod 是否仍在活动包 |
| Trail & Tales Delight | `mixin/trailandtalesdelight/*` | Budding Lantern Fruit 收获质量上下文 | 未迁移 | 依赖 Quality Food 迁移决策 |
| Trader Fresh | `mixin/trader_fresh/RestockEventHandlerMixin` | 修改村民补货交互逻辑 | 未迁移 | 待确认目标 mod |
| TrueUUID | `mixin/trueuuid/SkinRefreshHandlerMixin` | 修改/取消皮肤刷新处理 | 未迁移 | 待确认目标 mod 是否存在 |
| Datagen/lang/tag/loot 生成 | `data/*`, `CDRegistrateTags` | 旧 Registrate datagen、中文 lang provider、实体/damage type/provider | 部分迁移 | 当前资源手写/NeoForge datagen 混合；只按 1.21.1 需要保留 |
| OptiFine CIT 钱币贴图 | `assets/.../textures/optifine/cit` | 钱币随机/变体 CIT 资源 | 待确认 | 当前 1.21.1 pack 是否使用 OptiFine/CIT Resewn 未确认；通常偏 pack 资源 |

## MBD2 多方块迁移验证（2026-09-18）

注册、机器行为、UI 和资产归 Core；加工、制作及整合配方归 pack 的 KubeJS server scripts。六种多方块及部件共 **24 注册、8 配方类型** 已实现；2026-09-18 按用户要求同步到正式 CDPR 客户端，待用户启动验收；未发布、未删除 PTTOD 参考文件。机械合成编码器等旧单方块机器不属于本批。

| 多方块 | 已恢复行为 | 验证 |
| --- | --- | --- |
| 合金电炉 | 单/双线圈、8/32 并行、双线圈时间减半、800000 FE、总线方向/过滤/库存代理、存档与重成型 | 3 项 GameTest；客户端模型、自动成型、UI 和槽位交互 |
| 水力发电站 | 四档转子、最大 13 层、-32 RPM、512/2048/4096/8192 扭矩、拆控制器停转 | 5 项 GameTest；客户端四档完整转子及旋转已检查，含首次启动未执行资源重载 |
| 屠宰室 | 128 RPM / 1024 应力逐 tick 输入、1 输入/20 输出/4000 mB 血液、断动力回退、胴体/工具动画与清理 | 2 项 GameTest；客户端真实猪加工产物、UI、22 条 JEI 配方及悬挂胴体/工具；已观察加工中胴体分解阶段变化，未逐一覆盖所有动物 |
| 装配线 | 最短/最大结构、4 流体与 5..10 物品端口排序、具名输入、精确消耗与保留工具、Create 序列装配代理 | 2 项 GameTest，含真实精密构件序列；客户端成型/模型/UI 已检查，上半空白与旧定义一致 |
| 大型离心机 | 32 RPM 起转、16 并行、32/256 RPM 对应 200/50 tick、4/9 物品槽与 8000 mB 流体槽、Vintage Improvements 代理 | 1 项 GameTest，含真实物品/流体加工；客户端模型、UI、256 RPM 动态转子已检查 |
| 裂变反应堆 | 2..74 组件、800 tick 燃料、温度/冷却/损坏/倍率、冷态零产出、热态 FE/废液、6 种红石模式、双页 UI | 2 项 GameTest，含上升沿/报警、2500 K / 100% 损坏核爆、爆炸尺寸、工作中拆控制器仅触发一次核爆；客户端燃料消耗、启停、50% 燃烧率编辑、页签切换、逻辑模式选择及服务端同步通过 |

实现与跨版本修复：

- API 对照固定 MBD2 `1c5c7f6`（21.0.6）、旧 Forge MBD2 `51d7096`（1.0.24）、LDLib2 `c2d9921`（2.2.27）。`scripts/import-mbd2.py` 转换旧 NBT 为 Core SNBT，迁移开关字段、包装数据、渲染器、状态模型、动力接口和旧 UI。缺失可选结构方块的判定禁用，不匹配空气/任意方块。
- MBD2 异步结构快照未覆盖较短重复层的问题，通过覆盖全部合法位置及每控制器独立 BlockPattern 修复。燃料搜索错误查询加工类型、纯流体代理在事件前构建物品输出崩溃的问题，由 Core 自有 recipe type 兼容。
- Flywheel 旋转模型需在首次模型注册/烘焙前创建并保持强引用；MBD2 延迟创建加弱注册表曾导致金属水轮首次加载缺叶片、F3+T 后恢复。Core 提前登记所有机器状态的 PartialModel；未采用试验阶段的 cutout 材质改动。
- LDLib2 UI 模板不保存 Java 回调，实时 MachineUIEvent 重新绑定页签；机器数据由服务端同步。修复默认按钮遮住加工箭头、英文 JEI 耗时覆盖产物、Butchercraft 库存变化不发客户端包、旧命名空间及 New Age 机壳纹理变更。
- 补齐奶昔分离所需 10 种奶昔和 15 种冰淇淋流体，保留旧 tint/材质/音效，无世界方块和桶。来源提交及 MIT 许可随 JAR 保存于 `META-INF/licenses/mbd-food-fluids.txt`。pack 恢复 15 条 500 mB 奶昔 → 250 mB 奶 + 250 mB 冰淇淋 / 100 tick 配方；其他食品生产、容器链另行迁移。

最终验证证据：

- NeoForge 开发和隔离整包环境均为 **21.1.242**。15 项 GameTest 全部通过（`/tmp/cdpr-mbd-final-tests-3.log`）；包括严格数据校验、UI 纵向滚动/翻译及客户端模型提前登记；build 同时通过。
- 隔离整包服务端 **264 条 MBD 配方**通过原始 JSON 严格解码对照、非空物品/流体和网络编码往返（`/tmp/cdpr-mbd-full-server-9.log`）。先前仅检查已解码对象无法发现 MBD2 静默丢弃错误输入；客户端测试发现反应堆燃料输入包装错误后已修正为平铺 `{item,count}`，并增加原始 JSON 检查，不能将早期 264 条宽松校验视为等价证据。
- 最新无 MBD2/LDLib2 的 Core 隔离服务端启动到 Done（`/tmp/cdpr-core-no-mbd-final.log`），没有本兼容层缺类崩溃。第三方 Create Deco 自带 placard 配方仍有格式错误。
- 导入脚本在临时目录重新生成 149 个资产，与当前源资源逐字节一致；旧参考源保留。
- 最终隔离客户端 `/tmp/cdpr-mbd-client-10.log` 首次启动即正确显示四档水轮，未执行 F3+T。截图 `/tmp/cdpr-hydro-four-tiers-final.png`、`/tmp/cdpr-fan-cold-start.png`；逻辑端口纵向滚动/完整英文标签见 `/tmp/cdpr-logic-final-fit.png`，选择缺燃料模式后服务端保存 `state: 5`。
- pack 最终 refresh/check 通过，2071 个纳入文件、315 个元数据文件，JAR 被忽略（`/tmp/cdpr-mbd-pack-refresh-final2.log`、`/tmp/cdpr-mbd-pack-check-final2.log`）。
- 整包临时测试副本有 TaCZ `const` → `let` 的已有启动兼容修正，正式客户端同步时已将该已验证修正写回该文件；已有 Ice and Fire progression.js Rhino 重声明、Vintage Improvements 两条 rolling 配方错误仍存在，不能称为原样整包零错误。

旧设计边界：装配线最多 10 个物品端口，而旧内置 recipe_0 要 12 输入，保留旧限制。旧脚本、ID 引用及机器定义中未找到装配线及两种输入部件的制作配方，未擅自增加平衡配方，保留创造/命令获取。验证区分真实机器运行、配方数据/网络和客户端视觉；264 条配方不等于全部逐条人工加工。六种机器已完成本批功能迁移及代表性客户端验收；未将有限测试夸大为所有组合均已逐一验证。

- 2026-09-18 客户端同步：已验证开发构建安装至 `/home/halo/.local/share/PrismLauncher/instances/CDPR/minecraft/mods/createdelightcore-2.0.0.5+1.21.1.jar`（沿用受管文件名，JAR 内版本 `0.0.0`）；SHA-256 `60f5fae6565355c70fb55b8338cbb12f59d96a4bea02b0d6841cfd51684fddbf`。旧 JAR 与 TaCZ 脚本备份在 `/home/halo/.local/share/PrismLauncher/instances/CDPR/backups/mbd-client-sync-20260918-125248`。公开下载描述符仍指向发布版，后续托管文件同步可能恢复发布版；本次为本机测试覆盖。


### 配方改为 KubeJS 原生 schema（2026-09-18）

- 按用户要求，pack 的 `kubejs/server_scripts/mods/mbd2/` 改用 7 个脚本定义本批 99 条配方（75 条加工/燃料、24 条制作），移除对应的 99 个 `kubejs/data/.../recipe/*.json`，保持配方 ID。注册项、机器行为和资源继续归 Core；未修改或删除 0488/PTTOD 参考目录。
- MBD2 21.0.6 的 `MBDKubeJSPlugin.registerRecipeSchemas` 遍历已注册 recipe types，8 个 Core 类型均已有 `MBDRecipeSchema.SCHEMA`，无需重复补 schema。加工使用原生 `.inputItems/.inputFluids/.inputFE/.inputStress/.slotName`；制作使用 Minecraft 和 Create 原生 schema。`chance/perTick/slotName` 为后续内容的 builder 状态，脚本明确控制作用范围。
- 静态基线 75/75 加工/燃料配方逐字段一致（`/tmp/cdpr-verify-mbd-script.cjs`）。隔离整包冷启动到 Done，264 条 MBD 配方非空输入及网络往返通过（`/tmp/cdpr-mbd-kjs-final-server.log`）；冷启动及最终 `/reload` 后的 99 条实际加载配方均对照转换前 JSON 经同一 Minecraft Recipe CODEC 解码后的结果，数量、概率、耗能、逐 tick 消耗、端口名、时长、优先级、可见性及制作步骤保持一致。比较脚本 `/tmp/cdpr-compare-mbd-runtime.py`，数据 `/tmp/cdpr-mbd-server/mbd-kjs-runtime-comparison.json`。
- 对照仅规范化两种等效表示：MBD 未启用扭矩覆盖的默认值；Create 将单一流体输入转换为同一流体、空组件且非严格匹配的 `neoforge:components`。工作台配方显式用 `event.recipes.minecraft.crafting_shaped/crafting_shapeless` 保持原 serializer 类型。
- 测试机 inotify 实例配额耗尽，Framework/Immersive Paintings 在启动监听器时失败。最终验证仅在 `/tmp` 测试 JVM 中使用临时 agent 禁用 Linux 文件热监听，显式 `/reload` 仍执行完整配方加载；该 agent 未加入 pack、Core 或正式客户端，也未改系统配额。已有 Ice and Fire progression.js 重声明、Vintage Improvements 两条 rolling 错误不属于本次变更，仍未解决。
- 七个脚本已直接写入正式 CDPR 客户端目录，格式检查及 pack refresh/check 通过；不需要更换 Core JAR。启动客户端或已有世界执行 `/reload` 即可载入。


### 已迁移多方块旧源清理（2026-09-18）

- 用户确认后，按“迁移并验证一项，删除对应旧源”流程，从 `0488/tmp488/PTTOD/` 清理 **39 个文件**：24 个机器/部件定义（6 个 `.mb`、12 个 `.sm`、6 个 `.km`）、6 个 `.rt` 配方类型定义、7 个独立机器行为脚本、2 个独立加工配方脚本。上文“未删除旧源”描述此前验证阶段；本次只清理明确完成的独立文件。
- 删除前逐文件备份并校验 ZIP 内容与源文件字节一致，删除后确认 39 个路径均不存在。可恢复备份：`/home/halo/.local/share/PrismLauncher/instances/CDPR/backups/mbd-migrated-source-20260918-151430/PTTOD-migrated-multiblocks.zip`；逐路径 SHA-256 清单：`/home/halo/.local/share/PrismLauncher/instances/CDPR/backups/mbd-migrated-source-20260918-151430/manifest.json`。
- 保留 `kubejs/server_scripts/mbd2/mbd2.js`（含烘干机、洒水器、温室、售货/订单和机械合成编码器等未迁移制作配方），`mbd2_recipes/centrifugation.js` 与 `mbd2_recipes/proxy_recipe/*`（混有小型离心机与共享工具），以及 `Custom/multiblock.js`。这些混合文件的残留不表示六种多方块运行逻辑未完成。
- **旧 Ponder 多方块教程尚未按本批运行验证视为完成**，保留 `client_scripts/ponder/scene/mbd2/`、Ponder 标签及相关资产；旧共享模型、纹理、语言文件也保留待后续关联内容核对。食品生产/容器链和其余单方块机器不在本次清理范围。
- 清理只作用于迁移参考目录，正式客户端与 Core 实现未改。此前 99 条加载配方的等效对照再次通过；本次没有重新启动游戏或重复行为测试。


## Tetra 战斗词条兼容（2026-09-18）

- 将 pack `kubejs/server_scripts/mods/tetra/effects.js` 迁为 `compat/tetra/TetraCombatCompat.java`。`ModCommonEvents` 仅在安装 Tetra 时注册 NeoForge 伤害和玩家 tick 事件；使用 `IModularItem`、`ItemEffect` 公开 API，无反射。冻结、三种龙种克制、压制、自身辐射保留旧规则；命中缓慢/虚弱、辐射和电击仍由 pack 原生 JSON 实现。
- 当前 1.21.1 移植版没有可用的 Maven/CurseMaven 坐标，因此此项采用 Gradle 官方 Ivy artifact-only 远程解析作为例外：`cdpr.ports:tetra:1.21.1-6.13.0`，使用 pack 描述符的 HTTPS 制品地址，仅 `compileOnly`。不使用本机 mods JAR、不将 Tetra 打包进 Core。下载实测 SHA-256 `a8962495c55c91e821b6bdf62b04f19d02700c47053289faeb9393b8d4823c46` 与 pack 一致；后续有正式仓库坐标时替换该来源。
- `build` 通过。隔离服停用旧脚本后，92 个 Tetra 数据对象、冻结 300 刻、龙克制伤害 10 → 12、压制约 0.2、间接伤害不触发压制及普通物品对照全部通过；`/reload` 后无重复伤害。测试专用材料验证自身辐射的等级索引 2、100 → 200 刻累积及每 600 刻的触发边界。结果 `/tmp/cdpr-tetra-core-results.json`，日志 `/tmp/cdpr-tetra-core-check.log`。
- 无 Tetra/Mutil 的隔离服启动到 `Done`，无类加载错误，日志 `/tmp/cdpr-core-without-tetra-check.log`。当前包尚无材料赋予自身辐射，测试材料只存在于隔离服，未加入正式内容。对应 0488 旧 `Tetra/effect/irradiation.js` 已验证并清理。
- 已同步当前 CDPR 实例的 Core JAR，并删除正式 pack 的旧战斗脚本，需完整重启；本机开发覆盖尚未发布。JAR SHA-256 `7da8b7301256f77be91b4b7420dc868c68787d9961849c11344af5f610d86b87`，旧 JAR/脚本/旧源备份 `/home/halo/.local/share/PrismLauncher/instances/CDPR/backups/tetra-core-migration-20260918-202921`。公开下载描述符仍指向发布版。


## 活龙采血交互迁入 Core（2026-09-18）

- 按维护者要求，使用 `compat/iceandfire/DragonBloodCollectionCompat.java` 接管 pack 的采血交互，删除 `kubejs/server_scripts/mods/iceandfire/interactions.js`。NeoForge `EntityInteractSpecific` / `EntityInteract` 处理龙本体与 CE 分体；客户端阻止 CE 分体额外交互包，服务端验证已驯服活龙、主手采血器、副手玻璃瓶及冷却后执行。
- 保留消耗各一个物品（含创造模式）、60 tick 冷却、`max(50, maxHealth × 0.1)` 伤害及可致死行为。除 `hurt` 返回值外，还验证生命值与吸收量实际下降，避免 `LivingIncomingDamageEvent` 取消伤害后仍返回成功造成免费采血。伤害被拒绝不扣材料、不发产物；重复事件由冷却拦截。
- Uranus 从 runtimeOnly 调整为 implementation，编译所用 CE 龙类的接口需要此依赖；继续从原 CurseMaven 坐标获取。兼容注册按 `ModList` 条件执行；现有 Core 元数据本来就将冰火列为必需依赖，无冰火的启动检查被加载器在依赖检查阶段拒绝，本次未改变依赖声明，不宣称支持无冰火运行。
- `build` 通过；独立 NeoForge 21.1.242 / Java 21 测试服中的 16 项真实实体/事件检查通过，涵盖三种龙本体/分体、普通交互、最低伤害、致死采血、重复事件、未驯服、尸体、冷却、错手/错物品、取消伤害。结果 `/tmp/cdpr-blood-core-test/blood-results.json`，日志 `core-check-console.log` 的 `BLOOD_CORE_VALIDATION`；临时 KubeJS 仅为测试驱动，不随包发布。客户端实际点击待维护者重启复测，488 旧源保留。
- 已将测试 JAR 安装到本机 CDPR 的原受管文件名，需完整重启。SHA-256 `e7f31e4621fceb368e1017b1f867268e78cc325f6f313eac29df71a68f66c735`；旧 JAR/脚本备份 `/home/halo/.local/share/PrismLauncher/instances/CDPR/backups/blood-core-migration-20260918-212509`。本机开发覆盖未发布，公开描述符仍指向原发布制品。


## 剩余 MBD 单方块、订单和售货逻辑迁入 Core（2026-09-18）

本批按维护者要求将注册、交互、加工行为、经济计算、服务端/客户端 UI 全部放入 Core；KubeJS 仅保存原生 schema 配方。Ponder 明确暂缓。

- 新增 13 个机器定义：dryer、mechanic_grinding_wheel、contract_executor、electrolyzer、greenhouse_builder、mechanical_craft_encoder、mortar、order_deliverer、order_generator、quality_destroyer、sell_bin、small_centrifugation、sprinkler；新增 5 个加工类型，合计 **37 个机器/部件、13 个配方类型**。两个旧定义 order_generator / quality_destroyer 的事件图本来为空，保留其原有空行为，不虚构功能。
- Core 接管手动研钵（10 次点击）、小离心机代理（100 tick、100 FE/tick、概率输出）、转速打磨/去品质/Tetra 磨砺、契约执行器烈焰人加速、温室预检/库存提取/失败回滚/样本保留、机械合成编码器过滤/宽度/空格/堵塞不吞物、洒水与除湿湿度场的建立/续期/清理。
- Core 接管 42 类订单、16 类客户、品质/多样性/声望、拍卖、拆单、四方向桌布交付和奖励包裹；共用暂存库存防止重复要求双计数，缺货不吞订单。售货箱按所有者、每天早晨结算，从内部输入槽真正取走食物才付款，保留收据品质星级、音效、标题；修复旧脚本给低价值未消耗物品重复付款的问题。
- 食物估价迁入 MbdRecipeValues/MbdFoodEconomy，保留旧 OEV 基础价格、配方依赖传播、最小替代价格、整数舍入和循环熔断。服务端登录/重载同步估价表到独立客户端；Core Tooltip 显示单价/Shift 总价、订单品质分类、奖励及售货箱 Ctrl 说明。
- SolApplePie 参考为实际 1.20.1 2.3.0 JAR（Modrinth 文件 SHA-1 `db306008033065fc4ca97f66d38183206a55d458`），已核对反编译字节码，不能用 1.18 旧源码的默认常数替代。22 个复杂度覆盖之外，令 a=(nutrition+saturationPoints/2)/2，a<5 时 a*default/5，否则 default*4*log10(a-4)+1；不计品质食物营养加成。
- 订单 tag 修正 Casualness Delight / Miner's Delight / Alex's Mobs/ Caves 的新版命名空间及葡萄/油炸食品的嵌套 tag。缺失模组的旧食物 ID 保留 optional 项，生成订单只选当前存在的类别；这不代表那些未安装食品模组的生产链已迁移。
- pack 新增 `kubejs/server_scripts/mods/mbd2/single_machines.js`：4 条加工与 6 条制作；`crafting.js` 补齐钢机壳、钢玻璃机壳、钢框架机壳 3 条制作。此前 99 条加本批 13 条，共 **112 条实际加载配方，8 个脚本**。Create 的物品应用配方必须用 `Ingredient.of('#c:ingots/steel')`，裸 tag 会被此版本 schema 误判为流体。
- 第 4 条锻钢机壳配方保留原 ID 和材料，按 `art_of_forging:forged_steel_ingot` 可用性条件启用；当前该材料不存在，因此此配方尚不可用，旧 `Custom/multiblock.js` 仅保留这一条。机械编码器旧 Quark crafter 使用 1.21 原版 crafter 替代。旧注释契约配方、其他无制作来源的机器未擅加配方。

验证与跨版本修正：

- Java 21 / NeoForge 21.1.242 构建与 **26 项 GameTest** 通过：原有多方块、代表性单机加工、编码器/温室原子性、品质/订单算术、交付拒绝与成功、售货防重复付款、湿度场生命周期、估价网络 CODEC。修复旧多方块测试结构快照需要预载相邻区块的偶发问题。
- 隔离整包服 `/tmp/cdpr-mbd-server`，127.0.0.1:**25586**：433 条 MBD 配方严格原始输入和网络往返通过；此前 **99/99** 旧配方等效对照仍通过；新增 **13/13** 配方已实际加载，电解逐 tick 消耗/输出与洒水器 500 mB/100 tick 对照通过。真实 Quality Food 组件测得普通苹果订单品质 1，二级品质苹果为 3。证据 `/tmp/cdpr-mbd-full-singles-final.log`、`mbd-singles-runtime.json`、`mbd-quality-runtime.json`。
- 隔离开发客户端 `/tmp/cdpr-mbd-client` 的已有测试存档冷启动/真实模型、六个机器 GUI、服务端价格同步与客户端 tooltip 通过；截图 `/tmp/cdpr-singles-visual/screenshots/`，日志 `/tmp/cdpr-mbd-singles-client5.log`。修复小离心机 GeckoLib 的 `geckolib_model` 名称及 `data` 包装（仅服务端测试不会触发该错误）、新版进度开关、温室英文标签重叠、中文写死标题。此环境额外从 CurseMaven 加载家具模组以验证售货箱模型；临时探针仅在 /tmp，经单独 Gradle init 注入，不随正式构建发布。
- 不把以上代表性验证表述为所有加工/食品组合均已人工实测。隔离整包旧副本仍有与本批无关的 Ice and Fire / TaCZ 重声明、Vintage rolling 配方问题；未据此声称整个包零错误。

旧源清理：

- 备份 `/home/halo/.local/share/PrismLauncher/instances/CDPR/backups/mbd-singles-migration-20260918-222832/PTTOD-migrated-singles.zip`；相邻 manifest.json 记录逐文件 SHA-256 与处理后校验。校验归档后删除 **153 文件**：18 个机器/配方类型定义、109 个已迁移模型/材质/动画资产、机器行为/配方、订单/OEV/估价独立脚本及两张订单奖励表。
- 精简 **4 个混合文件**：tool_tip.js 只移除估价/售货提示；proxy_recipe/centrifugation.js 去掉 Core 已接管的动态 MBD 配方与重复转发，保留未迁移食品配方仍调用的 Vintage 助手；money.js 仅保留未迁移怪物掉币需要的货币转换；Custom/multiblock.js 仅留缺原料的锻钢机壳。
- Ponder 脚本/结构完全保留；旧 MBD 编辑器本地化、共享 RecipeUtil、未迁移食品生产/容器和怪物掉币不算本批已完成。0488 整个目录不可删除。
- 本批总代码和机械转换资源超过 800 行。后续提交应按可复核依赖拆分：机器定义与代理加工 → 温室/编码器/气候 → 订单/经济及同步 → pack 配方与旧源清理记录；复杂 Java 逻辑再按独立模块分段，每段控制在 500 行内。当前未创建提交或发布。

- 维护者指出温室三个数字框水平偏移，已将标签与输入框统一左边界、三个输入框统一 44 像素宽，恢复 TextField 默认 2 像素内边距；隔离客户端复测截图 `greenhouse_builder.png` 确认对齐，日志 `/tmp/cdpr-mbd-singles-client6.log`。最终行为回归 `/tmp/cdpr-mbd-singles-release-check.log` 为 26/26 通过。pack refresh 与 check 通过（1645 个纳入文件、315 个元数据文件，JAR 忽略）；此前条目的文件数对应当时仓库状态。

- 最终生产构建（无临时探针）通过，已同步正式 CDPR 客户端 `mods/createdelightcore-2.0.0.5+1.21.1.jar`，SHA-256 `5ff097f87a725b62c544c9ab582813142004ac1c1b8e92e7364310a694c00e78`。旧 JAR 备份为上述 singles 备份目录内 `client-Core-before.jar`；完整重启生效，公开描述符仍指向原发布制品，本次为本机开发覆盖。
- 同步后的最终 JAR 已在 25586 隔离整包服重新启动到 Done，433 条严格配方/网络校验及新增 13 条加载再次通过，日志 `/tmp/cdpr-mbd-singles-deployed.log`；测试服保留运行。


## TACZ 自动补弹与脚本 Java 调用清理（2026-09-18）

- 维护者确认 Core 活龙采血实测正常；已备份并清理 488 独立 `Ice and Fire/collect_dragon_blood.js`，此前“待客户端确认”状态至此关闭。
- 新增 `compat/tacz/TaczEnergyReloadCompat.java`，用原生 `PlayerTickEvent.Post` 接管 KubeJS 自动补弹。仅同时安装 TACZ 与 AE2 时注册；保留 HMG22 / EMG Prototype、每 120 tick、按扩容容量折算 16,000 AE 基础每发耗能、低电量优先、每堆只取一个元件的行为。先在副本完成扣能计划，再修改原背包并返还拆分元件，避免中途合并影响尚未扣能的槽位；不足一发不扣能，满弹不消耗。
- TACZ 通过 CurseMaven `curse.maven:tacz-1-21-1-1353462:7955596` 加入 compileOnly，不打包入 Core；远程制品 SHA-1 `58a9b52ddb49d060818fd86feb97b54e07bc32c4` 与 pack 描述符一致。TACZ、AE2 声明为可选依赖。补弹中英文提示迁入 Core 语言文件；删除 pack `energy_reload.js` 及两份旧翻译文件。
- pack 创造栏图标保留 KubeJS 数据声明，使用 `minecraft:custom_data` 的 `GunId` / `AttachmentId`，移除 ResourceLocation 类加载和 TACZ setter。冰火交易 `IntRange` 改用 MoreJS 原生数字范围转换，保留等级 2 规则。对应 TACZ、冰火、Gateways、Tetra server/startup 脚本检查范围内没有剩余 Java.loadClass；未宣称整包无脚本 Java 调用。
- `build` 通过。隔离服实际枪包/AE 元件的 15 组补弹场景通过，覆盖两把枪、能量合并、不足、满弹、上限、扩容与分数耗能、堆叠、低电量优先、时序、其它枪与副手。HMG22 二级扩容电池容量 160、16,000 AE 补 2 发；一级容量 120、每发 10,666.666… AE。11 个图标的完整 ItemStack 组件与旧 setter 产物等效，交易范围转换及脚本加载通过；图标客户端显示仍待维护者复核。结果 `/tmp/cdpr-tacz-core-test/tacz-results.json`，日志同目录 `core-check-console.log`。
- `/tmp/cdpr-tacz-without-tacz`、`/tmp/cdpr-tacz-without-ae2` 两组启动均到达 `Done`，没有兼容类缺类错误；日志各自的 `core-check-console.log`。临时 KubeJS 仅为测试驱动，不随包发布；三组测试服均已停止。
- 已安装测试通过的本机 Core，SHA-256 `3b060cd6de0fff2e6304140329391c0bee633a0809722b10afcc928ae7b04c0c`，需完整重启。备份 `/home/halo/.local/share/PrismLauncher/instances/CDPR/backups/tacz-core-migration-20260918-231738`。补弹仍待客户端实测，488 混合 `TACZ/misc.js` 保留；公开 Core 制品与描述符未发布更新。


### TACZ 补弹与图标客户端确认、旧源清理（2026-09-18）

- 维护者确认扣电和 TAB 图标正常，同意清理旧源；结合已通过的 15 组补弹场景、11 个图标组件对照，两项客户端验收关闭。此确认不覆盖其它配方或冰火交易。
- 重新核对 488 `kubejs/server_scripts/TACZ/misc.js` 整份仅含补弹逻辑，此前“混合文件”描述有误。已清理该文件及 `hotai/com/tacz/guns/init/ModCreativeTabs.badiff`，共 2 文件；现行 Core/pack 运行文件未变。
- 删除前校验 ZIP 字节与原文件一致，备份 `/home/halo/.local/share/PrismLauncher/instances/CDPR/backups/tacz-verified-source-cleanup-20260918-234425/PTTOD-tacz-verified.zip`，同目录 manifest.json 记录逐文件 SHA-256。公开制品发布仍待另行收尾。


## 锻钢机壳：依据最新版旧包补齐自定义锭（2026-09-19）

- 维护者新增最新版 Forge 1.20.1 整合包参考路径 `/home/halo/gitRepo/JSI/Create-Delight-Remake/`，已写入 pack `docs/DevGuide.md`、`AGENTS.md` 及 Core `AGENT.md`。该目录只读，不参与 0488 清理；本次核对 `main` 的 `cfaaab73052274663fc89fca78c042f01375da58`。
- 最新 `kubejs/server_scripts/Custom/multiblock.js:3` 已使用自定义 `createdelight:forged_steel_ingot`，注册位于 `startup_scripts/registry_item.js:743`。此前“必须等待 Art of Forging 锻钢锭”的结论仅对应陈旧 0488，现已被这条新版替代方案解决。
- Core 新增 `createdelightcore:forged_steel_ingot`、原纹理/物品模型、中英名称、创造栏和 `c:ingots[/forged_steel]` 标签。素材源提交和原许可证随 `META-INF/licenses/forged-steel-reference.txt` 保存；未使用 KJS startup 注册。
- KJS `mods/mbd2/crafting.js` 无条件恢复钨板金块 + 自定义锻钢锭 → 锻钢机壳。`mods/createdelightcore/forged_steel.js` 迁入新版 `Create Delight/recipe.js:143-173` 的 11 条合金、熔融、浇铸、锭块互转及制板配方，沿用旧配方路径并统一 Core 命名空间。保留 30 mB 熔融下界合金 + 250 mB 废液 + 4 金属碎片 → 360 mB 熔融锻钢；每锭/板 90 mB、每块 810 mB。旧 helper 的 superheated/superheat 判断实际产生最低热量 6，按实际旧结果保留。
- 新 Create 6 将 4 个碎片展开为 4 个 Ingredient，新版 FoundryBasinRecipe 限制 3 项而拒载。Core `FoundryBasinRecipeMixin` 仅对 AlloyingRecipe 把校验上限提升至至少 4；不改变库存匹配、消耗或产量。真实铸造盆已确认可从一个槽位的堆叠消耗全部 4 个，不需要增设物品槽。
- 构建通过（`/tmp/cdpr-forged-steel-build2.log`）；隔离整包启动/reload 后 12/12 相关配方加载、标签存在（`/tmp/cdpr-mbd-server/forged-steel-runtime.json`）；铸造盆实际匹配/应用验证 3 碎片拒绝、4 碎片成功、余料 0、产液 360 mB（`forged-steel-basin.json`）。此验证不等于所有机器都已逐条完整计时运行。Prettier 与 pack refresh/check 通过。
- 本批范围为机壳所需主生产链。最新版旧包 Tetra 工具材料配置及锻梁回收等其他 Tetra 配方未据此声明全部迁移；旧写法 `rolling(..., "2x tetra:forged_beam")` 与新版仅一个 Ingredient 的 API 仍需单独核对实际消耗，未擅自改成一梁换一锭。
- 已备份并删除 0488 剩余 `Custom/multiblock.js`；备份目录 `/home/halo/.local/share/PrismLauncher/instances/CDPR/backups/forged-steel-migration-20260919-001703`，最新旧包源码未修改。正式客户端已同步新 JAR，SHA-256 `1a28fa637fe7aec17c39c07cd146a7c6d1929b6db2934f7a2dd01d3765d3f278`，完整重启生效；公开下载描述符未改。


## 0488 难度、怪物掉币与交易白名单迁入 Core（2026-09-19）

- 本批机制在 Core 原生事件/API 实现，不新增 KubeJS 玩法监听或 Java 类加载。参考 0488 剩余源及最新版旧包 `cfaaab73052274663fc89fca78c042f01375da58`；保持 0488 的数值难度，未启用新版旧包的 6 阶等级、全掉落倍率或旧任务奖励。
- `compat/improvedmobs/ImprovedMobsCompat` / `DifficultyRules` 接管难度读取/调整、死亡惩罚和 5 条冰火难度掉落。死亡比例由 40% 在难度 0–250 线性降至 10%，扣除量按 5 向下取整；尊重 `disableRankChange`，难度最低 0。鸡蛇难度 100 掉眼 50%；巫妖/骑士难度 150 掉冰龙钢 10%，仆从/食尸鬼为 5%。AoF 的两条旧掉落按既定放弃决策不启用。
- 对照目标 Improved Mobs 1.15.2 / TenshiLib 2.2.5：通过 NeoForge 附件访问，调整后与重生后发送原生难度同步包。初版错误使用了兼容旧存档的附件，相关重生验证没有覆盖原生指令；该结论已被下文“原生指令难度清零修正”推翻。当前使用模组的正式附件接口，由 TenshiLib 复制难度，Core 保留禁改标记。两个依赖均从 CurseMaven compileOnly 解析，IM 保持可选。
- `compat/lightmanscurrency/MobCurrencyDrops` 接管怪物货币：真实玩家击杀、排除刷怪笼、仅敌对/攻击中 Mob；生命、攻击、护甲/韧性和 Tetra `createdelightcore:greedy` 加成沿用旧式。按 LC 主货币链拆分实体掉落，不直接存钱包。小于一铜的部分按旧 Rhino→long 行为截断；采用最新版旧包的缺攻击属性回退（末影龙 10、凋灵 8、其他 4.5），无 Tetra 时不加载其 API。
- `compat/lightmanscurrency/TraderWhitelist.unlock(player, trader, item)` 提供通用解锁能力。管理员/后续新任务可用 `/createdelightcore trader unlock <player> <traderID> <item>`，要求权限 2；只修改售卖物品匹配的现有交易白名单，重复操作不叠加，更新后标记规则持久化/同步。缺失交易机安全返回；黑名单保持原样，避免 LC `addToWhitelist` 清空黑名单。旧 7/10 号机及 FTB Quest ID 未恢复，新任务触发设计仍待编写。
- Java 21 build 与隔离 GameTest：不装 Tetra、装 Tetra 6.13.0/Mutil 6.3.1 两组均 **33/33** 通过。覆盖真实 death/respawn、取消死亡/禁改/暂停/下限、5 个实体的阈值及固定随机种子分布、铜币实际物品/数量、假玩家/刷怪笼/被动生物排除、贪婪数值计算、白名单隔离/幂等/黑名单保护。测试玩家使用真实 ServerPlayer 与空网络传输，未验证客户端 HUD 渲染；不宣称整包所有模组零错误。日志 `/tmp/cdpr-mechanics-gametest3.log`、`/tmp/cdpr-mechanics-tetra.log`；重现测试需在开发 runtime 加入上述 IM/TenshiLib（和可选 Tetra/Mutil）。
- 缺 LC/IM/Tetra 的隔离启动被 Create `Found unused register callbacks` 阻塞，随后触发注册表回滚错误；不是缺类错误，但不能据此宣称无 LC 完整启动通过。日志 `/tmp/cdpr-mechanics-optional.log`（Gradle 虽返回成功，游戏实际未启动）。

### util 的职责核对

- `reactor.js` 的产热/液冷/流量/发电/红石/核爆已由 `MbdReactor` 接管；`SeasonUtil.js` 的湿度场和下方落点由 `MbdClimate` 接管，本轮回归包含其已有 GameTest。0488 中两者均无剩余调用方，无需重复写 Java 包装。
- `metallurgy.js`、`brewinandchewin.js`、`refurbished_furniture.js` 是纯配方生成助手，适合配方层。家具已在 pack `mods/refurbished_furniture/recipes.js`、`mods/someassemblyrequired/recipes.js` 局部适配；冶金/酿造的剩余流水线应随具体材料、饮品配方继续迁移，不能因为 helper 名字不存在就恢复旧 API 或宣称全线完成。
- `trade.js` 的 LC 查询由 Core 原生 TraderAPI 使用取代；村民交易数据的剩余迁移单独处理，本轮未增加旧 TradeUtil/空木棍监听，也未改动已存在的冰火交易。Vinery 发酵桶重做和熔融金属遇水凝固属于此前 Core 单独缺口，不等同于上述配方助手。
- 本批以难度/经济机制为最小独立变更，新增机制与测试约 400 行；后续冶金/酿造配方及村民交易分批验证。当前未提交、未发布远程制品。

- 最终补齐原生掉币去重：0488 `[loot.entities].enabled=false`，当前实例为 true；Core 通过 LootTableLoadEvent 仅清空 LC `loot_addons/entity/tier1..6` 和 `loot_addons/boss/tier1..6`，不再依赖本机配置关闭默认掉币。其它来源的掉落保留。最终 **34/34** GameTest 通过，额外验证 12 张实际加载的表多次抽取均空、原版箱子掉落保留；日志 `/tmp/cdpr-mechanics-final.log`。不装 IM/TenshiLib/Tetra 的场景 **29/29** 通过（`/tmp/cdpr-mechanics-no-im.log`）；缺 LC 的前述阻塞仍保留。
- 最终生产 build 通过；已覆盖本机受管文件名 `mods/createdelightcore-2.0.0.5+1.21.1.jar`，内部开发版本保持 0.0.0，SHA-256 `4d5994f875ed42fe1c79a116789a72e3913b91dca2c5f2f8fa6d6709ce853c06`。需完整重启，未发布远程制品，描述符仍指向旧发布版。
- 备份及验证日志：`/home/halo/.local/share/PrismLauncher/instances/CDPR/backups/mechanics-core-migration-20260919-134510`。已校验 ZIP 与原文件 SHA-256 后清理 0488 `util/reactor.js`、`util/SeasonUtil.js` 共 2 文件。其余本轮难度/LC 旧源保留供客户端显示及新任务衔接验收；配方和村民交易助手保留供后续按调用方迁移，不清理最新版旧包源码。


## 0488 基础交互与工业配方支撑（2026-09-19）

- `BasicInteractions` / `ChainCasingInput` / `ChainCasingModifierPayload` 接管空手管道开口、Shift 反面和 Alt 轴/传送带连锁套拆壳。轴同方向每侧最多 64 格，传送带每侧 32 段且只处理同一 controller；保留最少两口，跳过未加载区块，逐邻块发布保护检查。只同步输入修饰键，交互位置/距离仍通过原版交互包验证，登录清理旧 Alt 状态。
- 欢迎语和 `DonorLogin` 移入 Core；可选 FTB Ranks API 读取服务端 `donate_list.json`，不执行标题字符串命令，缺模组/文件安全跳过；旧任务书不恢复。真实标题配置/客户端显示尚待实测。
- `LegacyStructureLoot` + `legacy_structure_loot.json` 机械提取旧 weighted pools，45 组规则/318 表 ID；实际整包匹配 307 表。缺表/缺物品跳过，不改原表其它池。0488 采掘/铁镐等级/扳手回收规则转 Core datapack block tags，动态 MBD 块使用 optional 引用。
- `VibratingOutputLimitMixin` 与 `VacuumizingOutputLimitMixin` 将对应配方校验上限对齐实际 9 槽库存；不裁掉旧 5–7 项矿簇产物。只有安装 Vintage 才应用，普通 Create BasinRecipe 不受影响。
- 注册 `rolled_polymer_sheet`：新版 Create Addition rolling 不支持序列装配，pack 保留轧制→部署两步采血器工艺。所有工业配方仍在 pack KJS，Core 不承载配方魔改脚本。
- Java 21 build 通过；**29/29 GameTest** 通过，含真实 Vintage 7 项输出、管道边界、轴套壳/拆壳、战利品池隔离。隔离整包服务端启动/reload 无 KubeJS 错误，新增 31 条（含 MBD 代理 2）、矿簇振动 7 条及 307 个结构表实际加载；上游 Vintage rolling 与 Integrated Farming 旧数据错误不属于本批，未称整包零错误。客户端连锁套壳视觉/真实 donor 配置未验收，保留相应旧源。
- 旧包较新参考提交 `cfaaab7` 仍引用已移除的 CEI ink，没有可直接沿用的替代物；该配方及缺模组生产线暂缓。10 个已验证旧源已按 pack 台账清理；备份/日志/清理 manifest 位于 `/home/halo/.local/share/PrismLauncher/instances/CDPR/backups/industrial-core-migration-20260919-134748`。本机 jar 已包含本批代码（另一个会话后续掉币更新被保留），需完整重启，远程制品未发布。


## 原生指令难度清零修正（2026-09-19）

- 维护者实测原生个人难度设为 114514，kill/重生后查询为 0。此为 Core 初版迁移错误，不是预期死亡惩罚；此前 34 项测试同样直接读写错误附件，不能证明原生指令链正确。
- 使用 Vineflower 1.12.0 追踪实际 IM 1.15.2 JAR 的 `ImprovedMobsNeoForge`、`ImprovedMobsAttachments`、`DifficultyHandler`、`PacketHandler` 和 TenshiLib 的 `AttachmentTypeWrapper`。原生指令/怪物计算/HUD 使用 `improvedmobs:player_difficulty`；初版 Core 使用了旧迁移用 `tenshilib:player_difficulty`。IM 每次玩家入世界会把后者覆盖到前者，导致错误旧值清零当前值。正式附件本来就由 TenshiLib 的 transferHandler 在重生时复制，先前“缺 copyOnDeath”归因不成立。
- Core 难度读取/修改及掉落条件改用 `ImprovedMobsAttachments.PLAYER_DIFFICULTY.get().get(player)`。移除错误的旧附件 Clone 复制。在 IM 入世界导入之前，只在正式数据不存在时导入旧数据，然后移除旧附件，避免重生/跨维度/重连反复覆盖；正式数据优先，不用旧值猜测恢复已丢失难度。禁改标记仍随 Clone 保留。
- 新回归通过原生命令设置 114514、执行 kill、真实 PlayerList.respawn，验证扣除 11450 后为 **103064**；同时验证旧零值不覆盖正式值、原生 PLAYERMEAN HUD 包为 103064、NBT 保存/加载一致，以及仅有旧附件的存档按 75 正确迁入。其它测试也改为读取正式附件。**35/35 GameTest 与 build 通过**，证据 `/home/halo/.local/share/PrismLauncher/instances/CDPR/backups/improvedmobs-native-fix-20260919-140145/regression.log`。
- pack `config/improvedmobs/client.toml` 的 `Show Difficulty` 从 false 改为 true；位置仍为右上角 X=5/Y=20。原生渲染在 F3 调试界面开启时隐藏，未新增 Core HUD 或客户端脚本。
- 已安装修复 JAR，SHA-256 `1a6099ea73d38e5206cc764e960ef7c7923ffd30b2522e5817dd79e86620d968`；备份 `/home/halo/.local/share/PrismLauncher/instances/CDPR/backups/improvedmobs-native-fix-20260919-140145`。需完整重启，客户端实际显示仍待维护者复测，旧难度脚本继续保留。未修改玩家存档、未自动恢复测试难度、未发布远程制品。


### 基础交互客户端确认与旧源清理（2026-09-19）

- 维护者确认本批 PACK 测试无问题并授权清理旧源；管道、Alt 连锁套拆壳和本批登录交互的客户端验收关闭。此次确认仅用于本批，不扩展到其它会话的难度/HUD 等待办。
- 已核对 Core 接管情况与引用，备份校验后删除 0488 `kubejs/server_scripts/Custom/encase.js`、`pipe.js`、`player_login.js` 共 3 文件。旧任务书按既定决策不恢复；可选赞助头衔由 Core `DonorLogin` 接管，维护者未单独说明真实 donor 配置测试，因此不记为该可选配置已实测。
- 备份：`/home/halo/.local/share/PrismLauncher/instances/CDPR/backups/basic-interactions-verified-cleanup-20260919-141315/PTTOD-basic-interactions.zip`；相邻 `manifest.json` 保存逐文件 SHA-256、归档校验和删除结果。
- 本次只清理旧源并更新台账；PACK 当前脚本/JAR 不变。缺模组/缺原料的混合配方、食品助手及其它批次旧源继续保留，最新版旧包参考仓库未修改。


## 难度与 LC 实测确认、旧源清理（2026-09-19）

- 维护者确认修正后的本机测试无问题，并明确授权删除这批已迁移旧源；本批客户端验收不再待定。缺 LC 的可选启动场景仍保留此前 Create 注册错误阻塞结论，不据此标记通过。
- 经引用核对、ZIP 备份及逐文件 SHA-256 校验，删除 0488 `PTTOD/kubejs/` 下 7 份旧脚本：`server_scripts/Improved Mobs/{difficulty,utils,loot,player_death}.js`、`server_scripts/Lightmans Currency/{entity_loot,white_list}.js`、`startup_scripts/utils/money.js`。通用白名单已由 Core 接管，旧任务 ID 已废弃，新任务触发设计仍待编写。
- `startup_scripts/custom/loot_data.js` 仍被未迁移的 `client_scripts/tool_tip.js:123、130–131` 用于难度掉落提示，暂时保留。未迁配方、村民交易和其它客户端内容不因此标记完成；最新版旧包参考源未修改。
- 备份与删除/保留清单：`/home/halo/.local/share/PrismLauncher/instances/CDPR/backups/improvedmobs-lc-verified-cleanup-20260919-141225`（`PTTOD-candidates.zip` 保存全部 8 份候选原件，`manifest.json` 记录实际删除 7 份及保留原因）。本轮仅清理旧源并更新记录，沿用已通过的玩法验证，不重新构建或覆盖 Core JAR。


## 0488 食品配方与交互收尾（2026-09-19）

- `PackBlockInteractions` 接管边境碎片召唤与 capsid 复制防护，注册表查找保持 Alex's Mobs 可选；尊重取消事件、主手、建造权限，保留旧配对规则和创意消耗行为。删除 pack 对应 KJS 机制。
- `MigrationTooltips` 使用原生 Create 序列组件，仅补普通中间物品提示；难度掉落提示和 `ImprovedMobsCompat` 共用 `DifficultyLootRules`，不改变阈值/概率。IM 缺失时跳过提示。`CauldronFeedback` 在可选 Fruits Delight 下补操作者音效/挥手，物品/方块变化仍归模组；适配当前 `*_jam_cauldron` ID。
- Core Java 21 build 通过。最终隔离整包服务端 **213/213**：103 TACZ/冰火配方、89 食品配方/既有序列、7 原生交互、14 反馈/提示生成。Core 新增机制分支验证通过；既有 35 GameTest 本轮未重跑。客户端声音/提示渲染仍待确认，保留相应旧源，不宣称可选模组缺失组合均已重新启动。
- 配方继续在 pack KJS，详见 pack `docs/MIGRATION_STATUS.md` 本轮记录；当前已清理 3 个独立配方旧源及混合文件的 8 条切片。测试驱动、日志、SHA-256 manifest 归档 `/home/halo/.local/share/PrismLauncher/instances/CDPR/backups/recipe-core-finish-20260919-161013`；非发布内容。
- 本机安装完整当前工作树构建 JAR，SHA-256 `51e0e37812a3ead3105d6f4e7db2e2f79db76f2220a20f975f321093d9ecc751`，旧 JAR 已备份；需完整重启。保留已有难度原生附件修复及其它会话改动，未发布制品、未更新远程描述符、未提交。

- 后续独立机制缺口：Create 分液机原生返回一个物品，不能只放宽 `EmptyingRecipe` 上限来恢复下界乐事双物品分液；需副产物暂存/输出及持久化/满槽验证。当前旧配方继续保留，未改机型或丢弃产物。定向反编译证据见本批归档 `drain-source`。


## 分液多产物与自定义 Tetra 材料（2026-09-19）

- `EmptyingOutputLimitMixin` / `ItemDrainOutputsMixin` / `ItemDrainBlockOutputsMixin` 为 Create 分液池提供最多 4 个物品输出。额外结果存入 `CreateDelightDrainOutputs`，沿原生出口依次输出；保留方向，排队产品从中点之后开始移动，不再触发加工。模拟不消费/排队；普通单输出走原实现。getter 在新输入/手工取物前衔接已生产的队列项，保存读取和拆除保留所有物品。
- `FluidHelperOutputsMixin` 补全手持倒罐路径，所有剩余物品返还玩家，满包时沿原生库存逻辑落地；Creative 行为保留。`ItemDrainCategoryMixin` 显示额外产物，JEI 缺失时仅跳过分类 UI mixin，服务端分液支持仍生效。分液规则在 Core，具体食物配方继续归 pack KJS。
- `ModItems.PALE_STEEL_NEEDLE` 原生注册 `createdelightcore:pale_steel_needle`，64 堆叠，普通材料纹理/模型/中英名归 Core。维护者明确禁止使用 Tetra 物品命名空间。来自低版本新包自定义注册，不是临时占位物；Tetra 模块 JSON 与专用模块贴图仍用 Tetra 数据命名空间，未发明原包没有的图纸。缺 Tetratic 的 Better Combat rapier 映射继续暂缓。
- pack 按最新旧包 `cfaaab73052274663fc89fca78c042f01375da58` 的实际替换恢复两道 Gateway，移除过时 AoF 阻塞；唯一仍缺依赖的原三道门为 Dreadsteel 奖励门。维度注册表确认木卫二缺失，维护者明确继续暂缓木卫二相关生成；Core 结构实体边界防护不冒充生成维度限制。
- Java 21 build 与隔离整包 **129/129** 检查通过（13 分液、26 Gateway、1 Tetra 原生模块数据、89 食品配方回归）。实际保存重建、满槽/模拟、拆机、四产物及手持旁路均覆盖。旧 GameTest 本轮未重跑；客户端 JEI/Tetra 渲染和完整通关未宣称验收。
- 本批复杂机制约 250 行，配方/门 JSON 是独立机械数据批次。旧源清理 4 文件及 2 混合文件局部见 pack 台账；备份证据 `/home/halo/.local/share/PrismLauncher/instances/CDPR/backups/drain-core-20260919-164446`。本机已安装完整工作树构建 SHA-256 `d249a2777483a216d7545b96bc1f0f5e7424194a41b314c1ec1555394db0c1ae`，需完整重启；远程制品/描述符未更新，未提交其它会话工作。


## Dreadsteel 同 ID 条件占位（2026-09-19）

- 按维护者授权，由 `DreadsteelFallback` 在未安装 `dreadsteel` 且 ID 空闲时注册 `dreadsteel:dreadsteel_ingot`；普通 64 堆叠材料，使用 Core 翻译和条件资源包中的原版下界合金临时纹理。正式模组存在时停止注册、资源挂载与占位提示。此项为原模组 ID 兼容例外，本包普通自定义材料仍归 `createdelightcore`。
- 本包永寒门恢复 5 波和奖励（最终 2 锭）；不注册虚假装备，装备和 Tetra 等分支保留。木卫二生成、Ponder 继续暂缓。

- Java 21 完整 Core build 通过；隔离 NeoForge 21.1.242 服务端验证 **24/24**：缺模组 6、存在同 modid 的测试模组 6、永寒门 12（配方/组件、5 波原生实体创建及属性、全部奖励）。同一份缺模组时保存的 7 个锭在加入测试模组后解析为其注册物品；占位 tooltip 和资源包退出。存在模组验证使用测试夹具，不等于未来正式 Dreadsteel 版本兼容验收；客户端贴图/提示渲染与人工完整通关尚未验证。测试夹具初次包名冲突已修正，最终两模式通过，未修改 Core 来绕过测试。
- SHA-256 校验备份后清理 0488 永寒门 JSON 和最后仅剩该门的 `Gateways/gate_pearl.js`，共 2 份完整旧源；Dreadsteel 装备/Tetra/创造栏混合分支仍保留，最新低版本参考仓库未改。备份、清理 manifest、测试夹具/脚本、结果和日志：`/home/halo/.local/share/PrismLauncher/instances/CDPR/backups/dreadsteel-placeholder-20260919-172618`。隔离服已停止，两个测试辅助模组已移出隔离 mods，未进入本包。
- 已部署本机 Core，SHA-256 `a056af81b39cb9fda36ebf687f3449c4465eea5cb12098c321f5e56341be518b`；须完整重启。未提交、未发布远程制品、未更新下载描述符。相关 JS/JSON Prettier 与 diff 空白检查通过。


## 默认配置、交易与客户端残留（2026-09-19）

- 按维护者要求，机制/功能归 Core、KJS 保留配方；订单、采购、售货箱已经在上一轮 Core `MbdOrders/MbdOrderDelivery/MbdEconomyEvents` 完成，本轮不重复实现，也不把此前 26 项 GameTest 冒充本轮重跑。Alt 连锁机制同样早已完成，本轮仅补情境提示。
- 反编译核对 NeoForge 21.1.242 `ConfigTracker` / `ServerLifecycleHooks`：SERVER 配置可从根 `config/` 加载，既有世界 `serverconfig/` 同名文件优先。将本包共享服务端配置放根 `config/` 并加入跟踪白名单；更新能分发文件，但不擅自覆盖玩家存档或保证更新器覆盖用户自行修改的配置。`defaultconfigs/` 不承担既有世界更新。
- 迁入 Create、Diesel Generators、Ore Excavation、Sophisticated Backpacks、Vintage Improvements 的支持项；Quality Food luck/18 条农田规则、Core 关闭掉落报告、FTB 禁用死亡/世界内路标；LC 现版 `.txt` 配置恢复原币替换、战利品币种、ATM/终端/领地价格等，原生生物掉钱关闭以免与 Core 重复。保留新增 schema 设置。旧 22 份 defaultconfigs 中 8 份是空模板，无行为待迁。
- 村民交易迁到 Core 原生 Java + JSON，54 条候选中当前 49 条可用；保持原 MoreJS 的替换范围、次数 16 / XP 2 / 倍率 0.05、双输入与缺项保护。移除目标 pack 5 份交易逻辑：farmersdelight/bakeries/vinery/iceandfire 的 trade.js 与 lightmanscurrency/economic.js。LC 数据按实际 ItemStack codec 改为 `id/count`，保留 10 家商店的 130 条交易；银行家 23、收银员 22、流浪商人 5 条配置正确加载，后者使用 `config/trades/minecraft/` 正确路径。
- 新版 LC 摇奖机 JSON 把 Weight 当百分比，旧数据能加载但交易无效。Core 对显式 `createdelightcore:relative_weights` 标记转换为相对概率，保留 10:5:1:500:1500:1000 比例，普通未标记数据不变。交易价格、限购/波动/白名单规则由原生系统继续处理；附魔书 EnchantmentTag 和地图 Decoration 修正为新版格式。
- 客户端补入 Core：21 个物品的 Shift/Ctrl 说明、真实 `create:banktank_air`/Quality Food 组件提示、Better Combat 首次按键提示（按下沿发包，死亡后标记保留）、Alt 情境提示、边境碎片 JEI 说明、10 种 Create Deco 币堆/币隐藏和牛奶流体展示。移除 freezing catalyst 对其它兼容模组的要求，仍尊重用户/分类开关。实际 GUI 渲染未验收，相关 0488 旧源继续保留。
- Java 21 完整 Core build 通过；隔离 NeoForge 21.1.242、253 模组服务端 **507/507**（49 条实际 MerchantOffer 内容/次数/XP、10 家商店的注册/有效交易/原价/规则、6 个摇奖概率、12 组原生村民 JSON 数量、LC 生效开关/价格、冷冻触媒）；reload 后数量不重复。配置支持项启动后回读 **330/330**。测试辅助类仅在隔离 JAR，未进入本机发布 JAR。最终 reload 无 KubeJS ERROR；Vintage rolling / Integrated Farming 上游错误仍存在。
- SHA-256 核对备份后清理 0488 **13 文件**：4 份完整等效配置（Create/Diesel/Ore Excavation/Vintage）、8 份空 SNBT 模板、已无调用者的交易工具脚本。Sophisticated Backpacks/QF/Core 混合配置、LC 缺项商店及客户端旧源保留；最新旧包参考仓库仍只读（核对提交 `cfaaab73052274663fc89fca78c042f01375da58`）。
- 缺模组、旧 NBT、不同语义和当前偏好冲突全部汇总到 pack `docs/MIGRATION_REMAINING_DECISIONS.md`，其中 LC 跳过 68 项，未擅自替换。Ponder / 世界生成继续暂缓。
- 备份/清理 manifest/测试脚本/结果：`/home/halo/.local/share/PrismLauncher/instances/CDPR/backups/config-trade-client-20260919-200105`。已部署本机 Core，SHA-256 `e0c1ec6a6eec86a7f222731aedb5e10bd7f3a3287aa44321e81b15f6e24eaddb`；需要完整重启。未提交、未发布远程制品或改下载描述符。审阅时按配置 → Core 交易/原生数据 → 客户端三阶段拆分；大篇幅 JSON/TOML 是机械数据转换，复杂逻辑单独审阅。
- 本批收尾：`devtool.sh refresh` / `check` 通过（bkmpw 0.2.2，315 份元数据），相关范围 diff 空白检查通过；全工作树检查另报既有 `config/c2me.toml:183` 行尾空白，未改动该无关配置。隔离服已停止，测试证据归档。


## Dreadsteel 全部缺失物品的同 ID 占位（2026-09-19）

- 维护者明确选择“先同 ID 占位”，沿用原锭的兼容方式；新增四件护甲、镰刀、盾牌、默认/白/黑/青铜四种外观套件共 10 项，连同原锭共 11 项。均保留 `dreadsteel:` 原 ID；装备占位最大堆叠 1，锭和套件为 64。
- 占位使用普通 Item 和临时原版图标，名称/提示明确没有装备属性、战斗技能或染色功能；未添加可消耗龙钢装备换占位物的升级配方。Dreadsteel 装备完整功能继续待适配，不能把注册存在当成功能等效。
- 检测到正式 `dreadsteel` modid 时退出全部注册和条件资源包；其它提供者已占用的 ID 也不重复注册。占位名字不写入 ItemStack 自定义名称，已有物品由原 ID 接管。
- Java 21 完整 build 通过；隔离 NeoForge 21.1.242 服务端缺模组/存在同 ID 提供者测试各 **58/58**，合计 **116/116**。覆盖 11 项 ID/堆叠/名称所有权/提示、条件资源与服务器数据包边界，及缺模组保存后加入提供者的 11 个 ItemStack 解析。存在模组场景用测试夹具，不能代替未来正式版本验收；客户端图标实际渲染未验收。
- 两个测试辅助 JAR 只用于隔离服，已移除，隔离服已停止。混合旧源仍保留，未把占位登记成旧装备功能已迁移。备份和验证证据：`/home/halo/.local/share/PrismLauncher/instances/CDPR/backups/dreadsteel-ad-astra-20260919-214903`。
- 已部署本机 Core，SHA-256 `f5288bda227ecca3f81dc56ebca31e46d67bc114758ab89a4102434cff8ca490`；需要完整重启。未提交、未发布远程制品或修改下载描述符。
- 本批 Core diff 空白检查、11 个条件模型 JSON 与部署 JAR 无测试类检查通过；pack refresh/check 通过。


## Ad Astra 失效交易退出迁移队列（2026-09-19）

- 维护者确认 Ad Astra 后续已经移除。只读核对最新版旧包 HEAD `cfaaab73052274663fc89fca78c042f01375da58`：`config/lightmanscurrency/PersistentTraders.json` 第 2723/2745/2767 行仍有 desh/ostrum/calorite 锭出售，第 3720–3984 行仍有对应原矿/粉碎矿/污浊粉/精炼粉回收，共 15 条，与 0488 一致。不能因旧交易 JSON 残留就判为需要重新注册这些材料。
- 最新旧包 `register_crushed_metal.js` 仅注册 titanium，`register_metal_dust.js` 仅注册 tin/silver/titanium；没有找到这三种 Ad Astra 矿物的替代配方/映射。遵照维护者移除决定废弃这 15 条，不映射到无关矿物、不加入同 ID 占位。
- 当前 1.21 目标商店、KJS 配方和 Core 注册本来就不包含这些交易/材料，本轮无目标玩法删改。备份并 SHA-256 校验后，仅从 0488 混合 PersistentTraders 删除 15 条失效旧交易，其它条目原样保留；最新版参考仓库未修改。清理 manifest：`/home/halo/.local/share/PrismLauncher/instances/CDPR/backups/dreadsteel-ad-astra-20260919-214903/ad-astra-retirement-manifest.json`。
- LC 待决定项由 68 降为 **53**（32 条缺物品/流体、21 条旧 NBT），另有 5 条 Core 村民交易因缺项自动跳过。上一批 68 条是当时快照，以新待决定清单为准。Dreadsteel 同 ID 占位仅关闭“ID 缺失”，完整装备功能仍未实现。
- 外部只读调查超时，改由本地只读代理完成定向对照；未将超时调查当作证据。旧包是 shallow checkout，本结论针对上述 HEAD 文件状态，不声称追溯了完整删除历史。


## 待办清单具体化与统计纠正（2026-09-19）

- 展开旧 Tag 内容后发现 8 张 Tetra 商店卷轴中 5 张明确来自已放弃的 AoF，退出待决定清单；未更改运行商店（此前均未迁入），旧混合源尚未清理。LC 真正未完成项应为 **48**（32 缺物品/流体 + 16 组件适配），另有 5 条 Core 村民缺项交易。
- 旧 salwayseat 文件明确 mode=BLACKLIST，7 种 Vintage Delight 罐头保留原版进食规则，其它食品放宽满饱食进食。此前“7 种罐头白名单”描述错误，以本条与待决定清单为准。
- 钱包、3 张非 AoF 卷轴、AE 组合包、2 条金钱修补书、6 种炖菜具体内容已写入 pack `docs/MIGRATION_REMAINING_DECISIONS.md`；这是适配欠项，不应笼统要求维护者决定 NBT 怎么转。配置取舍补充实际旧/新数值与玩家可选择的行为。


## 配置定案与 LC 组件交易收尾（2026-09-20）

- 维护者明确：低耦合缺模组/物品的混合分支挂起；Bakeries 等后续升级、graycottonseed 计划自写但先挂起、Sol Carrot/Sol Apple Pie 后续处理。Ad Astra/AoF 沿既定移除决定；Dreadsteel 仅同 ID 占位。现有物品的组件适配直接完成，不再列成要求维护者决定的玩法选项。
- 配置采用现版品质算法、Ecliptic 每节气 7 天（168 天/年）、当前 Supplementaries。暂保留 Always Eat 对所有 FOOD 放宽满饱食进食；**旧七种 Vintage Delight 罐头例外的原因是进食与放置冲突，后续单独处理，本轮解除限制不代表修复放置冲突。**
- Xaero 恢复旧锁北、群系/时间信息与 HUD (-1,-2)。`config/xaerohud.txt` 原先被忽略，本次加入配置跟踪白名单，以便更新分发；未覆盖其余地图个人设置。
- 补回 **16 条组件交易**：4 钱包自动兑换升级、3 Tetra 图纸卷轴、1 AE 简易存储组合包、2 金钱修补 I 附魔书、6 种迷之炖菜。使用当前原生 `wallet_data`、`scroll_data`、`container/custom_name`、`stored_enchantments`、`suspicious_stew_effects`，保留价格/数量/交易次数/XP；功能没有塞回 KJS。
- Core 新增 `WalletUpgradeGuard`，只作用于 utility_trader 的钱包换钱包交易：旧 IgnoreNBT 会忽略输入钱包内容，现于原生扣物前拒绝装钱、扩容、附魔或重命名钱包并给提示。空钱包及打开过的空钱包正常升级，产物开启 AutoExchange。
- Java 21 完整 Core build 通过。隔离 NeoForge 21.1.242 服务端 **130/130**，reload 后同样通过：原生钱包实际成功/拒绝/钱物不变/不足付款、卷轴图纸存在及放置提供图纸、AE 箱子放置后的四种物品及数量、实际村民报价/次数/XP、书的真实附魔、炖菜组件与实际吃下效果、全部物品保存重读。测试类只进隔离服。
- 逐文件 ZIP/SHA-256 核验后，从 0488 混合 JSON 移除这 16 个已验证旧 Tag 条目及此前放弃的 5 张 AoF 卷轴，其他混合内容继续保留。备份/测试/清理 manifest：`/home/halo/.local/share/PrismLauncher/instances/CDPR/backups/trade-components-20260919-234820`；最新版旧包参考仓库只读。
- 已部署本机 Core，SHA-256 `6a683c058a4ba9ff8eb95e581ddad6a630a17edd34c0a0093f865e179e87b8e9`。需要完整重启，未提交、未发布远程制品或更新下载描述符。地图与提示实际 GUI 渲染仍待客户端确认。


## 品质生长抗性与等效作物交易补齐（2026-09-20）

- 核对旧 Core 基线 `8b26efc` 的 `CropGrowthHandlerMixin` / `GrowthDetectorItemMixin` 后确认：旧版不仅调整收获品质，还确实改变作物实际生长。当前 Quality Food fork 已有收获品质的环境概率修正，但 Core 缺少实际生长注入。本轮补齐，未改现版 Quality Food 概率算法。
- Core 用当前 NeoForge/EC 注入点调整原生季节、湿度判定；保留骨粉与自然生长使用各自基础概率、温室/雨水递归和死亡处理。铁/金/钻石抗性系数为 0.25/0.5/1，按 `系数 + (1 - 系数) × 原概率` 计算，普通品质不变；未来高于钻石的品质按 1 封顶。生长探测器同步修正，湿度递归只在最外层加成一次。只有 Ecliptic Seasons 和 Quality Food 同时存在时启用新 mixin。
- 恢复品质作物提示，沿旧版节气物品标签识别范围，使用当前品质组件；提示说明抗季节/湿度生长惩罚，不混同于仅提高收获品质。
- 恢复 **5 条种子交易**：白菜/茄子/茴香/韭菜改用 Dumplings Delight 同用途种子，茶种子改用 Youkai’s Homecoming；保留 5 铜币/个。核对运行注册表、作物注册、种子回落、茶叶及制茶配方，不只是按名字替换。
- Core 农民交易恢复 **3 条**：Dumplings Delight 茄子、葱、白菜各 16 个换 1 铜币，原次数 16 / XP 2 保留。候选 54 条中当前 52 条有效，剩 canola / cream 两条缺项跳过。保留原组 replace 范围：白菜所在第二级不会删除模组原有交易；测试明确区分原生报价与新增 Core 报价。
- LC 当前 10 家商店含普通交易 **143 条**、摇奖 1 条；种子 43 条，所有原生交易有效。银行家 25、收银员 28、流浪商人 5。原 LC 48 项欠账中 16 条组件适配和 5 条替代种子完成，剩 **27 条**低耦合商品/流体交易挂起。不可将茶种子替代视为 Farmer’s Respite 茶流体也已存在。
- 艾草当前实际注册表无同用途种子/作物；最新版旧包仍是自定义注册，不能用无关种子替换，继续挂起。缺项以村民逐条跳过、订单可用物品/标签筛选及脚本存在性检查隔离，不会阻断已迁移的订单、采购、售货箱和可用交易。Bakeries / graycottonseed / Sol 系列按维护者决定挂起。
- Sophisticated Backpacks 旧 `voidAnythingEnabled` 已改名为 `voidAlwaysEnabled`，当前基础/高级虚空升级均 true；无需再补机制。配置定案、罐头进食/放置冲突备注见 pack 当前决定清单。
- Java 21 完整 build 通过。最终隔离服 **交易 164/164、品质生长 105/105**，启动与 reload 后均通过。交易覆盖新增种子可种植、三条农民实际报价、钱包附魔/内容/扩容/命名保护、16 条组件交易；品质覆盖普通/铁/金/钻石、季节入口、湿度阈值、探测器组合概率与递归不重复加成。新 mixin 在实际 EC 0.13.9.1 JAR 注入成功；未将客户端 GUI 渲染或真实玩家完整游玩声明为已验收。
- 首轮扩展测试把模组原生白菜报价也算进 Core 唯一性检查，产生两条失败；已按报价来源纠正测试，保留原生报价。LC 普通 `/reload` 不重读磁盘 PersistentTraders，新增内容通过完整重启验证；不能用旧商店缓存当新数据验收。
- 五条种子旧源经独立 ZIP 与 SHA-256 校验后从 0488 混合商店 JSON 精确移除，其他分支保留。连同前批，本轮清理 21 条已迁移旧交易及 5 条放弃的 AoF 卷轴。最新旧包参考仓库未修改；客户端旧源仍待实际渲染验收。
- 隔离服已停止，测试辅助 JAR/脚本已移出隔离服且未进入正式包。全部测试源、日志、结果、源备份与清理 manifest：`/home/halo/.local/share/PrismLauncher/instances/CDPR/backups/trade-components-20260919-234820`。
- 最终本机 Core SHA-256 `9376431396094021a457d2a7f03cd251c8b8a6adf44b638b691f3e3fd2ebd8a2`，需要完整重启；未提交、未发布远程制品或改下载描述符。普通商店 JSON/语言资源属于机械数据迁移；本轮新增/修改 Java 128 行，可分别审阅钱包保护、品质生长与数据配置。
- 收尾 `devtool.sh refresh` / `check` 通过（bkmpw 0.2.2，315 份元数据、1657 个索引文件）；Xaero HUD 已进入分发索引，运行 JAR 仍忽略。相关 JSON 格式、Core 与本轮 pack 范围 diff 空白检查通过。


## 熔融固化、AE2 终端品质与主世界浅层洞穴优化（2026-09-21）

- 维护者明确选择三项一起推进；AE2 终端品质按边界落在 `quality_food_j` fork，不放回 Core。
- Core 恢复熔融金属遇水固化注册：源流体按映射变对应金属/材料块，流动流体变 Create Scorchia；目标方块通过注册表 ID 延迟解析，缺少可选模组时不注册对应产物。当前 `CMFluidsMixin` 已禁用 Create Metallurgy 默认炉渣/原石交互，本批补上 CDC 自己的 `FluidInteractionRegistry` 注册。
- 主世界优化改为密度函数层实现，不再采用旧 `buildSurface` 刷石头逻辑。新增 `surfaceCaveOptimizationDepth` 配置（默认 16，0 禁用）与 `NoiseRouterData.entrances` 包装，按地形密度和高度压制浅层洞穴入口；深层洞穴保留，主要减少露天洞口导致的可渲染区块面积。该项只影响新生成区块，实际地貌/帧率收益仍需同种子客户端对照验收。
- Quality Food fork 新增 AE2 19.2.17 终端兼容：`CraftingTermMenu` 预览走保留品质转换，`CraftingTermSlot.craftItem` 在服务端实际抽取材料后执行转换/品质计算。覆盖手动单次与 Shift/批量路径；不扩展 AE2 自动合成。
- 本批已验证：`quality_food_j` `compileJava` 通过；Core `compileJava` 通过；两仓 `git diff --check` 通过。尚未启动隔离服验证流体交互、终端取出和噪声入口实际注入，部署与行为验收仍待做。

### 复审修正（2026-09-21）

- `Registry.register(BuiltInRegistries.DENSITY_FUNCTION_TYPE)` 写在 mod 构造器里会在启动时抛 `Registry is already frozen`：原版注册表在 `Bootstrap.bootStrap()` 冻结，`GameData.unfreezeData()` 在 mod 构造之后才执行。已改为 `DeferredRegister` 于 `RegisterEvent` 期间注册 `shallow_cave_suppression` 编解码器。
- `NoiseRouterData.entrances` mixin 中对 `densityFunctions.getOrThrow(SLOPED_CHEESE).value()` 的直接求值在 bootstrap 阶段抛 `unbound value`：此时 `overworld/sloped_cheese` 尚未注册进 `RegistrySetBuilder`。已按原版惯例改为包装 `DensityFunctions.HolderHolder` 延迟解析。
- 同一 mixin 直接 `Config.get()` 会在 `runData` 路径崩溃（datagen 不加载 COMMON 配置，但会走世界生成注册表 bootstrap）；已加 `Config.SPEC.isLoaded()` 守卫，未加载时回落默认值。
- 可选方块缺失时（dev 运行时缺 `createaddition:electrum_block`、`createutilities:void_steel_block`）原本直接跳过注册，使对应熔融流体完全失去固化；现回落到 Create Metallurgy 原语义（源→炉渣块、流动→原石）。`CMFluids.ALL_MOLTEN_FLUIDS` 中无映射的流体（铝/铅/镍/锇/锂/因瓦/康铜/亡灵金属）同样回落到该旧语义，不再静默丢失交互。
- `AlexsCavesDimensionOverrides.biomeForDimension` 对 null 维度键 NPE（`ImmutableMap.get(null)`），主世界生物群系采样线程不安全路径下会炸区块生成；已加 null 守卫。
- 开发服 `runServer` 全新世界已实际跑通：mod 构造、`entrances` bootstrap 包装、流体交互注册（含两条回落日志）、出生区区块生成、`Done` 均通过，日志 `run/logs/latest.log`。`runData` 被 mbd2 自身 `Minecraft.getInstance()` NPE 阻断（与本批无关），datagen 完整验证留待该问题修复后复核；`isLoaded` 守卫属防御性保留。洞穴地貌/帧率实际收益仍待同种子客户端对照，AE2 终端取出仍待验收。
