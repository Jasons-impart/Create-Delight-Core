package io.github.jasonsimpart.registry;

import io.github.jasonsimpart.CreateDelightCore;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CreateDelightCore.MODID);

    // 食物页：可食用物品、糖浆/果酱/果冻相关方块。
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> FOOD = CREATIVE_TABS.register("food", () -> CreativeModeTab.builder().title(Component.translatable("itemGroup.createdelightcore.food")).withTabsBefore(CreativeModeTabs.FOOD_AND_DRINKS).icon(() -> ModItems.STRAWBERRY_ICE_CREAM_SCOOP.get().getDefaultInstance()).displayItems((parameters, output) -> {
        output.accept(ModItems.UNFRIED_SHRIMP);
        output.accept(ModItems.UNFRIED_CHICKEN_CHIP);
        output.accept(ModItems.UNFRIED_CHICKEN_LEG);
        output.accept(ModItems.UNFRIED_TONKATSU);
        output.accept(ModItems.UNFRIED_FISH);
        output.accept(ModItems.UNFRIED_POTATO);
        output.accept(ModItems.UNFRIED_CALAMARI);
        output.accept(ModItems.STRAWBERRY_ICE_CREAM_SCOOP);
        output.accept(ModItems.BANANA_ICE_CREAM_SCOOP);
        output.accept(ModItems.MINT_ICE_CREAM_SCOOP);
        output.accept(ModItems.ADZUKI_ICE_CREAM_SCOOP);
        output.accept(ModItems.POMEGRANATE_ICE_CREAM_SCOOP);
        output.accept(ModItems.LIME_ICE_CREAM_SCOOP);
        output.accept(ModItems.APPLE_ICE_CREAM_SCOOP);
        output.accept(ModItems.BEETROOT_ICE_CREAM_SCOOP);
        output.accept(ModItems.CARROT_ICE_CREAM_SCOOP);
        output.accept(ModItems.ENCHANTED_FRUIT_ICE_CREAM_SCOOP);
        output.accept(ModItems.GLOW_BERRY_ICE_CREAM_SCOOP);
        output.accept(ModItems.PUMPKIN_ICE_CREAM_SCOOP);
        output.accept(ModBlocks.BASE_SYRUP.get());
        output.accept(ModBlocks.STRAWBERRY_SYRUP.get());
        output.accept(ModBlocks.VANILLA_SYRUP.get());
        output.accept(ModBlocks.MINT_SYRUP.get());
        output.accept(ModBlocks.BANANA_SYRUP.get());
        output.accept(ModBlocks.LUSH_CONFITURE_JELLY.get());
        output.accept(ModBlocks.LUSH_CONFITURE_JELLO.get());
        output.accept(ModBlocks.LUSH_CONFITURE_JELLY_BOTTLE.get());
        output.accept(ModItems.EMPTY_RICEBALL);
        output.accept(ModItems.FUGU_ROLL);
        output.accept(ModItems.RADGILL_SUSHI);
        output.accept(ModItems.DEEP_SEA_SUSHI_ROLL_SLICE);
        output.accept(ModItems.BUTTER);
        output.accept(ModItems.OIL_DOUGH);
        output.accept(ModItems.PUFF_PASTRY);
        output.accept(ModItems.RAW_CALAMARI);
        output.accept(ModItems.RAW_GHAST_CALAMARI);
        output.accept(ModItems.RAW_EMPANADA);
        output.accept(ModItems.RAW_CHEESE_PIZZA);
        output.accept(ModItems.OAT_BREAD);
        output.accept(ModItems.SALAMI);
        output.accept(ModItems.RAW_POTATO_PANCAKE);
        output.accept(ModItems.YORKSHIRE_PUDDING_AND_BEEF);
        output.accept(ModItems.EMPTY_POPSICLE);
        output.accept(ModItems.BRAISED_INTESTINES_IN_BROWN_SAUCE);
        output.accept(ModItems.BOILING_WATER_CABBAGE);
        output.accept(ModItems.MAYO_CORN_DOG);
        output.accept(ModItems.KETCHUP_CORN_DOG);
        output.accept(ModItems.WRAPPED_FRIES_GHASTA);
        output.accept(ModItems.ENCHANTED_GOLDEN_LANTERN_FRUIT);
        output.accept(ModItems.ENCHANTED_GOLDEN_CARROT);
        output.accept(ModItems.FUEL_HOTCREAM);
        output.accept(ModItems.LUSH_CONFITURE_JELLO_ITEM);
        output.accept(ModItems.ENCHANTED_GOLDEN_ARBUTUS_BERRIES);
        ModItems.COOKIE_DOUGH_ITEMS.forEach(output::accept);
    }).build());

    // 材料页：矿物、普通材料、工具、装备和非半成品杂项。
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MATERIALS = CREATIVE_TABS.register("materials", () -> CreativeModeTab.builder().title(Component.translatable("itemGroup.createdelightcore.materials")).withTabsBefore(FOOD.getKey()).icon(() -> ModItems.TIN_INGOT.get().getDefaultInstance()).displayItems((parameters, output) -> {
        output.accept(ModItems.BLACK_CHOCOLATE_MOLD_SOLID);
        output.accept(ModItems.BLACK_CHOCOLATE_MOLD_FILLED);
        output.accept(ModItems.WHITE_CHOCOLATE_MOLD_SOLID);
        output.accept(ModItems.WHITE_CHOCOLATE_MOLD_FILLED);
        output.accept(ModItems.RUBY_CHOCOLATE_MOLD_SOLID);
        output.accept(ModItems.RUBY_CHOCOLATE_MOLD_FILLED);
        output.accept(ModBlocks.TIN_ORE.get());
        output.accept(ModBlocks.DEEPSLATE_TIN_ORE.get());
        output.accept(ModBlocks.RAW_TIN_BLOCK.get());
        output.accept(ModBlocks.TIN_BLOCK.get());
        output.accept(ModBlocks.BRONZE_BLOCK.get());
        output.accept(ModBlocks.FORGED_STEEL_BLOCK.get());
        output.accept(ModBlocks.FRAGMENT_OF_BORDER.get());
        output.accept(ModBlocks.ENRICHED_SKY_STONE_BLOCK.get());
        output.accept(ModBlocks.IRON_CASING.get());
        output.accept(ModBlocks.SPACE_CASING.get());
        output.accept(ModBlocks.SKY_STEEL_CASING.get());
        output.accept(ModBlocks.STEEL_CASING.get());
        output.accept(ModBlocks.FORGE_STEEL_CASING.get());
        output.accept(ModBlocks.STEEL_GLASS_CASING.get());
        output.accept(ModBlocks.STEEL_CLEAR_GLASS_CASING.get());
        output.accept(ModBlocks.COW_ZIP.get());
        output.accept(ModBlocks.SHEEP_ZIP.get());
        output.accept(ModBlocks.PIG_ZIP.get());
        output.accept(ModBlocks.CHICKEN_ZIP.get());
        output.accept(ModBlocks.GOAT_ZIP.get());
        output.accept(ModBlocks.BLACK_RABBIT_ZIP.get());
        output.accept(ModBlocks.BROWN_RABBIT_ZIP.get());
        output.accept(ModBlocks.SPLOTCHED_RABBIT_ZIP.get());
        output.accept(ModBlocks.GOLD_RABBIT_ZIP.get());
        output.accept(ModBlocks.WHITE_RABBIT_ZIP.get());
        output.accept(ModBlocks.SALT_RABBIT_ZIP.get());
        output.accept(ModItems.TIN_INGOT);
        output.accept(ModItems.TIN_NUGGET);
        output.accept(ModItems.RAW_TIN);
        output.accept(ModItems.BRONZE_INGOT);
        output.accept(ModItems.BRONZE_NUGGET);
        output.accept(ModItems.CARBON_DUST);
        output.accept(ModItems.TIN_DUST);
        output.accept(ModItems.DIRTY_TIN_DUST);
        output.accept(ModItems.SILVER_DUST);
        output.accept(ModItems.DIRTY_SILVER_DUST);
        output.accept(ModItems.TITANIUM_DUST);
        output.accept(ModItems.DIRTY_TITANIUM_DUST);
        output.accept(ModItems.URANIUM_DUST);
        output.accept(ModItems.ENRICHED_URANIUMDUST);
        output.accept(ModItems.DEPLETED_URANIUM_DUST);
        output.accept(ModItems.CRUSHED_RAW_TITANIUM);
        output.accept(ModItems.CARBON_PLATE);
        output.accept(ModItems.ANDESITE_ALLOY_NUGGET);
        output.accept(ModItems.VERMICELLI);
        output.accept(ModItems.BOARD_NOODLES);
        output.accept(ModItems.CORN_FLOUR);
        output.accept(ModItems.WAFER_DOUGH);
        output.accept(ModItems.GUNCOTTON);
        output.accept(ModItems.OTHERWORLD_NOTE);
        output.accept(ModItems.UNIVERSAL_PRESS);
        output.accept(ModItems.ULTIMATE_UNIVERSAL_PRESS);
        output.accept(ModItems.REDSTONE_PASTE);
        output.accept(ModItems.GLOWSTONE_PASTE);
        output.accept(ModItems.SKY_STONE_PASTE);
        output.accept(ModItems.QUARTZ_GLASS_PARTS);
        output.accept(ModItems.QUARTZ_VIBRANT_GLASS_PARTS);
        output.accept(ModItems.SKY_COPPER_INGOT);
        output.accept(ModItems.CELL_HOUSING_CURVING_HEAD);
        output.accept(ModItems.PLANET_GEAR);
        output.accept(ModItems.MAGNETIC_MECHANISM);
        output.accept(ModItems.PROSPECTOR);
        output.accept(ModItems.PROSPECTOR_CORE);
        output.accept(ModItems.PHASE_TRANSITION_IRON);
        output.accept(ModItems.MMD_DIAMOND);
        output.accept(ModItems.INCOMPLETE_PAPER);
        output.accept(ModItems.WASTE_PAPER);
        output.accept(ModItems.UNFINISHED_LEATHER);
        output.accept(ModItems.DRY_YEAST);
        output.accept(ModItems.STEEL_SHEET);
        output.accept(ModItems.FORGED_STEEL_SHEET);
        output.accept(ModItems.BLOOD_COLLECTION_DEVICE);
        output.accept(ModItems.NEEDLE);
        output.accept(ModItems.ORDER);
        output.accept(ModItems.UNOPENED_ORDER);
        output.accept(ModItems.ORDER_DELIVERER_ITEM);
        output.accept(ModItems.QUALITY_ABSORBER);
        output.accept(ModItems.UNACTIVATED_CRYSTALLINE_FLOWER);
        output.accept(ModItems.DEBUG_RELOAD_TOOL);
        output.accept(ModItems.DEBUG_INFO_TOOL);
        output.accept(ModItems.AIR_HELMET);
        output.accept(ModItems.AIR_CHESTPLATE);
        output.accept(ModItems.AIR_LEGGINGS);
        output.accept(ModItems.AIR_BOOTS);
        output.accept(ModItems.OXYGEN_TANK);
        output.accept(ModItems.STURDY_OXYGEN_TANK);
        output.accept(ModItems.DREAD_HEART);
        output.accept(ModItems.DREAD_UPGRADE_SMITHING_TEMPLATE);
        output.accept(ModItems.DEVIL_EYE);
        output.accept(ModItems.DEMONIC_CODEX);
        output.accept(ModItems.BLEAK_ELECTRON_TUBE);
        output.accept(ModItems.OVERWORLD_METAL_ORE_CLUSTER);
        output.accept(ModItems.OVERWORLD_NOBLE_METAL_ORE_CLUSTER);
        output.accept(ModItems.NETHER_ORE_CLUSTER);
        output.accept(ModItems.MOON_ORE_CLUSTER);
        output.accept(ModItems.MARS_ORE_CLUSTER);
        output.accept(ModItems.MERCURY_ORE_CLUSTER);
        output.accept(ModItems.VENUS_ORE_CLUSTER);
        output.accept(ModItems.GLACIO_ORE_CLUSTER);
        output.accept(ModItems.MARS_GEMSTONE_CLUSTER);
        output.accept(ModItems.INFERIOR_GENETIC_SEED);
        output.accept(ModItems.NORMAL_GENETIC_SEED);
        output.accept(ModItems.REFINED_GENETIC_SEED);
        output.accept(ModItems.PURE_GENETIC_SEED);
        output.accept(ModItems.FLAWLESS_GENETIC_SEED);
    }).build());

    // 流体页：只展示有桶的流体，recipe-only 流体不会出现在创造栏。
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> FLUIDS = CREATIVE_TABS.register("fluids", () -> CreativeModeTab.builder().title(Component.translatable("itemGroup.createdelightcore.fluids")).withTabsBefore(MATERIALS.getKey()).icon(() -> ModFluids.FUEL_MIXTURES.bucket().get().getDefaultInstance()).displayItems((parameters, output) -> {
        ModFluids.SIMPLE_FLUIDS.stream()
                .filter(ModFluids.SimpleFluid::hasBucket)
                .forEach(fluid -> output.accept(fluid.bucket()));
    }).build());

    // 半成品页：Create 序列装配中间件、AE 中间件和食品模具等。
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> INTERMEDIATES = CREATIVE_TABS.register("intermediates", () -> CreativeModeTab.builder().title(Component.translatable("itemGroup.createdelightcore.intermediates")).withTabsBefore(FLUIDS.getKey()).icon(() -> ModItems.CREATE_MACHINE_TRANSITIONAL_ITEMS.getFirst().get().getDefaultInstance()).displayItems((parameters, output) -> {
        ModItems.UNBAKED_MUFFIN_ITEMS.forEach(output::accept);
        ModItems.POPSICLE_MOLD_FILLED_ITEMS.forEach(output::accept);
        ModItems.POPSICLE_MOLD_SOLID_ITEMS.forEach(output::accept);
        output.accept(ModItems.POTATO_STEW_BEEF);
        output.accept(ModItems.OIL_DOUGH_WITH_BUTTER);
        ModItems.AE_INTERMEDIATE_ITEMS.forEach(output::accept);
        ModItems.CREATE_MACHINE_TRANSITIONAL_ITEMS.forEach(output::accept);
        ModItems.AMMO_TRANSITIONAL_ITEMS.forEach(output::accept);
        ModItems.BURGER_SANDWICH_TRANSITIONAL_ITEMS.forEach(output::accept);
        ModItems.SUSHI_TRANSITIONAL_ITEMS.forEach(output::accept);
        ModItems.GENERATED_UNFINISHED_ITEMS.forEach(output::accept);
    }).build());

    // 钱币页。
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> COINS = CREATIVE_TABS.register("coins", () -> CreativeModeTab.builder().title(Component.translatable("itemGroup.createdelightcore.coins")).withTabsBefore(INTERMEDIATES.getKey()).icon(() -> ModItems.GOLD_COIN.get().getDefaultInstance()).displayItems((parameters, output) -> {
        output.accept(ModItems.IRON_COIN);
        output.accept(ModItems.COPPER_COIN);
        output.accept(ModItems.GOLD_COIN);
        output.accept(ModItems.EMERALD_COIN);
        output.accept(ModItems.NETHERITE_COIN);
    }).build());

    private ModCreativeTabs() {
    }

    public static void register(IEventBus modEventBus) {
        CREATIVE_TABS.register(modEventBus);
    }
}
