package io.github.jasonsimpart.createdelightcore.registry;

import com.github.alexthe666.iceandfire.block.IafBlockRegistry;
import com.github.alexthe666.iceandfire.misc.IafSoundRegistry;
import com.tterrag.registrate.util.entry.BlockEntry;
import io.github.jasonsimpart.createdelightcore.content.block.CDCCoinPile;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;

import static io.github.jasonsimpart.createdelightcore.registry.CDRegistration.REGISTRATE;

public class CDBlocks {
    public static final ResourceKey<CreativeModeTab> MISC_TAB = CDCreativeTabs.MISC.getKey();
    public static final ResourceKey<CreativeModeTab> COIN_TAB = CDCreativeTabs.COIN.getKey();
    //block
    public static final BlockEntry<GlassBlock> FRAGMENT_OF_BORDER;
    //coin_pile
    public static final BlockEntry<CDCCoinPile> IRON_COIN_PILE;
    public static final BlockEntry<CDCCoinPile> COPPER_COIN_PILE;
    public static final BlockEntry<CDCCoinPile> GOLD_COIN_PILE;
    public static final BlockEntry<CDCCoinPile> EMERALD_COIN_PILE;
    public static final BlockEntry<CDCCoinPile> NETHERITE_COIN_PILE;

    static {
        //coin_pile
        IRON_COIN_PILE = coinPileBlock("iron_coin_pile", Rarity.COMMON);
        COPPER_COIN_PILE = coinPileBlock("copper_coin_pile", Rarity.UNCOMMON);
        GOLD_COIN_PILE = coinPileBlock("gold_coin_pile", Rarity.RARE);
        EMERALD_COIN_PILE = coinPileBlock("emerald_coin_pile", Rarity.RARE);
        NETHERITE_COIN_PILE = coinPileBlock("netherite_coin_pile", Rarity.EPIC);
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
                )
                .addLayer(() -> RenderType::translucent)
                .tag(BlockTags.NEEDS_IRON_TOOL)
                .tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .register();
    }


    public static BlockEntry<Block> simpleBlock(String name, ResourceKey<CreativeModeTab> tab, Rarity rarity, float hardness, float resistance) {
        return REGISTRATE.block(name, Block::new)
                .item()
                .properties(properties -> properties.rarity(rarity))
                .tab(tab)
                .build()
                .properties(properties -> properties
                        .strength(2.0F)
                        .explosionResistance(resistance)
                        .destroyTime(hardness)
                )
                .register();
    }

    public static BlockEntry<CDCCoinPile> coinPileBlock(String name, Rarity rarity) {
        return REGISTRATE.block(name, CDCCoinPile::new)
                .item()
                .properties(properties -> properties
                        .fireResistant()
                        .rarity(rarity))
                .tab(COIN_TAB)
                .build()
                .properties(properties -> properties
                        .strength(5.0F)
                        .sound(IafBlockRegistry.SOUND_TYPE_GOLD)
                        .pushReaction(PushReaction.DESTROY)
                )
                .register();
    }

    public static void init() {
    }
}
