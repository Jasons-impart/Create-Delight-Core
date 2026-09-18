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
