package io.github.jasonsimpart.createdelightcore.content.recipe;

import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder;
import io.github.jasonsimpart.createdelightcore.registry.CDRecipeTypes;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.RecipeWrapper;

public class FanFreezingRecipe extends ProcessingRecipe<FanFreezingRecipe.FanFreezingRecipeWrapper> {
    public FanFreezingRecipe(ProcessingRecipeBuilder.ProcessingRecipeParams params) {
        super(CDRecipeTypes.FAN_FREEZING, params);
    }

    @Override
    public boolean matches(FanFreezingRecipeWrapper inv, Level worldIn) {
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

    public static class FanFreezingRecipeWrapper extends RecipeWrapper {
        public FanFreezingRecipeWrapper() {
            super(new ItemStackHandler(1));
        }
    }
}
