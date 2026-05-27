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

    public static final ModConfigSpec.DoubleValue LUNA_SOIL_BOOST_CHANCE = BUILDER
            .comment(
                    "月壤和湿润月壤耕地在随机刻促进上方可骨粉催熟方块的概率。",
                    "Chance for luna soil and moist luna soil farmland to boost bonemealable blocks on random ticks."
            )
            .defineInRange("lunaSoilBoostChance", 0.5D, 0.0D, 1.0D);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private Config() {
    }
}
