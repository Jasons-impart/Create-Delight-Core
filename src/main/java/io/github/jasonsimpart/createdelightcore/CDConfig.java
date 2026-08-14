package io.github.jasonsimpart.createdelightcore;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = CreateDelightCore.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CDConfig
{
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    private static final ForgeConfigSpec.BooleanValue DISABLE_DROP_REPORT = BUILDER
            .comment("Whether to enable the drop report")
            .define("disableDropReport", false);

    private static final ForgeConfigSpec.IntValue ITEM_THRESHOLD = BUILDER
            .comment("Threshold for drop reporting.")
            .defineInRange("itemThreshold", 100, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.BooleanValue IGNORE_STACK_COUNT = BUILDER
            .comment("If this option is enabled, the drop report will be counted by a single drop entity, not by the number of items in it")
            .define("ignoreStackCount", false);
    private static final ForgeConfigSpec.ConfigValue<String> MONEY_CAIN = BUILDER
            .comment("Money Chain used for money calculating")
            .define("moneyChain", "main");
    private static final ForgeConfigSpec.BooleanValue USE_MONEY_TELEPORT = BUILDER
            .comment("Whether to use money for teleporting")
            .define("useMoneyTeleport", true);
    private static final ForgeConfigSpec.IntValue TELEPORT_COST = BUILDER
            .comment("Coin used for waystone teleporting per unit.")
            .comment("It's the base value of the chain.")
            .defineInRange("teleportCost", 45, 1, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.DoubleValue LUNA_SOIL_BOOST_CHANCE = BUILDER
            .comment("The chance for luna soil to boost crops grows.")
            .defineInRange("lunaSoilBoostChance", 0.5, 0, 1);
    private static final ForgeConfigSpec.BooleanValue LOG_MORE_MOD_TETRA_INDEPENDENT_DAMAGE_MULTIPLIERS = BUILDER
            .comment("Whether to log More Mod Tetra independent damage multipliers.")
            .comment("Useful for debugging MMT damage stacking; logs only when MMT reports at least one independent multiplier.")
            .define("logMoreModTetraIndependentDamageMultipliers", false);
    private static final ForgeConfigSpec.BooleanValue ENABLE_ADDITIVE_MULTICRIT = BUILDER
            .comment("Backport the newer Apothic Attributes additive multicrit formula when Apothic Attributes is installed.")
            .define("enableAdditiveMulticrit", true);
    private static final ForgeConfigSpec.BooleanValue ECHOING_STRIKES_USE_ORIGINAL_DAMAGE = BUILDER
            .comment("Make Iron's Spells Echoing Strikes record the LivingHurtEvent amount captured at event construction.")
            .comment("This prevents event-based damage multipliers from being sampled once by the hit and again by the echo.")
            .define("echoingStrikesUseOriginalDamage", true);
    static final ForgeConfigSpec SPEC = BUILDER.build();

    private static final ForgeConfigSpec.Builder SERVER_BUILDER = new ForgeConfigSpec.Builder();
    private static final ForgeConfigSpec.IntValue SURFACE_DEPTH_LIMIT = SERVER_BUILDER
            .comment("The depth limit from surface for carving and noise cave start generating.")
            .comment("Carving and noise cave generation will be restricted to this many blocks below the surface.")
            .comment("Set to 0 to disable this feature.")
            .comment("default: 16")
            .defineInRange("surfaceDepthLimit", 16, 0, 256);
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> BELT_GRINDER_BLOCKED_SANDPAPER_RECIPES = SERVER_BUILDER
            .comment("Sandpaper polishing recipe IDs to block from the Belt Grinder.")
            .comment("Example: [\"createdelight:sandpaper_polishing/rose_quartz\"]")
            .defineList("beltGrinderBlockedSandpaperRecipes",
                    List.of("createdelight:sandpaper_polishing/rose_quartz"),
                    obj -> obj instanceof String);
    private static final ForgeConfigSpec.BooleanValue ENABLE_OPEN_ENDED_PIPE_LAVA_DRAIN_FIX = SERVER_BUILDER
            .comment("Whether Create open-ended pipes should safely drain lavalogged source blocks, such as Quark grates with lava.")
            .comment("Disable this only if a pack intentionally wants Create's original lava-draining behavior.")
            .define("enableOpenEndedPipeLavaDrainFix", true);
    private static final ForgeConfigSpec.BooleanValue ENABLE_SPONSOR_TITLES = SERVER_BUILDER
            .comment("Whether to automatically grant sponsor titles through FTB Ranks from the built-in or remote sponsor list.")
            .define("enableSponsorTitles", true);
    static final ForgeConfigSpec SERVER_SPEC = SERVER_BUILDER.build();


    public static boolean disableDropReport;
    public static int itemThreshold;
    public static boolean ignoreStackCount;
    public static String moneyChain;
    public static int teleportCost;
    public static boolean useMoneyTeleport;
    public static double lunaSoilBoostChance;
    public static boolean logMoreModTetraIndependentDamageMultipliers;
    public static boolean enableAdditiveMulticrit = true;
    public static boolean echoingStrikesUseOriginalDamage = true;
    public static int surfaceDepthLimit;
    public static List<String> beltGrinderBlockedSandpaperRecipes = new ArrayList<>();
    public static boolean enableOpenEndedPipeLavaDrainFix = true;
    public static boolean enableSponsorTitles = true;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event)
    {
        if (event.getConfig().getSpec() == SPEC) {
            disableDropReport = DISABLE_DROP_REPORT.get();
            itemThreshold = ITEM_THRESHOLD.get();
            ignoreStackCount = IGNORE_STACK_COUNT.get();
            moneyChain = MONEY_CAIN.get();
            useMoneyTeleport = USE_MONEY_TELEPORT.get();
            teleportCost = TELEPORT_COST.get();
            lunaSoilBoostChance = LUNA_SOIL_BOOST_CHANCE.get();
            logMoreModTetraIndependentDamageMultipliers = LOG_MORE_MOD_TETRA_INDEPENDENT_DAMAGE_MULTIPLIERS.get();
            enableAdditiveMulticrit = ENABLE_ADDITIVE_MULTICRIT.get();
            echoingStrikesUseOriginalDamage = ECHOING_STRIKES_USE_ORIGINAL_DAMAGE.get();
        }
        if (event.getConfig().getSpec() == SERVER_SPEC) {
            surfaceDepthLimit = SURFACE_DEPTH_LIMIT.get();
            beltGrinderBlockedSandpaperRecipes = new ArrayList<>(BELT_GRINDER_BLOCKED_SANDPAPER_RECIPES.get());
            enableOpenEndedPipeLavaDrainFix = ENABLE_OPEN_ENDED_PIPE_LAVA_DRAIN_FIX.get();
            enableSponsorTitles = ENABLE_SPONSOR_TITLES.get();
        }
    }
}
