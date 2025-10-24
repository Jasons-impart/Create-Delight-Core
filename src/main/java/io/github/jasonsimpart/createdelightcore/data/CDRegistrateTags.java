package io.github.jasonsimpart.createdelightcore.data;

import com.simibubi.create.AllItems;
import com.simibubi.create.AllTags;
import com.simibubi.create.foundation.data.TagGen;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.providers.RegistrateTagsProvider;
import io.github.jasonsimpart.createdelightcore.registry.CDTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import static io.github.jasonsimpart.createdelightcore.CreateDelightCore.REGISTRATE;
import static io.github.jasonsimpart.createdelightcore.registry.CDTags.forgeItemTag;

public class CDRegistrateTags {
    public static void addGenerators() {
        REGISTRATE.addDataGenerator(ProviderType.BLOCK_TAGS, CDRegistrateTags::genBlockTags);
        REGISTRATE.addDataGenerator(ProviderType.ITEM_TAGS, CDRegistrateTags::genItemTags);
        //REGISTRATE.addDataGenerator(ProviderType.FLUID_TAGS, CDRegistrateTags::genFluidTags);
        //REGISTRATE.addDataGenerator(ProviderType.ENTITY_TAGS, CDRegistrateTags::genEntityTags);
    }

    private static void genItemTags(RegistrateTagsProvider<Item> provIn) {
        TagGen.CreateTagsProvider<Item> prov = new TagGen.CreateTagsProvider<>(provIn, Item::builtInRegistryHolder);

        prov.tag(CDTags.AllItemTags.FREEZABLE.tag)
                .add(AllItems.BLAZE_CAKE.get())
                .add(Items.ICE)
                .add(Items.PACKED_ICE)
                .add(Items.WATER_BUCKET)
                .add(Items.MAGMA_CREAM)
                .add(Items.SNOWBALL)
                .add(Items.SNOW)
                .add(Items.CRYING_OBSIDIAN)
        ;
    }

    private static void genBlockTags(RegistrateTagsProvider<Block> provIn) {
        TagGen.CreateTagsProvider<Block> prov = new TagGen.CreateTagsProvider<>(provIn, Block::builtInRegistryHolder);

        prov.tag(CDTags.AllBlockTags.FAN_PROCESSING_CATALYSTS_FREEZING.tag)
                .add(Blocks.POWDER_SNOW);

        prov.tag(AllTags.AllBlockTags.FAN_TRANSPARENT.tag)
                .addTag(CDTags.AllBlockTags.FAN_PROCESSING_CATALYSTS_FREEZING.tag);
    }
}
