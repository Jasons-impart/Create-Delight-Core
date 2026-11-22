package io.github.jasonsimpart.createdelightcore.mixin.kaleidoscopetavern;

import com.github.ysbbbbbb.kaleidoscopetavern.crafting.recipe.BarrelRecipe;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.jasonsimpart.createdelightcore.compat.kaleidoscopetavern.TavernBarrelRecipes;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.List;

@Pseudo
@Mixin(targets = "com.github.ysbbbbbb.kaleidoscopetavern.compat.jei.category.BarrelRecipeCategory", remap = false)
public abstract class FixedBarrelJeiMixin {
    @ModifyArg(
            method = "setRecipe(Lmezz/jei/api/gui/builder/IRecipeLayoutBuilder;Lcom/github/ysbbbbbb/kaleidoscopetavern/crafting/recipe/BarrelRecipe;Lmezz/jei/api/recipe/IFocusGroup;)V",
            at = @At(value = "INVOKE", target = "Lmezz/jei/api/gui/builder/IRecipeSlotBuilder;addIngredients(Lmezz/jei/api/ingredients/IIngredientType;Ljava/util/List;)Lmezz/jei/api/gui/builder/IIngredientAcceptor;"),
            index = 1)
    private List<ItemStack> createdelightcore$showFixedInputCounts(List<ItemStack> stacks,
                                                                    @Local(argsOnly = true) BarrelRecipe recipe,
                                                                    @Local Ingredient ingredient) {
        var batch = TavernBarrelRecipes.fixedBatch(recipe);
        if (batch == null || stacks.isEmpty()) return stacks;
        var inputs = recipe.ingredients().stream().filter(input -> !input.isEmpty()).toList();
        int index = inputs.indexOf(ingredient);
        if (index < 0 || index >= batch.ingredientCounts().size()) return stacks;
        int count = batch.ingredientCounts().get(index);
        return stacks.stream().map(stack -> stack.copyWithCount(count)).toList();
    }

    @ModifyArg(
            method = "setRecipe(Lmezz/jei/api/gui/builder/IRecipeLayoutBuilder;Lcom/github/ysbbbbbb/kaleidoscopetavern/crafting/recipe/BarrelRecipe;Lmezz/jei/api/recipe/IFocusGroup;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;copyWithCount(I)Lnet/minecraft/world/item/ItemStack;", remap = true),
            index = 0)
    private int createdelightcore$showFixedOutputCount(int nativeCount, @Local(argsOnly = true) BarrelRecipe recipe) {
        return TavernBarrelRecipes.fixedBatch(recipe) == null ? nativeCount : recipe.result().getCount();
    }
}
