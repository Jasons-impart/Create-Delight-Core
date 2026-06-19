package io.github.jasonsimpart.registry;

import io.github.jasonsimpart.CreateDelightCore;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.List;

public final class ModFluids {
    private static final int DEFAULT_THIN_FLUID_ALPHA = 0xFF;

    // 客户端渲染贴图。简单流体多数用原版水贴图染色，粘稠流体用 CDC 自己的中性厚流体贴图。
    public static final ResourceLocation WATER_STILL = ResourceLocation.withDefaultNamespace("block/water_still");
    public static final ResourceLocation WATER_FLOWING = ResourceLocation.withDefaultNamespace("block/water_flow");
    public static final ResourceLocation WATER_OVERLAY = ResourceLocation.withDefaultNamespace("block/water_overlay");
    public static final ResourceLocation UNDERWATER_OVERLAY = ResourceLocation.withDefaultNamespace("textures/misc/underwater.png");
    public static final ResourceLocation LAVA_STILL = ResourceLocation.withDefaultNamespace("block/lava_still");
    public static final ResourceLocation LAVA_FLOWING = ResourceLocation.withDefaultNamespace("block/lava_flow");
    public static final ResourceLocation THICK_STILL = ResourceLocation.fromNamespaceAndPath(CreateDelightCore.MODID, "block/fluid/thick_still");
    public static final ResourceLocation THICK_FLOWING = ResourceLocation.fromNamespaceAndPath(CreateDelightCore.MODID, "block/fluid/thick_flow");

    public static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, CreateDelightCore.MODID);
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(Registries.FLUID, CreateDelightCore.MODID);

    // 工业/原料流体：有方块、有桶。
    public static final SimpleFluid FUEL_MIXTURES = simpleWaterLikeFluid("fuel_mixtures", 0x8470FF);
    public static final SimpleFluid LIGHT_CRUDE_OIL = simpleWaterLikeFluid("light_crude_oil", 0x8B7E66);
    public static final SimpleFluid ETHYLENE_FLUID = simpleWaterLikeFluid("ethylene_fluid", 0xE6E6FA);
    public static final SimpleFluid SPENT_LIQUOR = simpleWaterLikeFluid("spent_liquor", 0x99FFCD);
    public static final SimpleFluid PAPER_PULP = simpleWaterLikeFluid("paper_pulp", 0xF0FFFF);
    public static final SimpleFluid UNREFINED_SUGAR = simpleWaterLikeFluid("unrefined_sugar", 0xBCB998);
    public static final SimpleFluid CRYO_FUEL = simpleWaterLikeFluid("cryo_fuel", 0x87CEFA);
    public static final SimpleFluid NUT_MILK = simpleWaterLikeFluid("nut_milk", 0xF5E7C2);
    public static final SimpleFluid VINEGAR = simpleWaterLikeFluid("vinegar", 0x570000);
    public static final SimpleFluid RADON = simpleWaterLikeFluid("radon", 0xA0FFDA);
    public static final SimpleFluid SOYA_MILK = simpleWaterLikeFluid("soya_milk", 0xFEF9C1);

    // 茶饮/咖啡类流体：团队决定保留方块和桶。
    public static final SimpleFluid ANCIENT_COFFEE = simpleWaterLikeFluid("ancient_coffee", 0x58321E);
    public static final SimpleFluid TORCHFLOWER_TEA = simpleWaterLikeFluid("torchflower_tea", 0xF3B727);
    public static final SimpleFluid CHERRY_PETAL_TEA = simpleWaterLikeFluid("cherry_petal_tea", 0xF28BC8);
    public static final SimpleFluid PITCHER_PLANT_TEA = simpleWaterLikeFluid("pitcher_plant_tea", 0x8AF2C9);
    public static final SimpleFluid FIDDLEHEAD_TEA = simpleWaterLikeFluid("fiddlehead_tea", 0x44451B);
    public static final SimpleFluid SCARLET_TEA = simpleWaterLikeFluid("scarlet_tea", 0x82332A);
    public static final SimpleFluid LEMON_BLACK_TEA = simpleWaterLikeFluid("lemon_black_tea", 0x64352A);
    public static final SimpleFluid TEA_MOCHA = simpleWaterLikeFluid("tea_mocha", 0x815026);
    public static final SimpleFluid SAIDI_TEA = simpleWaterLikeFluid("saidi_tea", 0xB02F2F);
    public static final SimpleFluid CORNFLOWER_TEA = simpleWaterLikeFluid("cornflower_tea", 0xA97549);
    public static final SimpleFluid SAKURA_HONEY_TEA = simpleWaterLikeFluid("sakura_honey_tea", 0xE497A8);
    public static final SimpleFluid GENMAI_TEA = simpleWaterLikeFluid("genmai_tea", 0xB8A32F);
    public static final SimpleFluid GREEN_WATER = simpleWaterLikeFluid("green_water", 0x79AD5C);
    public static final SimpleFluid WHITE_TEA = simpleWaterLikeFluid("white_tea", 0xDBCFAA);
    public static final SimpleFluid SPRING_SODA = simpleWaterLikeFluid("spring_soda", 0x7FE3B3);
    public static final SimpleFluid SUMMER_CORDIAL = simpleWaterLikeFluid("summer_cordial", 0xF4B12E);
    public static final SimpleFluid AUTUMN_TEA = simpleWaterLikeFluid("autumn_tea", 0xF46D44);
    public static final SimpleFluid WINTER_GLOGG = simpleWaterLikeFluid("winter_glogg", 0xA9275E);

    // Vinery 风格饮品流体。
    public static final SimpleFluid APPLE_JUICE = simpleWaterLikeFluid("apple_juice", 0xEED4A7);
    public static final SimpleFluid MEAD = simpleWaterLikeFluid("mead", 0xEAC88A);
    public static final SimpleFluid APPLE_CIDER = simpleWaterLikeFluid("apple_cider", 0x9C6140);
    public static final SimpleFluid APPLE_WINE = simpleWaterLikeFluid("apple_wine", 0xD6D375);
    public static final SimpleFluid MELLOHI_WINE = simpleWaterLikeFluid("mellohi_wine", 0xC3E1B7);
    public static final SimpleFluid GLOWING_WINE = simpleWaterLikeFluid("glowing_wine", 0xFCD263);
    public static final SimpleFluid SOLARIS_WINE = simpleWaterLikeFluid("solaris_wine", 0xB68346);
    public static final SimpleFluid NOIR_WINE = simpleWaterLikeFluid("noir_wine", 0x443672);
    public static final SimpleFluid RED_WINE = simpleWaterLikeFluid("red_wine", 0xDF7F8B);
    public static final SimpleFluid STRAD_WINE = simpleWaterLikeFluid("strad_wine", 0x173443);
    public static final SimpleFluid CHERRY_WINE = simpleWaterLikeFluid("cherry_wine", 0x7D100F);
    public static final SimpleFluid CRISTEL_WINE = simpleWaterLikeFluid("cristel_wine", 0xEB7C7C);
    public static final SimpleFluid CREEPERS_CRUSH = simpleLavaTexturedFluid("creepers_crush", 0xC5E755);
    public static final SimpleFluid KELP_CIDER = simpleWaterLikeFluid("kelp_cider", 0x4AF6A8);
    public static final SimpleFluid LILITU_WINE = simpleWaterLikeFluid("lilitu_wine", 0x5C151A);
    public static final SimpleFluid JO_SPECIAL_MIXTURE = simpleWaterLikeFluid("jo_special_mixture", 0xBE0E22);
    public static final SimpleFluid EISWEIN = simpleWaterLikeFluid("eiswein", 0x82D7FC);
    public static final SimpleFluid AEGIS_WINE = simpleWaterLikeFluid("aegis_wine", 0xB96F47);
    public static final SimpleFluid BOLVAR_WINE = simpleWaterLikeFluid("bolvar_wine", 0xE35E5E);
    public static final SimpleFluid CHORUS_WINE = simpleWaterLikeFluid("chorus_wine", 0xAF1FBF);
    public static final SimpleFluid VILLAGERS_FRIGHT = simpleWaterLikeFluid("villagers_fright", 0x8F96AE);
    public static final SimpleFluid CLARK_WINE = simpleWaterLikeFluid("clark_wine", 0x696969);
    public static final SimpleFluid MAGNETIC_WINE = simpleWaterLikeFluid("magnetic_wine", 0xFC8F72);
    public static final SimpleFluid STAL_WINE = simpleWaterLikeFluid("stal_wine", 0xB71818);
    public static final SimpleFluid CHENET_WINE = simpleWaterLikeFluid("chenet_wine", 0x551625);
    public static final SimpleFluid BOTTLE_MOJANG_NOIR = simpleWaterLikeFluid("bottle_mojang_noir", 0x7D6D7D);
    public static final SimpleFluid JELLIE_WINE = simpleWaterLikeFluid("jellie_wine", 0xB24571);
    public static final SimpleFluid BLAZEWINE_PINOT = simpleWaterLikeFluid("blazewine_pinot", 0xE5272C);
    public static final SimpleFluid NETHERITE_NECTAR = simpleWaterLikeFluid("netherite_nectar", 0x650627);
    public static final SimpleFluid GHASTLY_GRENACHE = simpleWaterLikeFluid("ghastly_grenache", 0x16605A);
    public static final SimpleFluid LAVA_FIZZ = simpleWaterLikeFluid("lava_fizz", 0xC42200);
    public static final SimpleFluid NETHER_FIZZ = simpleWaterLikeFluid("nether_fizz", 0x915A8F);

    // 自带专用 still/flowing 贴图的流体。
    public static final SimpleFluid LUBRICATING_OIL = textureFluid("lubricating_oil");
    public static final SimpleFluid ICE_LUBRICATING_OIL = textureFluid("ice_lubricating_oil");
    public static final SimpleFluid CAKE_BATTER = textureFluid("cake_batter");
    public static final SimpleFluid RED_VELVET_CAKE_BATTER = textureFluid("red_velvet_cake_batter");
    public static final SimpleFluid EGG_YOLK = textureFluid("egg_yolk");
    public static final SimpleFluid ARTIFICIAL_EGG_YOLK = textureFluid("artificial_egg_yolk");
    public static final SimpleFluid EGG_TART_FLUID = textureFluid("egg_tart_fluid");
    public static final SimpleFluid FIRE_DRAGON_BLOOD = textureFluid("fire_dragon_blood", 10);
    public static final SimpleFluid ICE_DRAGON_BLOOD = textureFluid("ice_dragon_blood", 10);
    public static final SimpleFluid LIGHTNING_DRAGON_BLOOD = textureFluid("lightning_dragon_blood", 10);

    // 旧 Core molten/slime/radiation 流体。
    public static final SimpleFluid MOLTEN_ANDESITE = simpleLavaTexturedFluid("molten_andesite", 0x8C8C8C);
    public static final SimpleFluid MOLTEN_AZURE_NEODYMIUM = simpleLavaTexturedFluid("molten_azure_neodymium", 0x4A8CFF);
    public static final SimpleFluid MOLTEN_SCARLET_NEODYMIUM = simpleLavaTexturedFluid("molten_scarlet_neodymium", 0xD7394F);
    public static final SimpleFluid MOLTEN_TITANIUM = simpleLavaTexturedFluid("molten_titanium", 0xC7C8D2);
    public static final SimpleFluid MOLTEN_MARTIAN_STEEL = simpleLavaTexturedFluid("molten_martian_steel", 0xA55B52);
    public static final SimpleFluid MOLTEN_FIRE_STEEL = simpleLavaTexturedFluid("molten_fire_steel", 0xE95A24);
    public static final SimpleFluid MOLTEN_ICE_STEEL = simpleLavaTexturedFluid("molten_ice_steel", 0xA7E9FF);
    public static final SimpleFluid MOLTEN_LIGHTNING_STEEL = simpleLavaTexturedFluid("molten_lightning_steel", 0xDCEEFF);
    public static final SimpleFluid MOLTEN_FORGED_STEEL = simpleLavaTexturedFluid("molten_forged_steel", 0x6F7180);
    public static final SimpleFluid MOLTEN_GLASS = simpleLavaTexturedFluid("molten_glass", 0xDDF8FF);
    public static final SimpleFluid MOLTEN_QUARTZ_GLASS = simpleLavaTexturedFluid("molten_quartz_glass", 0xF7F4E8);
    public static final SimpleFluid MOLTEN_QUARTZ_VIBRANT_GLASS = simpleLavaTexturedFluid("molten_quartz_vibrant_glass", 0xA6F4FF);
    public static final SimpleFluid SLIME = textureFluid("slime", 25);
    public static final SimpleFluid FERROUSLIME = textureFluid("ferrouslime", 25);
    public static final SimpleFluid CHORUSSLIME = textureFluid("chorusslime", 25);
    public static final SimpleFluid NUCLEAR_WASTE = textureFluid("nuclear_waste");

    // 配方专用流体：只注册源/流动流体，没有方块和桶。
    public static final SimpleFluid LUSH_CONFITURE_JELLY_FLUID = stillOnlyRecipeFluid("lush_confiture_jelly");
    public static final SimpleFluid LUSH_CONFITURE_JELLO_FLUID = stillOnlyRecipeFluid("lush_confiture_jello");
    public static final SimpleFluid BASE_SYRUP_FLUID = stillOnlyRecipeFluid("base_syrup");
    public static final SimpleFluid STRAWBERRY_SYRUP_FLUID = stillOnlyRecipeFluid("strawberry_syrup");
    public static final SimpleFluid VANILLA_SYRUP_FLUID = stillOnlyRecipeFluid("vanilla_syrup");
    public static final SimpleFluid MINT_SYRUP_FLUID = stillOnlyRecipeFluid("mint_syrup");
    public static final SimpleFluid BANANA_SYRUP_FLUID = stillOnlyRecipeFluid("banana_syrup");
    public static final SimpleFluid FILLING = stillOnlyRecipeFluid("filling");

    // 咖啡配方专用流体：水贴图染色，没有方块和桶。
    public static final SimpleFluid ESPRESSO_FLUID = waterLikeRecipeFluid("espresso_fluid", 0x8D5528);
    public static final SimpleFluid AMERICANO_FLUID = waterLikeRecipeFluid("americano_fluid", 0x53443D);
    public static final SimpleFluid RISTRETTO_FLUID = waterLikeRecipeFluid("ristretto_fluid", 0x572E18);
    public static final SimpleFluid LATTE_FLUID = waterLikeRecipeFluid("latte_fluid", 0xE2AD78);
    public static final SimpleFluid AFFOGATO_FLUID = waterLikeRecipeFluid("affogato_fluid", 0xF2D2BA);
    public static final SimpleFluid CON_PANNA_FLUID = waterLikeRecipeFluid("con_panna_fluid", 0x864F26);
    public static final SimpleFluid CAPPUCCINO_FLUID = waterLikeRecipeFluid("cappuccino_fluid", 0xBA894B);
    public static final SimpleFluid MACCHIATO_FLUID = waterLikeRecipeFluid("macchiato_fluid", 0x9B5F32);
    public static final SimpleFluid MOCHA_FLUID = waterLikeRecipeFluid("mocha_fluid", 0x975B21);

    // 旧 Core 虚拟配方流体。
    public static final SimpleFluid CARROT_MILKSHAKE = waterLikeRecipeFluid("carrot_milkshake", 0xFDC381);
    public static final SimpleFluid GLOW_BERRY_MILKSHAKE = waterLikeRecipeFluid("glow_berry_milkshake", 0xF5B256);
    public static final SimpleFluid ENCHANTED_FRUIT_MILKSHAKE = waterLikeRecipeFluid("enchanted_fruit_milkshake", 0xDFDA48);
    public static final SimpleFluid APPLE_MILKSHAKE = waterLikeRecipeFluid("apple_milkshake", 0xF6D894);
    public static final SimpleFluid BEETROOT_MILKSHAKE = waterLikeRecipeFluid("beetroot_milkshake", 0xEA4D5B);
    public static final SimpleFluid RED_GRAPEJUICE = waterLikeRecipeFluid("red_grapejuice", 0x73207A);
    public static final SimpleFluid JUNGLE_RED_GRAPEJUICE = waterLikeRecipeFluid("jungle_red_grapejuice", 0x4F1D85);
    public static final SimpleFluid SAVANNA_RED_GRAPEJUICE = waterLikeRecipeFluid("savanna_red_grapejuice", 0xBE4EE0);
    public static final SimpleFluid TAIGA_RED_GRAPEJUICE = waterLikeRecipeFluid("taiga_red_grapejuice", 0x7400A8);
    public static final SimpleFluid WHITE_GRAPEJUICE = waterLikeRecipeFluid("white_grapejuice", 0x819E4C);
    public static final SimpleFluid JUNGLE_WHITE_GRAPEJUICE = waterLikeRecipeFluid("jungle_white_grapejuice", 0x48531E);
    public static final SimpleFluid SAVANNA_WHITE_GRAPEJUICE = waterLikeRecipeFluid("savanna_white_grapejuice", 0x98AF3D);
    public static final SimpleFluid TAIGA_WHITE_GRAPEJUICE = waterLikeRecipeFluid("taiga_white_grapejuice", 0x77882F);
    public static final SimpleFluid WARPED_GRAPEJUICE = waterLikeRecipeFluid("warped_grapejuice", 0x005251);
    public static final SimpleFluid CRIMSON_GRAPEJUICE = waterLikeRecipeFluid("crimson_grapejuice", 0x651114);

    // 粘稠流体：使用 CDC 中性厚流体贴图，避免旧 lava 贴图橙色污染。
    public static final SimpleFluid MALICE_SOLUTION = simpleLavaTexturedFluid("malice_solution", 0x33E6EF);
    public static final SimpleFluid SKY_SOLUTION = simpleLavaTexturedFluid("sky_solution", 0x494949);
    public static final SimpleFluid UNFERMENTED_PAPER_PULP = simpleLavaTexturedFluid("unfermented_paper_pulp", 0xF0FFFF);
    public static final SimpleFluid YEAST = simpleLavaTexturedFluid("yeast", 0x9B897E);

    // 创造栏、客户端颜色和批量数据生成共用的流体清单。
    public static final List<SimpleFluid> SIMPLE_FLUIDS = List.of(
            FUEL_MIXTURES,
            LIGHT_CRUDE_OIL,
            ETHYLENE_FLUID,
            SPENT_LIQUOR,
            PAPER_PULP,
            UNREFINED_SUGAR,
            CRYO_FUEL,
            NUT_MILK,
            VINEGAR,
            RADON,
            SOYA_MILK,
            ANCIENT_COFFEE,
            TORCHFLOWER_TEA,
            CHERRY_PETAL_TEA,
            PITCHER_PLANT_TEA,
            FIDDLEHEAD_TEA,
            SCARLET_TEA,
            LEMON_BLACK_TEA,
            TEA_MOCHA,
            SAIDI_TEA,
            CORNFLOWER_TEA,
            SAKURA_HONEY_TEA,
            GENMAI_TEA,
            GREEN_WATER,
            WHITE_TEA,
            SPRING_SODA,
            SUMMER_CORDIAL,
            AUTUMN_TEA,
            WINTER_GLOGG,
            APPLE_JUICE,
            MEAD,
            APPLE_CIDER,
            APPLE_WINE,
            MELLOHI_WINE,
            GLOWING_WINE,
            SOLARIS_WINE,
            NOIR_WINE,
            RED_WINE,
            STRAD_WINE,
            CHERRY_WINE,
            CRISTEL_WINE,
            CREEPERS_CRUSH,
            KELP_CIDER,
            LILITU_WINE,
            JO_SPECIAL_MIXTURE,
            EISWEIN,
            AEGIS_WINE,
            BOLVAR_WINE,
            CHORUS_WINE,
            VILLAGERS_FRIGHT,
            CLARK_WINE,
            MAGNETIC_WINE,
            STAL_WINE,
            CHENET_WINE,
            BOTTLE_MOJANG_NOIR,
            JELLIE_WINE,
            BLAZEWINE_PINOT,
            NETHERITE_NECTAR,
            GHASTLY_GRENACHE,
            LAVA_FIZZ,
            NETHER_FIZZ,
            LUBRICATING_OIL,
            ICE_LUBRICATING_OIL,
            CAKE_BATTER,
            RED_VELVET_CAKE_BATTER,
            EGG_YOLK,
            ARTIFICIAL_EGG_YOLK,
            EGG_TART_FLUID,
            FIRE_DRAGON_BLOOD,
            ICE_DRAGON_BLOOD,
            LIGHTNING_DRAGON_BLOOD,
            MOLTEN_ANDESITE,
            MOLTEN_AZURE_NEODYMIUM,
            MOLTEN_SCARLET_NEODYMIUM,
            MOLTEN_TITANIUM,
            MOLTEN_MARTIAN_STEEL,
            MOLTEN_FIRE_STEEL,
            MOLTEN_ICE_STEEL,
            MOLTEN_LIGHTNING_STEEL,
            MOLTEN_FORGED_STEEL,
            MOLTEN_GLASS,
            MOLTEN_QUARTZ_GLASS,
            MOLTEN_QUARTZ_VIBRANT_GLASS,
            SLIME,
            FERROUSLIME,
            CHORUSSLIME,
            NUCLEAR_WASTE,
            LUSH_CONFITURE_JELLY_FLUID,
            LUSH_CONFITURE_JELLO_FLUID,
            BASE_SYRUP_FLUID,
            STRAWBERRY_SYRUP_FLUID,
            VANILLA_SYRUP_FLUID,
            MINT_SYRUP_FLUID,
            BANANA_SYRUP_FLUID,
            FILLING,
            ESPRESSO_FLUID,
            AMERICANO_FLUID,
            RISTRETTO_FLUID,
            LATTE_FLUID,
            AFFOGATO_FLUID,
            CON_PANNA_FLUID,
            CAPPUCCINO_FLUID,
            MACCHIATO_FLUID,
            MOCHA_FLUID,
            CARROT_MILKSHAKE,
            GLOW_BERRY_MILKSHAKE,
            ENCHANTED_FRUIT_MILKSHAKE,
            APPLE_MILKSHAKE,
            BEETROOT_MILKSHAKE,
            RED_GRAPEJUICE,
            JUNGLE_RED_GRAPEJUICE,
            SAVANNA_RED_GRAPEJUICE,
            TAIGA_RED_GRAPEJUICE,
            WHITE_GRAPEJUICE,
            JUNGLE_WHITE_GRAPEJUICE,
            SAVANNA_WHITE_GRAPEJUICE,
            TAIGA_WHITE_GRAPEJUICE,
            WARPED_GRAPEJUICE,
            CRIMSON_GRAPEJUICE,
            MALICE_SOLUTION,
            SKY_SOLUTION,
            UNFERMENTED_PAPER_PULP,
            YEAST
    );

    private ModFluids() {
    }

    public static void register(IEventBus modEventBus) {
        FLUID_TYPES.register(modEventBus);
        FLUIDS.register(modEventBus);
    }

    // ---- 注册辅助方法 ----

    private static SimpleFluid simpleWaterLikeFluid(String name, int rgb) {
        return simpleFluid(name, rgb, WATER_STILL, WATER_FLOWING, WATER_OVERLAY);
    }

    private static SimpleFluid simpleLavaTexturedFluid(String name, int rgb) {
        return simpleFluid(name, rgb, THICK_STILL, THICK_FLOWING, null);
    }

    private static SimpleFluid textureFluid(String name) {
        return textureFluid(name, 5);
    }

    private static SimpleFluid textureFluid(String name, int tickRate) {
        ResourceLocation stillTexture = ResourceLocation.fromNamespaceAndPath(CreateDelightCore.MODID, "block/fluid/" + name + "/still");
        ResourceLocation flowingTexture = ResourceLocation.fromNamespaceAndPath(CreateDelightCore.MODID, "block/fluid/" + name + "/flowing");
        return simpleFluid(name, 0xFFFFFF, stillTexture, flowingTexture, null, tickRate);
    }

    private static SimpleFluid stillOnlyRecipeFluid(String name) {
        ResourceLocation stillTexture = ResourceLocation.fromNamespaceAndPath(CreateDelightCore.MODID, "block/fluid/" + name + "/still");
        return simpleFluid(name, 0xFFFFFF, stillTexture, stillTexture, null, 5, false, false);
    }

    private static SimpleFluid waterLikeRecipeFluid(String name, int rgb) {
        return simpleFluid(name, rgb, WATER_STILL, WATER_FLOWING, WATER_OVERLAY, 5, false, false);
    }

    private static SimpleFluid simpleFluid(String name, int rgb, ResourceLocation stillTexture, ResourceLocation flowingTexture, ResourceLocation overlayTexture) {
        return simpleFluid(name, rgb, stillTexture, flowingTexture, overlayTexture, 5);
    }

    private static SimpleFluid simpleFluid(String name, int rgb, ResourceLocation stillTexture, ResourceLocation flowingTexture, ResourceLocation overlayTexture, int tickRate) {
        return simpleFluid(name, rgb, stillTexture, flowingTexture, overlayTexture, tickRate, true, true);
    }

    private static SimpleFluid simpleFluid(String name, int rgb, ResourceLocation stillTexture, ResourceLocation flowingTexture, ResourceLocation overlayTexture, int tickRate, boolean hasBlock, boolean hasBucket) {
        SimpleFluidReferences references = new SimpleFluidReferences(tickRate);
        DeferredHolder<FluidType, FluidType> fluidType = FLUID_TYPES.register(name, () -> new FluidType(FluidType.Properties.create()
                .descriptionId("fluid." + CreateDelightCore.MODID + "." + name)
                .canExtinguish(true)
                .canHydrate(true)
                .supportsBoating(true)
                .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)
        ));

        DeferredHolder<Fluid, BaseFlowingFluid.Source> source = FLUIDS.register(name, () -> new BaseFlowingFluid.Source(references.properties()));
        DeferredHolder<Fluid, BaseFlowingFluid.Flowing> flowing = FLUIDS.register("flowing_" + name, () -> new BaseFlowingFluid.Flowing(references.properties()));
        DeferredBlock<LiquidBlock> block = hasBlock ? ModBlocks.BLOCKS.register(name, () -> new LiquidBlock(source.get(), BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).noLootTable())) : null;
        DeferredItem<BucketItem> bucket = hasBucket ? ModItems.ITEMS.registerItem(name + "_bucket", properties -> new BucketItem(source.get(), properties
                .craftRemainder(Items.BUCKET)
                .stacksTo(1)
        )) : null;

        references.set(fluidType, source, flowing, block, bucket);
        return new SimpleFluid(name, DEFAULT_THIN_FLUID_ALPHA << 24 | rgb, stillTexture, flowingTexture, overlayTexture, fluidType, source, flowing, block, bucket);
    }

    // 单个简单流体的注册结果。部分 recipe-only 流体没有 block/bucket。
    public record SimpleFluid(
            String name,
            int tintColor,
            ResourceLocation stillTexture,
            ResourceLocation flowingTexture,
            ResourceLocation overlayTexture,
            DeferredHolder<FluidType, FluidType> fluidType,
            DeferredHolder<Fluid, BaseFlowingFluid.Source> source,
            DeferredHolder<Fluid, BaseFlowingFluid.Flowing> flowing,
            DeferredBlock<LiquidBlock> block,
            DeferredItem<BucketItem> bucket
    ) {
        public boolean hasBucket() {
            return bucket != null;
        }
    }

    // BaseFlowingFluid.Properties 需要互相引用 source/flowing/block/bucket，所以先占位再回填。
    private static final class SimpleFluidReferences {
        private DeferredHolder<FluidType, FluidType> fluidType;
        private DeferredHolder<Fluid, BaseFlowingFluid.Source> source;
        private DeferredHolder<Fluid, BaseFlowingFluid.Flowing> flowing;
        private DeferredBlock<LiquidBlock> block;
        private DeferredItem<BucketItem> bucket;
        private final int tickRate;

        private SimpleFluidReferences(int tickRate) {
            this.tickRate = tickRate;
        }

        private void set(
                DeferredHolder<FluidType, FluidType> fluidType,
                DeferredHolder<Fluid, BaseFlowingFluid.Source> source,
                DeferredHolder<Fluid, BaseFlowingFluid.Flowing> flowing,
                DeferredBlock<LiquidBlock> block,
                DeferredItem<BucketItem> bucket
        ) {
            this.fluidType = fluidType;
            this.source = source;
            this.flowing = flowing;
            this.block = block;
            this.bucket = bucket;
        }

        private BaseFlowingFluid.Properties properties() {
            BaseFlowingFluid.Properties properties = new BaseFlowingFluid.Properties(fluidType, source, flowing)
                    .tickRate(tickRate);
            if (block != null) {
                properties.block(block);
            }
            if (bucket != null) {
                properties.bucket(bucket);
            }
            return properties;
        }
    }
}
