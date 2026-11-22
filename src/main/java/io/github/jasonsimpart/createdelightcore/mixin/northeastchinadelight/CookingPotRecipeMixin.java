package io.github.jasonsimpart.createdelightcore.mixin.northeastchinadelight;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.jasonsimpart.createdelightcore.compat.northeastchinadelight.SeasoningBottleCompat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import vectorwing.farmersdelight.common.crafting.CookingPotRecipe;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

@Mixin(value = CookingPotRecipe.class, remap = false)
public abstract class CookingPotRecipeMixin {
    @WrapOperation(method = "matches(Lnet/minecraftforge/items/wrapper/RecipeWrapper;Lnet/minecraft/world/level/Level;)Z",
            at = @At(value = "INVOKE", target = "Lnet/minecraftforge/common/util/RecipeMatcher;findMatches(Ljava/util/List;Ljava/util/List;)[I"))
    private int[] createdelightcore$matchSeasonings(List<ItemStack> inputs, List<? extends Predicate<ItemStack>> ingredients,
                                                    Operation<int[]> original, @Local(argsOnly = true) Level level) {
        if (inputs.stream().noneMatch(SeasoningBottleCompat::isBottle)) {
            return original.call(inputs, ingredients);
        }
        ItemStack result = ((CookingPotRecipe) (Object) this).getResultItem(level.registryAccess());
        List<ItemStack> projected = new ArrayList<>(inputs.size());
        for (ItemStack input : inputs) {
            ItemStack ingredient = SeasoningBottleCompat.asIngredient(input);
            if (SeasoningBottleCompat.isBottle(input) && (ingredient.isEmpty()
                    || !SeasoningBottleCompat.hasAmount(input, SeasoningBottleCompat.cookingPotAmount(ingredient, result)))) {
                return null;
            }
            projected.add(ingredient);
        }
        return original.call(projected, ingredients);
    }
}
