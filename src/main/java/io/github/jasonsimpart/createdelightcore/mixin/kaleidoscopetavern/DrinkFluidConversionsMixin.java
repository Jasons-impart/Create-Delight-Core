package io.github.jasonsimpart.createdelightcore.mixin.kaleidoscopetavern;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import de.cadentem.quality_food.util.QualityUtils;
import io.github.createdelight.qualityfoodfluids.api.QualityFoodFluidsApi;
import io.github.jasonsimpart.createdelightcore.compat.kaleidoscopetavern.TavernQuality;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

/** CGT's nested item tag alone is invisible to QFF tanks and pipes. */
@Pseudo
@Mixin(targets = "com.yision.creategearsandtavern.content.fluids.drink.DrinkFluidConversions", remap = false)
public abstract class DrinkFluidConversionsMixin {
    @ModifyReturnValue(method = "fromItem(Lnet/minecraft/world/item/ItemStack;II)Lnet/minecraftforge/fluids/FluidStack;",
            at = @At("RETURN"))
    private static FluidStack createdelightcore$writeFluidQuality(FluidStack result, ItemStack input, int amount, int brewLevel) {
        return result.isEmpty() || !TavernQuality.supportsQuality(input) ? result
                : QualityFoodFluidsApi.withQuality(result, QualityUtils.getQuality(input));
    }

    @ModifyReturnValue(method = "toItem", at = @At("RETURN"))
    private static ItemStack createdelightcore$writeBottleQuality(ItemStack result, FluidStack input) {
        return TavernQuality.supportsQuality(result) ? TavernQuality.qualify(result, QualityFoodFluidsApi.getQuality(input)) : result;
    }
}
