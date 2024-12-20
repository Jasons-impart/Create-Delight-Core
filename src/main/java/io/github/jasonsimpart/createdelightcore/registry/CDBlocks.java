package io.github.jasonsimpart.createdelightcore.registry;

import com.simibubi.create.AllBlocks;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;

import static io.github.jasonsimpart.createdelightcore.registry.CDRegistration.REGISTRATE;

public class CDBlocks extends AllBlocks {
    public static final ResourceKey<CreativeModeTab> MISC_TAB = CDCreativeTabs.MISC.getKey();
    //block
    public static final BlockEntry<Block> FRAGMENT_OF_BORDER;


    static {
        FRAGMENT_OF_BORDER = REGISTRATE.block("fragment_of_border", Block::new)
                .item()
                .properties(properties -> properties.rarity(Rarity.RARE))
                .tab(MISC_TAB)
                .build()
                .properties(properties -> properties
                        .lightLevel(blockState -> 15)
                        .strength(10.0F)
                        .sound(SoundType.METAL)
                        .noLootTable()
                )
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

    public static void init() {
    }
}
