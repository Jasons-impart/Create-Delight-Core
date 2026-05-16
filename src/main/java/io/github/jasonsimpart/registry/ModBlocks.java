package io.github.jasonsimpart.registry;

import io.github.jasonsimpart.CreateDelightCore;
import io.github.jasonsimpart.content.block.JelloBlock;
import io.github.jasonsimpart.content.block.JellyBottleBlock;
import io.github.jasonsimpart.content.block.JellyBlock;
import io.github.jasonsimpart.content.block.SyrupBlock;
import io.github.jasonsimpart.content.item.JellyBottleItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(CreateDelightCore.MODID);

    public static final DeferredBlock<Block> TIN_ORE = registerBlock("tin_ore", () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_ORE).mapColor(MapColor.METAL).requiresCorrectToolForDrops()));

    public static final DeferredBlock<Block> DEEPSLATE_TIN_ORE = registerBlock("deepslate_tin_ore", () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_IRON_ORE).mapColor(MapColor.METAL).requiresCorrectToolForDrops()));

    public static final DeferredBlock<Block> RAW_TIN_BLOCK = registerBlock("raw_tin_block", () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.RAW_IRON_BLOCK).sound(SoundType.METAL).requiresCorrectToolForDrops()));

    public static final DeferredBlock<Block> TIN_BLOCK = registerBlock("tin_block", () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).sound(SoundType.METAL).requiresCorrectToolForDrops()));

    public static final DeferredBlock<Block> BRONZE_BLOCK = registerBlock("bronze_block", () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).sound(SoundType.METAL).requiresCorrectToolForDrops()));

    public static final DeferredBlock<Block> FORGED_STEEL_BLOCK = registerBlock("forged_steel_block", () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.NETHERITE_BLOCK).sound(SoundType.METAL).requiresCorrectToolForDrops()));

    public static final DeferredBlock<TransparentBlock> FRAGMENT_OF_BORDER = registerBlock("fragment_of_border", () -> new TransparentBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS).lightLevel(state -> 15).strength(10.0F).sound(SoundType.METAL).noOcclusion().noLootTable()), new Item.Properties().rarity(Rarity.RARE));

    public static final DeferredBlock<SyrupBlock> BASE_SYRUP = syrup("base_syrup");
    public static final DeferredBlock<SyrupBlock> STRAWBERRY_SYRUP = syrup("strawberry_syrup");
    public static final DeferredBlock<SyrupBlock> VANILLA_SYRUP = syrup("vanilla_syrup");
    public static final DeferredBlock<SyrupBlock> MINT_SYRUP = syrup("mint_syrup");
    public static final DeferredBlock<SyrupBlock> BANANA_SYRUP = syrup("banana_syrup");

    public static final DeferredBlock<JellyBlock> LUSH_CONFITURE_JELLY = registerBlock("lush_confiture_jelly", () -> new JellyBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.HONEY_BLOCK), "lush_confiture"));

    public static final DeferredBlock<JelloBlock> LUSH_CONFITURE_JELLO = registerBlock("lush_confiture_jello", () -> new JelloBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SLIME_BLOCK), "lush_confiture"));

    public static final DeferredBlock<JellyBottleBlock> LUSH_CONFITURE_JELLY_BOTTLE = BLOCKS.register("lush_confiture_jelly_bottle", () -> new JellyBottleBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS).strength(0.3F).noOcclusion().sound(SoundType.GLASS)));

    private ModBlocks() {
    }

    public static void register(IEventBus modEventBus) {
        ModItems.ITEMS.register("lush_confiture_jelly_bottle", () -> new JellyBottleItem(LUSH_CONFITURE_JELLY_BOTTLE.get(), new Item.Properties().food(new net.minecraft.world.food.FoodProperties.Builder().nutrition(1).saturationModifier(1.0F).build())));
        BLOCKS.register(modEventBus);
    }

    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block) {
        return registerBlock(name, block, new Item.Properties());
    }

    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block, Item.Properties itemProperties) {
        DeferredBlock<T> registeredBlock = BLOCKS.register(name, block);
        ModItems.ITEMS.register(name, () -> new BlockItem(registeredBlock.get(), itemProperties));
        return registeredBlock;
    }

    private static DeferredBlock<SyrupBlock> syrup(String name) {
        return registerBlock(name, () -> new SyrupBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.HONEY_BLOCK).instabreak().noOcclusion().sound(SoundType.HONEY_BLOCK)));
    }
}
