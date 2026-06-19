package io.github.jasonsimpart.content.recipe;

import com.simibubi.create.content.processing.recipe.ProcessingRecipeParams;
import com.simibubi.create.content.processing.recipe.StandardProcessingRecipe;
import io.github.jasonsimpart.registry.ModRecipeTypes;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;

public class FanFreezingRecipe extends StandardProcessingRecipe<SingleRecipeInput> {
    public FanFreezingRecipe(ProcessingRecipeParams params) {
        super(ModRecipeTypes.FAN_FREEZING, params);
    }

    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
        if (input.isEmpty()) {
            return false;
        }
        return ingredients.getFirst().test(input.getItem(0));
    }

    @Override
    protected int getMaxInputCount() {
        return 1;
    }

    @Override
    protected int getMaxOutputCount() {
        return 12;
    }
}
