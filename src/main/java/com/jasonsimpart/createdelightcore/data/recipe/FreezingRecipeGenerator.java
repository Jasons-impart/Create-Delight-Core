package com.jasonsimpart.createdelightcore.data.recipe;

import com.jasonsimpart.createdelightcore.registry.CDItems;
import com.simibubi.create.foundation.data.recipe.ProcessingRecipeGen;
import com.simibubi.create.foundation.recipe.IRecipeTypeInfo;
import net.minecraft.data.DataGenerator;
import org.forsteri.ratatouille.entry.CRRecipeTypes;

public class FreezingRecipeGenerator extends ProcessingRecipeGen {
    public FreezingRecipeGenerator(DataGenerator generator) {
        super(generator);
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
