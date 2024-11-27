package io.github.jasonsimpart.createdelightcore.recipe;

import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder;
import io.github.jasonsimpart.createdelightcore.registry.CDRecipeTypes;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.RecipeWrapper;

public class BatchFreezingRecipe extends ProcessingRecipe<BatchFreezingRecipe.BatchFreezingRecipeWrapper> {
    public BatchFreezingRecipe(ProcessingRecipeBuilder.ProcessingRecipeParams params) {
        super(CDRecipeTypes.BATCH_FREEZING, params);
    }

    @Override
    public boolean matches(BatchFreezingRecipeWrapper inv, Level worldIn) {
        if (inv.isEmpty())
            return false;
        return ingredients.get(0)
                .test(inv.getItem(0));
    }

    @Override
    protected int getMaxInputCount() {
        return 1;
    }

    @Override
    protected int getMaxOutputCount() {
        return 12;
    }

    public static class BatchFreezingRecipeWrapper extends RecipeWrapper {
        public BatchFreezingRecipeWrapper() {
            super(new ItemStackHandler(1));
        }
    }
}
