package io.github.jasonsimpart.createdelightcore.registry;

import com.github.alexthe666.iceandfire.block.IafBlockRegistry;
import com.tterrag.registrate.util.entry.BlockEntry;
import io.github.jasonsimpart.createdelightcore.content.block.CoinPileBlock;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;

import static io.github.jasonsimpart.createdelightcore.registry.CDRegistration.REGISTRATE;
import static io.github.jasonsimpart.createdelightcore.registry.CDTags.forgeBlockTag;
import static io.github.jasonsimpart.createdelightcore.registry.CDTags.forgeItemTag;

public class CDBlocks {
    public static final ResourceKey<CreativeModeTab> MISC_TAB = CDCreativeTabs.MISC.getKey();
    public static final ResourceKey<CreativeModeTab> COIN_TAB = CDCreativeTabs.COIN.getKey();
    //electrum
    public static final BlockEntry<Block> ELECTRUM;
    //tin
    public static final BlockEntry<Block> TIN_ORE;
    public static final BlockEntry<Block> DEEPSLATE_TIN_ORE;
    public static final BlockEntry<Block> RAW_TIN;
    public static final BlockEntry<Block> TIN;
    //bronze
    public static final BlockEntry<Block> BRONZE;
    //fragment_of_border
    public static final BlockEntry<GlassBlock> FRAGMENT_OF_BORDER;
    //coin_pile
    public static final BlockEntry<CoinPileBlock> IRON;
    public static final BlockEntry<CoinPileBlock> COPPER;
    public static final BlockEntry<CoinPileBlock> GOLD;
    public static final BlockEntry<CoinPileBlock> EMERALD;
    public static final BlockEntry<CoinPileBlock> NETHERITE;

    static {
        //metal
        ELECTRUM = simpleMetalBlock("electrum", BlockTags.NEEDS_IRON_TOOL);
        TIN_ORE = simpleOre("tin", BlockTags.NEEDS_IRON_TOOL);
        DEEPSLATE_TIN_ORE = simpleDeepslateOre("tin", BlockTags.NEEDS_DIAMOND_TOOL);
        RAW_TIN = simpleRawMetalBlock("tin", BlockTags.NEEDS_IRON_TOOL);
        TIN = simpleMetalBlock("tin", BlockTags.NEEDS_IRON_TOOL);
        BRONZE = simpleMetalBlock("bronze", BlockTags.NEEDS_IRON_TOOL);
        //coin_pile
        IRON = coinPileBlock("iron", Rarity.COMMON);
        COPPER = coinPileBlock("copper", Rarity.UNCOMMON);
        GOLD = coinPileBlock("gold", Rarity.RARE);
        EMERALD = coinPileBlock("emerald", Rarity.RARE);
        NETHERITE = coinPileBlock("netherite", Rarity.EPIC);
        //fargment_of_border
        //noinspection removal
        FRAGMENT_OF_BORDER = REGISTRATE.block("fragment_of_border", GlassBlock::new)
                .item()
                .properties(properties -> properties.rarity(Rarity.RARE))
                .tab(MISC_TAB)
                .build()
                .properties(properties -> BlockBehaviour.Properties.copy(Blocks.GLASS)
                        .lightLevel(blockState -> 15)
                        .strength(10.0F)
                        .sound(SoundType.METAL)
                        .noLootTable()
                        .requiresCorrectToolForDrops()
                )
                .addLayer(() -> RenderType::translucent)
                .tag(BlockTags.NEEDS_IRON_TOOL)
                .tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .register();
    }

    public static BlockEntry<Block> simpleMetalBlock(String metalName, TagKey<Block> pickaxeLevel) {
        return REGISTRATE.block(metalName + "_block", Block::new)
                .item()
                .properties(properties -> properties.rarity(Rarity.COMMON))
                .tag(forgeItemTag("storage_blocks"), forgeItemTag("storage_blocks/" + metalName))
                .tab(MISC_TAB)
                .build()
                .properties(properties -> properties
                        .strength(5.0F, 6.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                )
                .tag(BlockTags.MINEABLE_WITH_PICKAXE, pickaxeLevel)
                .tag(forgeBlockTag("storage_blocks") ,forgeBlockTag("storage_blocks/" + metalName))
                .register();
    }

    public static BlockEntry<Block> simpleRawMetalBlock(String metalName, TagKey<Block> pickaxeLevel) {
        return REGISTRATE.block("raw_" + metalName + "_block", Block::new)
                .item()
                .properties(properties -> properties.rarity(Rarity.COMMON))
                .tag(forgeItemTag("storage_blocks"), forgeItemTag("storage_blocks/" + "raw_" + metalName))
                .tab(MISC_TAB)
                .build()
                .properties(properties -> properties
                        .strength(5.0F, 6.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                )
                .tag(BlockTags.MINEABLE_WITH_PICKAXE, pickaxeLevel)
                .tag(forgeBlockTag("storage_blocks") ,forgeBlockTag("storage_blocks/" + "raw_" + metalName))
                .register();
    }

    public static BlockEntry<Block> simpleOre(String metalName, TagKey<Block> pickaxeLevel) {
        return REGISTRATE.block(metalName + "_ore", Block::new)
                .item()
                .properties(properties -> properties.rarity(Rarity.COMMON))
                .tag(forgeItemTag("ores"), forgeItemTag("ores/" + metalName), forgeItemTag("ores_in_ground/stone"))
                .tab(MISC_TAB)
                .build()
                .properties(properties -> properties
                        .strength(3.0F, 3.0F)
                        .sound(SoundType.STONE)
                        .requiresCorrectToolForDrops()
                )
                .tag(BlockTags.MINEABLE_WITH_PICKAXE, pickaxeLevel)
                .tag(forgeBlockTag("ores"), forgeBlockTag("ores/" + metalName), forgeBlockTag("ores_in_ground/stone"))
                .register();
    }

    public static BlockEntry<Block> simpleDeepslateOre(String metalName, TagKey<Block> pickaxeLevel) {
        return REGISTRATE.block("deepslate_" + metalName + "_ore", Block::new)
                .item()
                .properties(properties -> properties.rarity(Rarity.COMMON))
                .tag(forgeItemTag("ores"), forgeItemTag("ores/" + "deepslate" + metalName), forgeItemTag("ores_in_ground/deepslate"))
                .tab(MISC_TAB)
                .build()
                .properties(properties -> properties
                        .strength(4.5F, 3.0F)
                        .sound(SoundType.DEEPSLATE)
                        .requiresCorrectToolForDrops()
                )
                .tag(BlockTags.MINEABLE_WITH_PICKAXE, pickaxeLevel)
                .tag(forgeBlockTag("ores"), forgeBlockTag("ores/" + "deepslate" + metalName), forgeBlockTag("ores_in_ground/deepslate"))
                .register();
    }

    public static BlockEntry<Block> simpleBlock(String name, float destoryTime, float resistance, SoundType soundType,TagKey<Block> tool, TagKey<Block> toolLevel) {
        return REGISTRATE.block(name + "_block", Block::new)
                .item()
                .properties(properties -> properties.rarity(Rarity.COMMON))
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

    public static BlockEntry<CoinPileBlock> coinPileBlock(String coinTtier, Rarity rarity) {
        return REGISTRATE.block(coinTtier + "_coin_pile", CoinPileBlock::new)
                .item()
                .properties(properties -> properties
                        .fireResistant()
                        .rarity(rarity))
                .build()
                .properties(properties -> properties
                        .strength(0.3F)
                        .sound(IafBlockRegistry.SOUND_TYPE_GOLD)
                        .forceSolidOff()
                        .randomTicks()
                        .pushReaction(PushReaction.DESTROY)
                )
                .register();
    }

    public static void init() {
    }
}
