package io.github.jasonsimpart;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public final class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue ENABLE_BLAZE_CAKE_FOOD_PATCH = BUILDER
            .comment(
                    "是否启用从 KubeJS 迁移来的机械动力烈焰蛋糕食物属性与吃后点燃补丁。修改后需要重启游戏。",
                    "Enable the Create blaze cake food component and post-eating ignition patch migrated from KubeJS. Requires restart."
            )
            .define("enableBlazeCakeFoodPatch", true);

    public static final ModConfigSpec.BooleanValue ENABLE_AE2_BUDDING_QUARTZ_PROTECTION = BUILDER
            .comment(
                    "是否保护 AE2 赛特斯石英母岩：玩家未潜行时禁止破坏。",
                    "Protect AE2 budding certus quartz blocks by preventing break attempts unless the player is sneaking."
            )
            .define("enableAe2BuddingQuartzProtection", true);

    public static final ModConfigSpec.BooleanValue ENABLE_CREATE_SA_SPOUT_FILLING = BUILDER
            .comment(
                    "是否启用机械动力注液器在置物台/权重弹射器上给 Create Stuff Additions 装备充水/充燃料。修改后需要重启游戏。",
                    "Enable Create spouts filling Create Stuff Additions equipment on depots and weighted ejectors. Requires restart."
            )
            .define("enableCreateSaSpoutFilling", true);

    public static final ModConfigSpec.BooleanValue ENABLE_AE2_SPENT_LIQUOR_SPOUT = BUILDER
            .comment(
                    "是否启用陨石废液注液器交互：促进/修复 AE2 赛特斯石英母岩。修改后需要重启游戏。",
                    "Enable spent liquor spout interactions for growing and repairing AE2 budding certus quartz. Requires restart."
            )
            .define("enableAe2SpentLiquorSpout", true);

    public static final ModConfigSpec.BooleanValue ENABLE_ALEXSCAVES_SULFUR_SPOUT = BUILDER
            .comment(
                    "是否启用酸液注液器交互：让 Alex's Caves 硫磺芽生长。修改后需要重启游戏。",
                    "Enable acid spout interactions for growing Alex's Caves sulfur buds. Requires restart."
            )
            .define("enableAlexsCavesSulfurSpout", true);

    public static final ModConfigSpec.BooleanValue ENABLE_ALEXSCAVES_DIMENSION_BIOME_OVERRIDES = BUILDER
            .comment(
                    "是否让 createdelightcore 的 Alex's Caves 独立维度在 Alex's Caves 结构判定中视为对应洞穴生物群系。只影响白名单维度。",
                    "Treat createdelightcore's standalone Alex's Caves dimensions as their matching cave biomes for Alex's Caves structure checks only. Whitelisted dimensions only."
            )
            .define("enableAlexsCavesDimensionBiomeOverrides", true);

    public static final ModConfigSpec.BooleanValue ENABLE_ALEXSCAVES_DIMENSION_MONSTER_SPAWN_GUARD = BUILDER
            .comment(
                    "是否阻止 minecraft 和 quark 的怪物生成在 createdelightcore 的 Alex's Caves 洞穴维度中。只拦截新生成实体，不清理存档已有实体。",
                    "Prevent minecraft and quark monsters from spawning in createdelightcore's Alex's Caves dimensions. Only new entities are blocked; entities loaded from disk are preserved."
            )
            .define("enableAlexsCavesDimensionMonsterSpawnGuard", true);

    public static final ModConfigSpec.BooleanValue ENABLE_INVALID_RESOURCE_PATH_FILTERS = BUILDER
            .comment(
                    "是否过滤特定第三方 mod jar 中已知的坏资源/data map 文件。只过滤白名单中的路径，不放宽全局资源路径校验。",
                    "Filter known bad resource/data map files from specific third-party mod jars only. This does not loosen global resource path validation."
            )
            .define("enableInvalidResourcePathFilters", true);

    public static final ModConfigSpec.BooleanValue ENABLE_DATA_MAP_MISSING_KEY_FILTERS = BUILDER
            .comment(
                    "是否过滤已知会引用缺失注册对象的 NeoForge DataMap 键。只过滤白名单中的坏键，不屏蔽其它 DataMap 错误。",
                    "Filter known NeoForge DataMap keys that reference missing registry objects only. Other DataMap errors remain visible."
            )
            .define("enableDataMapMissingKeyFilters", true);

    public static final ModConfigSpec.BooleanValue ENABLE_PHANTOM_COMPOST_PARTICLES = BUILDER
            .comment(
                    "是否显示幻灵肥料方块的客户端菌丝粒子。只影响视觉效果。",
                    "Show client-side mycelium particles from phantom compost blocks. Visual only."
            )
            .define("enablePhantomCompostParticles", true);

    public static final ModConfigSpec.BooleanValue ENABLE_JELLY_BLOCK_PARTICLES = BUILDER
            .comment(
                    "是否显示果酱/果冻方块滑落和落地时的客户端方块粒子。只影响视觉效果。",
                    "Show client-side block particles when entities slide on or land on jelly/jello blocks. Visual only."
            )
            .define("enableJellyBlockParticles", true);

    public static final ModConfigSpec.BooleanValue ENABLE_QUARK_CONTRIBUTOR_REWARD_PATCH = BUILDER
            .comment(
                    "是否禁用 Quark 贡献者奖励加载和等级判定。Mixin 仅在 Quark 存在时应用；修改后需要重启游戏。",
                    "Disable Quark contributor reward loading and tier checks. The mixin only applies when Quark is present. Requires restart."
            )
            .define("enableQuarkContributorRewardPatch", true);

    public static final ModConfigSpec.BooleanValue ENABLE_XAERO_UPDATE_CHECK_BLOCK = BUILDER
            .comment(
                    "是否禁用 Xaero Minimap / World Map / XaeroLib 的启动联网检查，包括更新检查、Patreon 数据和在线组件加载。Mixin 仅在对应 Xaero mod 存在时应用；修改后需要重启游戏。",
                    "Disable Xaero Minimap / World Map / XaeroLib startup internet checks, including update checks, Patreon data and online widget loading. The mixins only apply when matching Xaero mods are present. Requires restart."
            )
            .define("enableXaeroUpdateCheckBlock", true);

    public static final ModConfigSpec.BooleanValue ENABLE_ECLIPTIC_SEASONS_CHUNK_ATTACHMENT_SYNC_PATCH = BUILDER
            .comment(
                    "是否修正 Ecliptic Seasons 雪状态区块附件同步时序：增量同步只发送给已确认收到该区块的客户端。Mixin 仅在 Ecliptic Seasons 存在时应用；修改后需要重启游戏。",
                    "Fix Ecliptic Seasons snowy chunk attachment sync timing by sending incremental updates only to clients that have acknowledged the chunk. The mixins only apply when Ecliptic Seasons is present. Requires restart."
            )
            .define("enableEclipticSeasonsChunkAttachmentSyncPatch", true);

    public static final ModConfigSpec.BooleanValue DISABLE_DROP_REPORT = BUILDER
            .comment(
                    "是否禁用掉落物报告。报告会低频统计已加载维度中的掉落物，并在区块内数量超过阈值时通知在线玩家。",
                    "Disable the dropped item report. The report periodically scans loaded levels and notifies online players when a chunk exceeds the threshold."
            )
            .define("disableDropReport", false);

    public static final ModConfigSpec.IntValue DROP_REPORT_ITEM_THRESHOLD = BUILDER
            .comment(
                    "掉落物报告的区块阈值。统计值超过此数量时报告该区块。",
                    "Chunk threshold for the dropped item report. Chunks above this count are reported."
            )
            .defineInRange("itemThreshold", 100, 0, Integer.MAX_VALUE);

    public static final ModConfigSpec.BooleanValue DROP_REPORT_IGNORE_STACK_COUNT = BUILDER
            .comment(
                    "掉落物报告是否按掉落实体数量统计，而不是按物品堆叠数量统计。",
                    "Count dropped item entities instead of item stack sizes in the dropped item report."
            )
            .define("ignoreStackCount", false);

    public static final ModConfigSpec.IntValue DROP_REPORT_INTERVAL_TICKS = BUILDER
            .comment(
                    "掉落物报告扫描间隔，单位为 tick。旧 Core 固定为 6000 tick，即 5 分钟。",
                    "Dropped item report scan interval in ticks. Old Core used 6000 ticks, or 5 minutes."
            )
            .defineInRange("dropReportIntervalTicks", 6000, 6000, Integer.MAX_VALUE);

    public static final ModConfigSpec.IntValue DROP_REPORT_MAX_CHUNKS = BUILDER
            .comment(
                    "每次掉落物报告最多显示多少个超阈值区块，避免刷屏。",
                    "Maximum over-threshold chunks shown per dropped item report to avoid chat spam."
            )
            .defineInRange("dropReportMaxChunks", 10, 1, 100);

    public static final ModConfigSpec.BooleanValue ENABLE_WAYSTONES_MONEY_TELEPORT = BUILDER
            .comment(
                    "是否启用旧 Core 的 Waystones 传送货币消耗：将 Waystones 经验等级/经验点需求换算为 Lightman's Currency 基础币值。",
                    "Enable old Core Waystones money teleport costs by replacing Waystones XP requirements with Lightman's Currency base coin value costs."
            )
            .define("enableWaystonesMoneyTeleport", true);

    public static final ModConfigSpec.ConfigValue<String> WAYSTONES_MONEY_CHAIN = BUILDER
            .comment(
                    "Waystones 传送扣款使用的 Lightman's Currency coin chain。",
                    "Lightman's Currency coin chain used by Waystones teleport money costs."
            )
            .define("waystonesMoneyChain", "main");

    public static final ModConfigSpec.IntValue WAYSTONES_TELEPORT_COST_PER_LEVEL = BUILDER
            .comment(
                    "每 1 点 Waystones 经验成本换算成多少 Lightman's Currency 基础币值。旧 Core 默认值为 45。",
                    "Base coin value charged for each Waystones XP cost unit. Old Core default was 45."
            )
            .defineInRange("waystonesTeleportCostPerLevel", 45, 1, Integer.MAX_VALUE);

    public static final ModConfigSpec.ConfigValue<List<? extends String>> BELT_GRINDER_BLOCKED_SANDPAPER_RECIPES = BUILDER
            .comment(
                    "Sandpaper polishing recipe IDs to hide from Create: Metallurgy Belt Grinder auto-inheritance and JEI display.",
                    "Example: [\"createdelightcore:sandpaper_polishing/rose_quartz\"]"
            )
            .defineListAllowEmpty(
                    "beltGrinderBlockedSandpaperRecipes",
                    List.of("createdelightcore:sandpaper_polishing/rose_quartz"),
                    value -> value instanceof String
            );

    public static final ModConfigSpec.EnumValue<RecipeRemoveMissingIdMode> RECIPE_REMOVE_MISSING_ID_MODE = BUILDER
            .comment(
                    "KubeJS ServerEvents.recipes event.remove({id: ...}) missing recipe handling. OFF keeps KubeJS behavior, WARN logs only, STRICT fails the reload.",
                    "Only exact ID filters are checked. Broad filters such as mod/input/output/type/regex/predicate are not affected."
            )
            .defineEnum("recipeRemoveMissingIdMode", RecipeRemoveMissingIdMode.STRICT);

    public static final ModConfigSpec.BooleanValue ENABLE_KUBEJS_CREATE_SEQUENCED_FLUID_FIX = BUILDER
            .comment(
                    "是否修正 KubeJS Create 在 create:sequenced_assembly.sequence 内生成的 create:filling 流体 ingredient 格式。只改最终 recipe JSON，不改脚本 API。",
                    "Fix KubeJS Create fluid ingredient JSON generated for create:filling steps inside create:sequenced_assembly.sequence. Only final recipe JSON is normalized; script APIs are unchanged."
            )
            .define("enableKubeJsCreateSequencedFluidFix", true);

    public static final ModConfigSpec.BooleanValue DEBUG_KUBEJS_CREATE_SEQUENCED_FLUID_FIX = BUILDER
            .comment(
                    "是否记录 KubeJS Create 序列装配流体 recipe JSON 修正命中的 recipe。",
                    "Log recipes touched by the KubeJS Create sequenced assembly fluid JSON fix."
            )
            .define("debugKubeJsCreateSequencedFluidFix", false);

    public static final ModConfigSpec.DoubleValue LUNA_SOIL_BOOST_CHANCE = BUILDER
            .comment(
                    "月壤和湿润月壤耕地在随机刻促进上方可骨粉催熟方块的概率。",
                    "Chance for luna soil and moist luna soil farmland to boost bonemealable blocks on random ticks."
            )
            .defineInRange("lunaSoilBoostChance", 0.5D, 0.0D, 1.0D);

    public static final ModConfigSpec SPEC = BUILDER.build();

    public enum RecipeRemoveMissingIdMode {
        OFF,
        WARN,
        STRICT
    }

    private Config() {
    }
}
