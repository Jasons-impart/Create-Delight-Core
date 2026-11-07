// Adapted from Create More Recipes by iglee42 (MIT). See META-INF/licenses/create-more-recipes.txt.
package io.github.jasonsimpart.createdelightcore.mixin.create;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.content.processing.basin.BasinRecipe;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.recipe.HeatCondition;
import io.github.jasonsimpart.createdelightcore.content.cooler.CoolerHeat;
import io.github.jasonsimpart.createdelightcore.content.cooler.SnowmanCoolerBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = BasinRecipe.class, remap = false)
public abstract class SnowmanCoolingBasinMixin {
    @WrapOperation(method = "apply(Lcom/simibubi/create/content/processing/basin/BasinBlockEntity;Lnet/minecraft/world/item/crafting/Recipe;Z)Z",
            at = @At(value = "INVOKE", target = "Lcom/simibubi/create/content/processing/recipe/HeatCondition;testBlazeBurner(Lcom/simibubi/create/content/processing/burner/BlazeBurnerBlock$HeatLevel;)Z"))
    private static boolean createdelightcore$requireCooler(HeatCondition condition, BlazeBurnerBlock.HeatLevel heat,
            Operation<Boolean> original, @Local(argsOnly = true) BasinBlockEntity basin) {
        if (!CoolerHeat.isCooling(condition)) return original.call(condition, heat);
        if (basin.getLevel() == null) return false;
        var state = basin.getLevel().getBlockState(basin.getBlockPos().below());
        var needed = CoolerHeat.isFreezing(condition)
                ? SnowmanCoolerBlock.HeatLevel.FREEZING : SnowmanCoolerBlock.HeatLevel.COOLING;
        return state.getBlock() instanceof SnowmanCoolerBlock
                && SnowmanCoolerBlock.getHeatLevelOf(state).isAtLeast(needed);
    }
}
