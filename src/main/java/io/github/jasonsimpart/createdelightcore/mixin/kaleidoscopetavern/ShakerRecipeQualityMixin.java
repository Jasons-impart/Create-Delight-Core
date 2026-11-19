package io.github.jasonsimpart.createdelightcore.mixin.kaleidoscopetavern;

import com.github.ysbbbbbb.kaleidoscopetavern.crafting.recipe.ShakerRecipe;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.createdelight.qualityfoodfluids.api.QualityFoodFluidsCreateRules;
import io.github.jasonsimpart.createdelightcore.compat.kaleidoscopetavern.TavernQuality;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ArrayList;
import java.util.List;

@Pseudo
@Mixin(value = ShakerRecipe.class, remap = false)
public abstract class ShakerRecipeQualityMixin {
    @ModifyReturnValue(method = "assemble(Lnet/minecraft/world/SimpleContainer;Lnet/minecraft/core/RegistryAccess;)Lnet/minecraft/world/item/ItemStack;",
            at = @At("RETURN"))
    private ItemStack createdelightcore$qualityFromActualIngredients(ItemStack result, SimpleContainer input,
                                                                    RegistryAccess registries) {
        if (!TavernQuality.supportsQuality(result)) return result;
        List<ItemStack> ingredients = new ArrayList<>();
        for (int slot = 0; slot < input.getContainerSize(); slot++) {
            var ingredient = input.getItem(slot);
            if (!ingredient.isEmpty()) ingredients.add(ingredient.copy());
        }
        return TavernQuality.qualify(result, QualityFoodFluidsCreateRules.rollBasinQuality(ingredients, List.of()));
    }
}
