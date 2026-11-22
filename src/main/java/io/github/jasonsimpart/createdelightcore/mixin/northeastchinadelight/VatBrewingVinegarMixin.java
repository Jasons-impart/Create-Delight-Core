package io.github.jasonsimpart.createdelightcore.mixin.northeastchinadelight;

import com.gunmu.northeast_china_delight.block.VatBlockEntity;
import com.gunmu.northeast_china_delight.crafting.VatBrewing;
import com.gunmu.northeast_china_delight.crafting.VatRecipe;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import de.cadentem.quality_food.core.Quality;
import io.github.createdelight.qualityfoodfluids.api.QualityFoodFluidsCreateRules;
import io.github.jasonsimpart.createdelightcore.compat.northeastchinadelight.PackVinegar;
import io.github.jasonsimpart.createdelightcore.compat.northeastchinadelight.VatVinegarAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ArrayList;
import java.util.List;

@Pseudo
@Mixin(value = VatBrewing.class, remap = false)
public abstract class VatBrewingVinegarMixin {
    @WrapOperation(method = "complete", at = @At(value = "INVOKE",
            target = "Lcom/gunmu/northeast_china_delight/block/VatBlockEntity;addProduct(Lnet/minecraft/world/level/material/Fluid;I)V"))
    private static void createdelightcore$qualifyVinegar(VatBlockEntity vat, Fluid fluid, int amount, Operation<Void> original,
                                                        @Local(argsOnly = true) VatRecipe recipe) {
        if (PackVinegar.VAT_RECIPE.toString().equals(recipe.id()) && PackVinegar.isFluid(fluid) && vat.productMb(fluid) == 0) {
            List<ItemStack> consumed = new ArrayList<>();
            for (ItemStack stack : vat.contents()) {
                if (stack.isEmpty()) continue;
                for (int slot = 0; slot < recipe.slots().size(); slot++) {
                    if (recipe.slots().get(slot).fate() == VatRecipe.Fate.CONSUME && recipe.accepts(slot, stack)) {
                        consumed.add(stack.copy());
                        break;
                    }
                }
            }
            Quality quality = QualityFoodFluidsCreateRules.rollBasinQuality(consumed, List.of());
            ((VatVinegarAccess) vat).createdelightcore$setVinegarQuality(quality);
        }
        original.call(vat, fluid, amount);
    }

    @WrapMethod(method = "fillContainer")
    private static ItemStack createdelightcore$fillVinegarBottle(VatBlockEntity vat, ItemStack empty, Operation<ItemStack> original) {
        Quality quality = ((VatVinegarAccess) vat).createdelightcore$getVinegarQuality();
        ItemStack result = original.call(vat, empty);
        return result != null && PackVinegar.isBottle(result)
                ? QualityFoodFluidsCreateRules.applyItemOutputQuality(quality, result) : result;
    }
}
