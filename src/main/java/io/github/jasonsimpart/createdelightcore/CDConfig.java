package io.github.jasonsimpart.createdelightcore;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

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
    static final ForgeConfigSpec SPEC = BUILDER.build();

    private static final ForgeConfigSpec.Builder SERVER_BUILDER = new ForgeConfigSpec.Builder();
    private static final ForgeConfigSpec.IntValue SURFACE_DEPTH_LIMIT = SERVER_BUILDER
            .comment("The depth limit from surface for carving and noise cave start generating.")
            .comment("Carving and noise cave generation will be restricted to this many blocks below the surface.")
            .comment("Set to 0 to disable this feature.")
            .comment("default: 16")
            .defineInRange("surfaceDepthLimit", 16, 0, 256);
    static final ForgeConfigSpec SERVER_SPEC = SERVER_BUILDER.build();


    public static boolean disableDropReport;
    public static int itemThreshold;
    public static boolean ignoreStackCount;
    public static String moneyChain;
    public static int teleportCost;
    public static boolean useMoneyTeleport;
    public static double lunaSoilBoostChance;
    public static int surfaceDepthLimit;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event)
    {
        disableDropReport = DISABLE_DROP_REPORT.get();
        itemThreshold = ITEM_THRESHOLD.get();
        ignoreStackCount = IGNORE_STACK_COUNT.get();
        moneyChain = MONEY_CAIN.get();
        useMoneyTeleport = USE_MONEY_TELEPORT.get();
        teleportCost = TELEPORT_COST.get();
        lunaSoilBoostChance = LUNA_SOIL_BOOST_CHANCE.get();
        surfaceDepthLimit = SURFACE_DEPTH_LIMIT.get();
    }
}
