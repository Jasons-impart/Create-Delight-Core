package io.github.jasonsimpart.createdelightcore.mixin.kaleidoscopetavern;

import com.github.ysbbbbbb.kaleidoscopetavern.item.ShakerItem;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.createdelight.qualityfoodfluids.api.QualityFoodFluidsCreateRules;
import io.github.jasonsimpart.createdelightcore.compat.kaleidoscopetavern.TavernQuality;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ArrayList;
import java.util.List;

/** Timing and missing-recipe results bypass ShakerRecipe.assemble; serialization must never reroll. */
@Pseudo
@Mixin(value = ShakerItem.class, remap = false)
public abstract class ShakerFallbackQualityMixin {
    @WrapOperation(method = "releaseUsing", remap = true, at = @At(value = "INVOKE",
            target = "Lcom/github/ysbbbbbb/kaleidoscopetavern/item/ShakerItem;setResult(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)V",
            remap = false))
    private void createdelightcore$qualifyTimingResult(ItemStack shaker, ItemStack result, Operation<Void> original) {
        original.call(shaker, createdelightcore$qualifyFallback(shaker, result));
    }

    @WrapOperation(method = "lambda$handRecipe$1", at = @At(value = "INVOKE",
            target = "Lcom/github/ysbbbbbb/kaleidoscopetavern/item/ShakerItem;setResult(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)V"))
    private static void createdelightcore$qualifyMissingRecipeResult(ItemStack shaker, ItemStack result, Operation<Void> original) {
        original.call(shaker, createdelightcore$qualifyFallback(shaker, result));
    }

    @Unique
    private static ItemStack createdelightcore$qualifyFallback(ItemStack shaker, ItemStack result) {
        var storage = ShakerItem.getStorage(shaker);
        List<ItemStack> ingredients = new ArrayList<>();
        for (int slot = 0; slot < storage.getSlots(); slot++) {
            var stack = storage.getStackInSlot(slot);
            if (!stack.isEmpty()) ingredients.add(stack.copy());
        }
        return TavernQuality.qualify(result, QualityFoodFluidsCreateRules.rollBasinQuality(ingredients, List.of()));
    }
}
