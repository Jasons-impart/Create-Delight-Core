package io.github.jasonsimpart.createdelightcore.data.recipe;

import com.simibubi.create.AllItems;
import io.github.jasonsimpart.createdelightcore.registry.CDItems;
import io.github.jasonsimpart.createdelightcore.registry.CDRecipeTypes;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Items;
import org.forsteri.ratatouille.entry.CRItems;

public class FanFreezingRecipeGen extends CDProcessingRecipeGen {
    GeneratedRecipe
            BLAZE_CAKE = secondaryRecipe(AllItems.BLAZE_CAKE::get, AllItems.POWDERED_OBSIDIAN::get, AllItems.CINDER_FLOUR::get, .90f),
            PACKED_ICE = convert(Items.ICE, Items.PACKED_ICE),
            BLUE_ICE = convert(Items.PACKED_ICE, Items.BLUE_ICE),
            POWDER_SNOW_BUCKET = convert(Items.WATER_BUCKET, Items.POWDER_SNOW_BUCKET),
            SLIME_BALL = convert(Items.MAGMA_CREAM, Items.SLIME_BALL),
            SNOW = convert(Items.SNOWBALL, Items.SNOW),
            SNOW_BLOCK = convert(Items.SNOW, Items.SNOW_BLOCK),
            OBSIDIAN = convert(Items.CRYING_OBSIDIAN, Items.OBSIDIAN),
            CHOCOLATE = convert(CRItems.CHOCOLATE_MOLD_FILLED.get(), CRItems.CHOCOLATE_MOLD_SOLID.get()),
            BLACK_CHOCOLATE = convert(CDItems.BLACK_CHOCOLATE_MOLD_FILLED.get(), CDItems.BLACK_CHOCOLATE_MOLD_SOLID.get()),
            WHITE_CHOCOLATE = convert(CDItems.WHITE_CHOCOLATE_MOLD_FILLED.get(), CDItems.WHITE_CHOCOLATE_MOLD_SOLID.get()),
            RUBY_CHOCOLATE = convert(CDItems.RUBY_CHOCOLATE_MOLD_FILLED.get(), CDItems.RUBY_CHOCOLATE_MOLD_SOLID.get());

    public FanFreezingRecipeGen(PackOutput output) {
        super(output);
    }

    @Override
    protected CDRecipeTypes getRecipeType() {
        return CDRecipeTypes.FAN_FREEZING;
    }
}
