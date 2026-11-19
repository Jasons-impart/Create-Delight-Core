package io.github.jasonsimpart.createdelightcore.mixin.dungeonsdelight;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.createdelight.qualityfoodfluids.api.QualityFoodFluidsApi;
import io.github.createdelight.qualityfoodfluids.api.QualityFoodFluidsCreateRules;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.yirmiri.dungeonsdelight.common.block.entity.MonsterPotBlockEntity;
import net.yirmiri.dungeonsdelight.common.block.entity.container.MonsterPotRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;

/** One litre of wort represents four original 250 mB servings, in a single real bucket. */
@Mixin(value = MonsterPotBlockEntity.class, remap = false)
public abstract class MonsterPotWortMixin {
    private static boolean createdelightcore$isWort(MonsterPotRecipe recipe) {
        return recipe.getId().equals(new ResourceLocation("createdelight", "alcohol_preparation/ghastly_spirits"));
    }

    @Inject(method = "canCook", at = @At("HEAD"), cancellable = true)
    private void createdelightcore$requireFullBucketIngredients(MonsterPotRecipe recipe, CallbackInfoReturnable<Boolean> cir) {
        if (!createdelightcore$isWort(recipe)) return;
        var pot = (MonsterPotBlockEntity) (Object) this;
        var inventory = pot.getInventory();
        for (int slot = 0; slot < 6; slot++) {
            if (inventory.getStackInSlot(slot).getCount() < 4) {
                cir.setReturnValue(false);
                return;
            }
        }
        // Buckets cannot stack. Require an empty destination and the real empty bucket before spending food.
        if (!inventory.getStackInSlot(6).isEmpty() || !inventory.getStackInSlot(8).isEmpty()
                || !ItemStack.isSameItemSameTags(inventory.getStackInSlot(7), recipe.getOutputContainer())) {
            cir.setReturnValue(false);
        }
    }

    @WrapOperation(method = "processCooking", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V", remap = true))
    private void createdelightcore$consumeFourServings(ItemStack ingredient, int count, Operation<Void> original,
                                                       @Local(argsOnly = true) MonsterPotRecipe recipe) {
        original.call(ingredient, createdelightcore$isWort(recipe) ? count * 4 : count);
    }

    @WrapOperation(method = "processCooking", at = @At(value = "INVOKE",
            target = "Lnet/yirmiri/dungeonsdelight/common/block/entity/container/MonsterPotRecipe;getResultItem(Lnet/minecraft/core/RegistryAccess;)Lnet/minecraft/world/item/ItemStack;",
            remap = true))
    private ItemStack createdelightcore$wortQuality(MonsterPotRecipe recipe, RegistryAccess registries,
                                                    Operation<ItemStack> original) {
        ItemStack result = original.call(recipe, registries);
        if (!createdelightcore$isWort(recipe)) return result;
        var inventory = ((MonsterPotBlockEntity) (Object) this).getInventory();
        var inputs = new ArrayList<ItemStack>();
        for (int slot = 0; slot < 6; slot++) inputs.add(inventory.getStackInSlot(slot).copyWithCount(4));
        return QualityFoodFluidsCreateRules.applyItemOutputQuality(
                QualityFoodFluidsCreateRules.rollBasinQuality(inputs, java.util.List.of()),
                QualityFoodFluidsApi.clearItemQuality(result.copy()));
    }
}
