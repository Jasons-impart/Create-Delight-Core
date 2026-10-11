package io.github.jasonsimpart.createdelightcore.mixin.create;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.kinetics.crafter.RecipeGridHandler;
import io.github.jasonsimpart.createdelightcore.content.util.StorageRecipeQuality;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;
import java.util.function.Function;

@Mixin(value = RecipeGridHandler.class, remap = false)
public abstract class RecipeGridHandlerQualityMixin {
    // Both vanilla crafting and the mechanical-crafting fallback use Optional.map.
    @WrapOperation(method = "tryToApplyRecipe", at = @At(value = "INVOKE",
            target = "Ljava/util/Optional;map(Ljava/util/function/Function;)Ljava/util/Optional;"),
            require = 2, expect = 2)
    private static Optional<ItemStack> createdelightcore$convertResult(
            Optional<Recipe<?>> recipes, Function<Recipe<?>, ItemStack> mapper,
            Operation<Optional<ItemStack>> original, @Local CraftingContainer inputs,
            @Local(argsOnly = true) Level level) {
        return original.call(recipes, (Function<Recipe<?>, ItemStack>) recipe ->
                StorageRecipeQuality.convert(mapper.apply(recipe), inputs, recipe, level));
    }
}
