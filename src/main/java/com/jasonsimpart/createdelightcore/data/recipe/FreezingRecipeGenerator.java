package com.jasonsimpart.createdelightcore.data.recipe;

import com.jasonsimpart.createdelightcore.register.CDItems;
import com.simibubi.create.foundation.recipe.IRecipeTypeInfo;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.forsteri.ratatouille.data.recipe.ProcessingRecipeGen;
import org.forsteri.ratatouille.data.recipe.RataouilleRecipeProvider;
import org.forsteri.ratatouille.entry.CRRecipeTypes;

public class FreezingRecipeGenerator extends ProcessingRecipeGen {
    public FreezingRecipeGenerator(PackOutput packOutput) {
        super(packOutput);
        create(CDItems.BLACK_CHOCOLATE_MOLD_SOLID.getId(), transform ->
                transform.require(CDItems.BLACK_CHOCOLATE_MOLD_FILLED.get())
                        .output(CDItems.BLACK_CHOCOLATE_MOLD_SOLID.get()));
        create(CDItems.WHITE_CHOCOLATE_MOLD_SOLID.getId(), transform ->
                transform.require(CDItems.WHITE_CHOCOLATE_MOLD_FILLED.get())
                        .output(CDItems.WHITE_CHOCOLATE_MOLD_SOLID.get()));
        create(CDItems.RUBY_CHOCOLATE_MOLD_SOLID.getId(), transform ->
                transform.require(CDItems.RUBY_CHOCOLATE_MOLD_FILLED.get())
                        .output(CDItems.RUBY_CHOCOLATE_MOLD_SOLID.get()));
    }

    protected IRecipeTypeInfo getRecipeType() {
        return CRRecipeTypes.FREEZING;
    }
}
