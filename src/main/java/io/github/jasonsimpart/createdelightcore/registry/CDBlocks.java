package io.github.jasonsimpart.createdelightcore.registry;

import com.github.alexthe666.iceandfire.block.IafBlockRegistry;
import com.simibubi.create.Create;
import com.simibubi.create.content.decoration.encasing.CasingBlock;
import com.simibubi.create.foundation.block.connected.SimpleCTBehaviour;
import com.simibubi.create.foundation.data.BuilderTransformers;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.providers.loot.RegistrateBlockLootTables;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.entry.ItemEntry;
import io.github.jasonsimpart.createdelightcore.AllSpriteShifts;
import io.github.jasonsimpart.createdelightcore.content.block.CoinPileBlock;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraftforge.common.Tags;

import static io.github.jasonsimpart.createdelightcore.registry.CDRegistration.REGISTRATE;
import static io.github.jasonsimpart.createdelightcore.registry.CDTags.forgeBlockTag;
import static io.github.jasonsimpart.createdelightcore.registry.CDTags.forgeItemTag;

public class CDBlocks {
    public static final ResourceKey<CreativeModeTab> MISC_TAB = CDCreativeTabs.MISC.getKey();
    public static final ResourceKey<CreativeModeTab> COIN_TAB = CDCreativeTabs.COIN.getKey();
    //electrum
    public static final BlockEntry<Block> ELECTRUM;
    //tin
        //ore
        public static final BlockEntry<Block> TIN_ORE;
        public static final BlockEntry<Block> DEEPSLATE_TIN_ORE;
        //metal
        public static final BlockEntry<Block> RAW_TIN;
        public static final BlockEntry<Block> TIN;
    //bronze
    public static final BlockEntry<Block> BRONZE;
    //forged_steel
    public static final BlockEntry<Block> FORGED_STEEL;
    //fragment_of_border
    public static final BlockEntry<GlassBlock> FRAGMENT_OF_BORDER;
    //coin_pile
    public static final BlockEntry<CoinPileBlock> IRON;
    public static final BlockEntry<CoinPileBlock> COPPER;
    public static final BlockEntry<CoinPileBlock> GOLD;
    public static final BlockEntry<CoinPileBlock> EMERALD;
    public static final BlockEntry<CoinPileBlock> NETHERITE;

    static {
        //electrum
        ELECTRUM = simpleMetalBlock("electrum", BlockTags.NEEDS_IRON_TOOL);
        //tin
            //ore
            TIN_ORE = simpleOre("tin", BlockTags.NEEDS_IRON_TOOL, CDItems.RAW_TIN);
            DEEPSLATE_TIN_ORE = simpleDeepslateOre("tin", BlockTags.NEEDS_IRON_TOOL, CDItems.RAW_TIN);
            //metal
            RAW_TIN = simpleRawMetalBlock("tin", BlockTags.NEEDS_IRON_TOOL);
            TIN = simpleMetalBlock("tin", BlockTags.NEEDS_IRON_TOOL);
        //bronze
        BRONZE = simpleMetalBlock("bronze", BlockTags.NEEDS_IRON_TOOL);
        //forged_steel
        FORGED_STEEL = simpleMetalBlock("forged_steel", BlockTags.NEEDS_DIAMOND_TOOL);
        //fargment_of_border
        //noinspection removal
        FRAGMENT_OF_BORDER = REGISTRATE.block("fragment_of_border", GlassBlock::new)
                .item()
                .properties(p -> p.rarity(Rarity.RARE))
                .tab(MISC_TAB)
                .build()
                .initialProperties(() -> Blocks.GLASS)
                .properties(p -> p
                        .lightLevel(bs -> 15)
                        .strength(10.0F)
                        .sound(SoundType.METAL)
                        .noLootTable()
                )
                .addLayer(() -> RenderType::translucent)
                .tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .register();
        //coin_pile
        IRON = coinPileBlock("iron", Rarity.COMMON);
        COPPER = coinPileBlock("copper", Rarity.UNCOMMON);
        GOLD = coinPileBlock("gold", Rarity.RARE);
        EMERALD = coinPileBlock("emerald", Rarity.RARE);
        NETHERITE = coinPileBlock("netherite", Rarity.EPIC);
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
                .properties(properties -> properties.rarity(Rarity.COMMON))
                .tag(Tags.Items.ORES, forgeItemTag("ores/deepslate" + metalName),
                        forgeItemTag("ores_in_ground/deepslate"))
                .tab(MISC_TAB)
                .build()
                .initialProperties(() -> Blocks.DEEPSLATE_GOLD_ORE)
                .properties(p -> p.mapColor(MapColor.METAL)
                        .requiresCorrectToolForDrops()
                        .sound(SoundType.DEEPSLATE)
                )
                .loot((lt, b) -> lt.add(b,
                        RegistrateBlockLootTables.createSilkTouchDispatchTable(b,
                                lt.applyExplosionDecay(b, LootItem.lootTableItem(dropItem.get())
                                        .apply(ApplyBonusCount.addOreBonusCount(Enchantments.BLOCK_FORTUNE))))))
                .tag(BlockTags.MINEABLE_WITH_PICKAXE, pickaxeLevel)
                .tag(Tags.Blocks.ORES, forgeBlockTag("ores/deepslate" + metalName),
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

    //TODO: 修改为注册方块的轮子
    public static final BlockEntry<CasingBlock> STEEL_CASING = REGISTRATE.block("steel_casing", CasingBlock::new)
            .properties(p -> p.mapColor(MapColor.PODZOL))
            .onRegister(CreateRegistrate.connectedTextures(() -> new SimpleCTBehaviour(AllSpriteShifts.STEEL_CASING)))
            .item()
            .build()
            .register();
    public static void init() {
    }
}
