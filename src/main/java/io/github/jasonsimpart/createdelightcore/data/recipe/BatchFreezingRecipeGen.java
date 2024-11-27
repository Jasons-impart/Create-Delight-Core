package io.github.jasonsimpart.createdelightcore.data.recipe;

import com.simibubi.create.AllItems;
import io.github.jasonsimpart.createdelightcore.registry.CDRecipeTypes;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Items;

public class BatchFreezingRecipeGen extends CDProcessingRecipeGen {
    GeneratedRecipe
            BLAZE_CAKE = secondaryRecipe(AllItems.BLAZE_CAKE::get, AllItems.POWDERED_OBSIDIAN::get, AllItems.CINDER_FLOUR::get, .90f),
            PACKED_ICE = convert(Items.ICE, Items.PACKED_ICE),
            BLUE_ICE = convert(Items.PACKED_ICE, Items.BLUE_ICE),
            POWDER_SNOW_BUCKET = convert(Items.WATER_BUCKET, Items.POWDER_SNOW_BUCKET),
            SLIME_BALL = convert(Items.MAGMA_CREAM, Items.SLIME_BALL),
            SNOW = convert(Items.SNOWBALL, Items.SNOW),
            SNOW_BLOCK = convert(Items.SNOW, Items.SNOW_BLOCK),
            OBSIDIAN = convert(Items.CRYING_OBSIDIAN, Items.OBSIDIAN);

    public BatchFreezingRecipeGen(PackOutput output) {
        super(output);
    }

    @Override
    protected CDRecipeTypes getRecipeType() {
        return CDRecipeTypes.BATCH_FREEZING;
    }
}
