package io.github.jasonsimpart.createdelightcore.registry;

import com.github.alexthe666.iceandfire.block.IafBlockRegistry;
import com.renyigesai.bakeries.block.pizza.PizzaBlock;
import com.renyigesai.bakeries.block.pizza.RawPizzaBlock;
import com.simibubi.create.content.decoration.encasing.CasingBlock;
import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import com.simibubi.create.foundation.block.connected.SimpleCTBehaviour;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import com.tterrag.registrate.providers.loot.RegistrateBlockLootTables;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.entry.ItemEntry;
import io.github.jasonsimpart.createdelightcore.CreateDelightCore;
import io.github.jasonsimpart.createdelightcore.content.block.*;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import io.github.jasonsimpart.createdelightcore.content.item.JellyBottleItem;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraftforge.client.model.generators.BlockModelBuilder;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.Tags;
import net.minecraftforge.registries.ForgeRegistries;

import static io.github.jasonsimpart.createdelightcore.CreateDelightCore.REGISTRATE;
import static io.github.jasonsimpart.createdelightcore.registry.CDTags.forgeBlockTag;
import static io.github.jasonsimpart.createdelightcore.registry.CDTags.forgeItemTag;

public class CDBlocks {
    public static final ResourceKey<CreativeModeTab> MISC_TAB = CDCreativeTabs.MISC.getKey();
    public static final ResourceKey<CreativeModeTab> COIN_TAB = CDCreativeTabs.COIN.getKey();
    public static final ResourceKey<CreativeModeTab> FOOD_TAB = CDCreativeTabs.FOOD.getKey();
    //coin
    public static final BlockEntry<CoinPileBlock> IRON_COIN_PILE = simpleCoinPileBlock("iron", CDItems.IRON);
    public static final BlockEntry<CoinPileBlock> COPPER_COIN_PILE = simpleCoinPileBlock("copper", CDItems.COPPER);
    public static final BlockEntry<CoinPileBlock> GOLD_COIN_PILE = simpleCoinPileBlock("gold", CDItems.GOLD);
    public static final BlockEntry<CoinPileBlock> EMERALD_COIN_PILE = simpleCoinPileBlock("emerald", CDItems.EMERALD);
    public static final BlockEntry<CoinPileBlock> NETHERITE_COIN_PILE = simpleCoinPileBlock("netherite", CDItems.NETHERITE);
    //tin
    //ore
    public static final BlockEntry<Block> TIN_ORE = simpleOre("tin", BlockTags.NEEDS_IRON_TOOL, CDItems.RAW_TIN);
    public static final BlockEntry<Block> DEEPSLATE_TIN_ORE = simpleDeepslateOre("tin", BlockTags.NEEDS_IRON_TOOL, CDItems.RAW_TIN);
    //metal
    public static final BlockEntry<Block> RAW_TIN = simpleRawMetalBlock("tin", BlockTags.NEEDS_IRON_TOOL);
    public static final BlockEntry<Block> TIN = simpleMetalBlock("tin", BlockTags.NEEDS_IRON_TOOL);
    //bronze
    public static final BlockEntry<Block> BRONZE = simpleMetalBlock("bronze", BlockTags.NEEDS_IRON_TOOL);
    //forged_steel
    public static final BlockEntry<Block> FORGED_STEEL = simpleMetalBlock("forged_steel", BlockTags.NEEDS_DIAMOND_TOOL);
    //casing
    public static final BlockEntry<CasingBlock> STEEL_CASING = simpleCasingBlock("steel", Rarity.COMMON, CDCSpriteShifts.STEEL_CASING);
    public static final BlockEntry<CasingBlock> FORGE_STEEL_CASING = simpleCasingBlock("forge_steel", Rarity.RARE, CDCSpriteShifts.FORGE_STEEL_CASING);
    public static final BlockEntry<GlassCassing> STEEL_GLASS_CASING = simpleGlassCasingBlock("steel", Rarity.COMMON, CDCSpriteShifts.STEEL_GLASS_CASING);
    public static final BlockEntry<GlassCassing> STEEL_CLEAR_GLASS_CASING = simpleGlassCasingBlock("steel_clear", Rarity.COMMON, CDCSpriteShifts.STEEL_CLEAR_GLASS_CASING);
    //syrup
    public static final BlockEntry<SyrupBlock> BASE = simpleSyrupBlock("base");
    public static final BlockEntry<SyrupBlock> STRAWBERRY = simpleSyrupBlock("strawberry");
    public static final BlockEntry<SyrupBlock> VANILLA = simpleSyrupBlock("vanilla");
    public static final BlockEntry<SyrupBlock> MINT = simpleSyrupBlock("mint");
    public static final BlockEntry<SyrupBlock> BANANA = simpleSyrupBlock("banana");
    //jam_bottle
    public static final BlockEntry<JellyBottleBlock> LUSH_CONFITURE = simpleJellyBottleBlock("lush_confiture", 1, 1, 0XF0612E);
    //jelly_block
    public static final BlockEntry<JellyBlock> LUSH_CONFITURE_JELLY = simpleJellyBlock("lush_confiture_jelly", "lush_confiture", 0XF0612E);
    //jello_block
    public static final BlockEntry<JelloBlock> LUSH_CONFITURE_JELLO = simpleJelloBlock("lush_confiture_jello", "lush_confiture", 0XF0612E);
    //fragment_of_border
    public static final BlockEntry<GlassBlock> FRAGMENT_OF_BORDER =
            REGISTRATE.block("fragment_of_border", GlassBlock::new)
                    .item()
                    .properties(p -> p.rarity(Rarity.RARE))
                    .tab(MISC_TAB)
                    .build()
                    .initialProperties(() -> Blocks.GLASS)
                    .properties(p -> p
                            .lightLevel(bs -> 15)
                            .strength(10.0F)
                            .sound(SoundType.METAL)
                            .noLootTable())
                .addLayer(() -> RenderType::translucent)
                .tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .register();
    //flower_cluster
    public static final BlockEntry<FlowerClusterBlock> FIRE_LILY_CLUSTER =
            REGISTRATE.block("fire_lily_cluster", p -> new FlowerClusterBlock(
                    BlockBehaviour.Properties.copy(IafBlockRegistry.FIRE_LILY.get()),
                            () -> IafBlockRegistry.FIRE_LILY.get().asItem()))
                    .item(FlowerClusterBlockItem::new)
                    .tab(MISC_TAB)
                    .build()
                    .register();
    public static final BlockEntry<FlowerClusterBlock> FROST_LILY_CLUSTER =
            REGISTRATE.block("frost_lily_cluster", p -> new FlowerClusterBlock(
                    BlockBehaviour.Properties.copy(IafBlockRegistry.FROST_LILY.get()),
                            () -> IafBlockRegistry.FROST_LILY.get().asItem()))
                    .item(FlowerClusterBlockItem::new)
                    .tab(MISC_TAB)
                    .build()
                    .register();
    public static final BlockEntry<FlowerClusterBlock> LIGHTNING_LILY_CLUSTER =
            REGISTRATE.block("lightning_lily_cluster", p -> new FlowerClusterBlock(
                    BlockBehaviour.Properties.copy(IafBlockRegistry.LIGHTNING_LILY.get()),
                            () -> IafBlockRegistry.LIGHTNING_LILY.get().asItem()))
                    .item(FlowerClusterBlockItem::new)
                    .tab(MISC_TAB)
                    .build()
                    .register();
    //luna_soil
    public static final BlockEntry<LunaSoilBlock> LUNA_SOIL =
            REGISTRATE.block("luna_soil", p -> new LunaSoilBlock(BlockBehaviour.Properties.copy(Blocks.DIRT).randomTicks()))
                    .tag(BlockTags.MINEABLE_WITH_SHOVEL, BlockTags.DIRT)
                    .item()
                    .tab(MISC_TAB)
                    .build()
                    .register();
    public static final BlockEntry<LunaSoilFarmlandBlock> LUNA_SOIL_FARMLAND =
            REGISTRATE.block("luna_soil_farmland", properties -> new LunaSoilFarmlandBlock(BlockBehaviour.Properties.copy(Blocks.FARMLAND)))
                    .item()
                    .tab(MISC_TAB)
                    .build()
                    .register();
    public static final BlockEntry<PhantomCompostBlock> PHANTOM_COMPOST =
            REGISTRATE.block("phantom_compost", p -> new PhantomCompostBlock(
                            BlockBehaviour.Properties
                                    .copy(Blocks.DIRT)
                                    .strength(1.2F)
                                    .sound(SoundType.CROP)))
                    .tag(BlockTags.MINEABLE_WITH_SHOVEL)
                    .item()
                    .tab(MISC_TAB)
                    .build()
                    .register();
    // pizza
    public static final BlockEntry<RawPizzaBlock> RAW_VEGETABLE_PIZZA = rawPizzaBlock("vegetable");
    public static final BlockEntry<PizzaBlock> VEGETABLE_PIZZA = pizzaBlock("vegetable");
    public static final BlockEntry<RawPizzaBlock> RAW_MEATLOVERS_PIZZA = rawPizzaBlock("meatlovers");
    public static final BlockEntry<PizzaBlock> MEATLOVERS_PIZZA = pizzaBlock("meatlovers");
    public static final BlockEntry<RawPizzaBlock> RAW_NETHER_PIZZA = rawPizzaBlock("nether");
    public static final BlockEntry<PizzaBlock> NETHER_PIZZA = pizzaBlock("nether");


    public static BlockEntry<CoinPileBlock> simpleCoinPileBlock(String coinTier, ItemEntry<Item> coinItem) {
        return REGISTRATE.block(coinTier + "_coin_pile", p -> new CoinPileBlock(p, coinItem))
                .properties(p -> p
                        .mapColor(MapColor.METAL)
                        .strength(0.3F, 1.0F)
                        .noOcclusion()
                        .sound(IafBlockRegistry.SOUND_TYPE_GOLD)
                )
                .blockstate((ctx, pvd) -> pvd.getVariantBuilder(ctx.get()).forAllStates(state ->
                        ConfiguredModel.builder()
                                .modelFile(coinPileModel(pvd, coinTier, state.getValue(CoinPileBlock.LAYERS)))
                                .build()))
                .loot((lt, block) -> {
                    var pool = net.minecraft.world.level.storage.loot.LootPool.lootPool()
                            .setRolls(ConstantValue.exactly(1.0F));
                    for (int layers = 1; layers <= 8; layers++) {
                        pool.add(LootItem.lootTableItem(coinItem.get())
                                .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                                        .setProperties(StatePropertiesPredicate.Builder.properties()
                                                .hasProperty(CoinPileBlock.LAYERS, layers)))
                                .apply(SetItemCountFunction.setCount(ConstantValue.exactly(layers))));
                    }
                    lt.add(block, net.minecraft.world.level.storage.loot.LootTable.lootTable().withPool(pool));
                })
                .register();
    }

    public static BlockEntry<RawPizzaBlock> rawPizzaBlock(String name) {
        String id = "raw_" + name + "_pizza";
        return REGISTRATE.block(id, p -> new RawPizzaBlock(() -> ForgeRegistries.ITEMS.getValue(CreateDelightCore.id(id))))
                .blockstate((ctx, pvd) -> pvd.simpleBlock(ctx.get(),
                        new ModelFile.UncheckedModelFile(pvd.modLoc("block/raw_pizza"))))
                .item()
                .properties(p -> p.stacksTo(16))
                .transform(b -> b.model((ctx, pvd) -> pvd.generated(ctx, pvd.modLoc("item/" + id))))
                .tab(FOOD_TAB)
                .build()
                .register();
    }

    public static BlockEntry<PizzaBlock> pizzaBlock(String name) {
        String id = name + "_pizza";
        String texture = switch (name) {
            case "vegetable", "meatlovers" -> "cooked_" + id;
            default -> id;
        };
        return REGISTRATE.block(id, p -> new PizzaBlock(2, 0.1F))
                .blockstate((ctx, pvd) -> pvd.getVariantBuilder(ctx.get()).forAllStates(state -> {
                    Direction facing = state.getValue(HorizontalDirectionalBlock.FACING);
                    int slice = state.getValue(PizzaBlock.SLICE);
                    int rotation = switch (facing) {
                        case EAST -> 270;
                        case NORTH -> 180;
                        case WEST -> 90;
                        default -> 0;
                    };
                    return ConfiguredModel.builder()
                            .modelFile(new ModelFile.UncheckedModelFile(
                                    pvd.modLoc("block/pizza_" + (slice + 1))))
                            .rotationY(rotation)
                            .build();
                }))
                .item()
                .transform(b -> b.model((ctx, pvd) -> pvd.generated(ctx, pvd.modLoc("item/" + texture))))
                .tab(FOOD_TAB)
                .build()
                .loot((lt, block) -> lt.add(block, net.minecraft.world.level.storage.loot.LootTable.lootTable()
                        .withPool(net.minecraft.world.level.storage.loot.LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(1.0F))
                                .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                                        .setProperties(StatePropertiesPredicate.Builder.properties()
                                                .hasProperty(PizzaBlock.SLICE, 0)))
                                .add(LootItem.lootTableItem(block)))))
                .register();
    }

    private static ModelFile coinPileModel(RegistrateBlockstateProvider pvd, String coinTier, int layers) {
        int height = layers * 2;
        String modelName = coinTier + "_coin_pile_height" + height;
        ResourceLocation texture = pvd.modLoc("block/" + coinTier + "_coin_pile");

        if (layers == 8)
            return pvd.models().cubeAll(modelName, texture);

        BlockModelBuilder model = pvd.models().getBuilder(modelName)
                .parent(new ModelFile.UncheckedModelFile("block/thin_block"))
                .texture("particle", texture)
                .texture("texture", texture);

        model.element()
                .from(0.0F, 0.0F, 0.0F)
                .to(16.0F, height, 16.0F)
                .face(Direction.DOWN).uvs(0.0F, 0.0F, 16.0F, 16.0F).texture("#texture").cullface(Direction.DOWN).end()
                .face(Direction.UP).uvs(0.0F, 0.0F, 16.0F, 16.0F).texture("#texture").end()
                .face(Direction.NORTH).uvs(0.0F, 16.0F - height, 16.0F, 16.0F).texture("#texture").cullface(Direction.NORTH).end()
                .face(Direction.SOUTH).uvs(0.0F, 16.0F - height, 16.0F, 16.0F).texture("#texture").cullface(Direction.SOUTH).end()
                .face(Direction.WEST).uvs(0.0F, 16.0F - height, 16.0F, 16.0F).texture("#texture").cullface(Direction.WEST).end()
                .face(Direction.EAST).uvs(0.0F, 16.0F - height, 16.0F, 16.0F).texture("#texture").cullface(Direction.EAST).end()
                .end();
        return model;
    }

    public static BlockEntry<Block> simpleMetalBlock(String metalName, TagKey<Block> pickaxeLevel) {
        return REGISTRATE.block(metalName + "_block", Block::new)
                .item()
                .tag(Tags.Items.STORAGE_BLOCKS,
                        forgeItemTag("storage_blocks/" + metalName))
                .tab(MISC_TAB)
                .build()
                .initialProperties(() -> Blocks.GOLD_BLOCK)
                .properties(p -> p
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                )
                .tag(BlockTags.MINEABLE_WITH_PICKAXE, pickaxeLevel)
                .tag(Tags.Blocks.STORAGE_BLOCKS,
                        forgeBlockTag("storage_blocks/" + metalName))
                .register();
    }

    public static BlockEntry<Block> simpleRawMetalBlock(String metalName, TagKey<Block> pickaxeLevel) {
        return REGISTRATE.block("raw_" + metalName + "_block", Block::new)
                .item()
                .tag(Tags.Items.STORAGE_BLOCKS,
                        forgeItemTag("storage_blocks/raw_" + metalName))
                .tab(MISC_TAB)
                .build()
                .initialProperties(() -> Blocks.RAW_GOLD_BLOCK)
                .properties(p -> p
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                )
                .tag(BlockTags.MINEABLE_WITH_PICKAXE, pickaxeLevel)
                .tag(Tags.Blocks.STORAGE_BLOCKS,
                        forgeBlockTag("storage_blocks/raw_" + metalName))
                .register();
    }

    public static BlockEntry<Block> simpleOre(String metalName, TagKey<Block> pickaxeLevel, ItemEntry<Item> dropItem) {
        return REGISTRATE.block(metalName + "_ore", Block::new)
                .item()
                .tag(Tags.Items.ORES, forgeItemTag("ores/" + metalName),
                        forgeItemTag("ores_in_ground/stone"))
                .tab(MISC_TAB)
                .build()
                .initialProperties(() -> Blocks.GOLD_ORE)
                .properties(p -> p.mapColor(MapColor.METAL)
                        .requiresCorrectToolForDrops()
                        .sound(SoundType.STONE)
                )
                .loot((lt, b) -> lt.add(b,
                        RegistrateBlockLootTables.createSilkTouchDispatchTable(b,
                                lt.applyExplosionDecay(b, LootItem.lootTableItem(dropItem.get())
                                        .apply(ApplyBonusCount.addOreBonusCount(Enchantments.BLOCK_FORTUNE))))))
                .tag(BlockTags.MINEABLE_WITH_PICKAXE, pickaxeLevel)
                .tag(Tags.Blocks.ORES, forgeBlockTag("ores/" + metalName),
                        forgeBlockTag("ores_in_ground/stone"))
                .register();
    }

    public static BlockEntry<Block> simpleDeepslateOre(String metalName, TagKey<Block> pickaxeLevel, ItemEntry<Item> dropItem) {
        return REGISTRATE.block("deepslate_" + metalName + "_ore", Block::new)
                .item()
                .tag(Tags.Items.ORES, forgeItemTag("ores/" + metalName),
                        forgeItemTag("ores_in_ground/deepslate"))
                .tab(MISC_TAB)
                .build()
                .initialProperties(() -> Blocks.GOLD_ORE)
                .properties(p -> p.mapColor(MapColor.METAL)
                        .requiresCorrectToolForDrops()
                        .sound(SoundType.DEEPSLATE)
                )
                .loot((lt, b) -> lt.add(b,
                        RegistrateBlockLootTables.createSilkTouchDispatchTable(b,
                                lt.applyExplosionDecay(b, LootItem.lootTableItem(dropItem.get())
                                        .apply(ApplyBonusCount.addOreBonusCount(Enchantments.BLOCK_FORTUNE))))))
                .tag(BlockTags.MINEABLE_WITH_PICKAXE, pickaxeLevel)
                .tag(Tags.Blocks.ORES, forgeBlockTag("ores/" + metalName),
                        forgeBlockTag("ores_in_ground/deepslate"))
                .register();
    }

    public static BlockEntry<Block> simpleBlock(String name, float destoryTime, float resistance, SoundType soundType,TagKey<Block> tool, TagKey<Block> toolLevel) {
        return REGISTRATE.block(name + "_block", Block::new)
                .item()
                .properties(p -> p.rarity(Rarity.COMMON))
                .tab(MISC_TAB)
                .build()
                .properties(properties -> properties
                        .strength(destoryTime, resistance)
                        .sound(soundType)
                        .requiresCorrectToolForDrops()
                )
                .tag(tool, toolLevel)
                .register();
    }

    public static BlockEntry<CasingBlock> simpleCasingBlock(String name, Rarity rarity, CTSpriteShiftEntry spriteShifts){
        return REGISTRATE.block(name + "_casing", CasingBlock::new)
                .item()
                .properties(p -> p.rarity(rarity))
                .tab(MISC_TAB)
                .build()
                .properties(p -> p
                        .strength(6.0F, 1200.0F)
                        .mapColor(MapColor.METAL)
                        .sound(SoundType.METAL)
                )
                .onRegister(CreateRegistrate.connectedTextures(() -> new SimpleCTBehaviour(spriteShifts)))
                .tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .register();
    }

    public static BlockEntry<GlassCassing> simpleGlassCasingBlock(String name, Rarity rarity, CTSpriteShiftEntry spriteShifts){
        //noinspection removal
        return REGISTRATE.block(name + "_glass_casing", GlassCassing::new)
                .item()
                .properties(p -> p.rarity(rarity))
                .tab(MISC_TAB)
                .build()
                .initialProperties(() -> Blocks.GLASS)
                .properties(p -> p
                        .mapColor(MapColor.METAL)
                        .sound(SoundType.GLASS)
                )
                .addLayer(() -> RenderType::translucent)
                .onRegister(CreateRegistrate.connectedTextures(() -> new SimpleCTBehaviour(spriteShifts)))
                .register();
    }

    public static BlockEntry<SyrupBlock> simpleSyrupBlock(String name){
        return REGISTRATE.block(name + "_syrup", SyrupBlock::new)
                .item()
                .properties(p -> p.rarity(Rarity.COMMON))
                .tab(FOOD_TAB)
                .build()
                .initialProperties(() -> Blocks.HONEY_BLOCK)
                .properties(properties -> properties
                        .instabreak()
                        .noOcclusion()
                        .sound(SoundType.HONEY_BLOCK)
                )
                .blockstate((ctx, pvd) -> pvd.simpleBlock(ctx.get(), pvd.models().getBuilder(ctx.getName())
                        .parent(new ModelFile.UncheckedModelFile(pvd.modLoc("block/syrup")))
                        .texture("top", pvd.modLoc("block/" + name + "_syrup"))
                        .texture("bottom", pvd.modLoc("block/" + name + "_syrup"))
                        .texture("side", pvd.modLoc("block/" + name + "_syrup"))
                        .renderType("translucent")
                ))
                .register();
    }

    public static BlockEntry<JellyBottleBlock> simpleJellyBottleBlock(String name, int nutrition, float saturation, int color){
        return REGISTRATE.block(name + "_jelly_bottle", JellyBottleBlock::new)
                .blockstate((ctx, pvd) ->pvd.simpleBlock(ctx.get(), pvd.models().getBuilder(ctx.getName())
                        .parent(new ModelFile.UncheckedModelFile(pvd.modLoc("block/jam_bottle_block")))
                        .texture("cap_top", pvd.modLoc("block/jam_bottle_cap_top"))
                        .texture("cap_bottom", pvd.modLoc("block/jam_bottle_cap_bottom"))
                        .texture("body", pvd.modLoc("block/jam_bottle_body"))
                        .texture("content", pvd.modLoc("block/" + name + "_jam_content"))
                        .renderType("cutout")
                ))
                .item(JellyBottleItem::new)
                .properties(p -> p
                        .food(new FoodProperties.Builder()
                                .nutrition(nutrition)
                                .saturationMod(saturation)
                                .build())
                        .rarity(Rarity.COMMON)
                )
                .transform(b -> b.model((ctx, pvd) -> pvd.generated(ctx,
                                pvd.modLoc("item/jam_bottle"),
                                pvd.modLoc("item/jam")))
                        .color(() -> () -> ((pStack, layer) -> layer == 0 ? -1 : color))
                )
                .tab(FOOD_TAB)
                .build()
                .properties(properties -> properties
                        .strength(0.3F)
                        .noOcclusion()
                        .sound(SoundType.GLASS)
                )
                .register();
    }

    public static BlockEntry<JellyBlock> simpleJellyBlock(String name, String fruit, int color){
        return REGISTRATE.block(name, p -> new JellyBlock(BlockBehaviour.Properties.copy(Blocks.HONEY_BLOCK), fruit))
                .blockstate((ctx, pvd) -> pvd.simpleBlock(ctx.get(), pvd.models()
                        .withExistingParent(ctx.getName(), pvd.modLoc("block/tinted"))
                        .texture("all", pvd.modLoc("block/jelly"))
                        .renderType("translucent")))
                .color(() -> () -> (s, l, p, x) -> color)
                .item()
                .color(() -> () -> (s, x) -> color)
                .tab(FOOD_TAB)
                .build()
                .register();
    }

    public static BlockEntry<JelloBlock> simpleJelloBlock(String name, String fruit, int color){
        return REGISTRATE.block(name, p -> new JelloBlock(BlockBehaviour.Properties.copy(Blocks.SLIME_BLOCK), fruit))
                .blockstate((ctx, pvd) -> pvd.simpleBlock(ctx.get(), pvd.models()
                        .withExistingParent(ctx.getName(), pvd.modLoc("block/tinted"))
                        .texture("all", pvd.modLoc("block/jello"))
                        .renderType("translucent")))
                .color(() -> () -> (s, l, p, x) -> color)
                .item()
                .color(() -> () -> (s, x) -> color)
                .tab(FOOD_TAB)
                .build()
                .register();
    }


    public static void init() {
    }
}
