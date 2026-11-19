package io.github.jasonsimpart.createdelightcore.mixin.kaleidoscopetavern;

import com.github.ysbbbbbb.kaleidoscopetavern.blockentity.brew.PressingTubBlockEntity;
import com.github.ysbbbbbb.kaleidoscopetavern.crafting.recipe.PressingTubRecipe;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import de.cadentem.quality_food.util.QualityUtils;
import io.github.createdelight.qualityfoodfluids.api.QualityFoodFluidsApi;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.ItemStackHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(value = PressingTubBlockEntity.class, remap = false)
public abstract class PressingTubQualityMixin {
    @Shadow public abstract ItemStackHandler getItems();
    @Shadow public abstract FluidTank getFluid();

    @Inject(method = "lambda$press$0", at = @At("HEAD"), cancellable = true)
    private void createdelightcore$checkQualifiedJuiceFits(ItemStack ingredient, SimpleContainer input,
            PressingTubRecipe recipe, CallbackInfoReturnable<Boolean> cir) {
        var output = QualityFoodFluidsApi.withQuality(new FluidStack(recipe.getFluid(), recipe.getFluidAmount()),
                QualityUtils.getQuality(ingredient));
        // The native method ignores fill's return value and would otherwise consume rejected ingredients.
        if (getFluid().fill(output, IFluidHandler.FluidAction.SIMULATE) < output.getAmount()) {
            cir.setReturnValue(false);
        }
    }

    @ModifyExpressionValue(method = "lambda$press$0", at = @At(value = "NEW",
            target = "(Lnet/minecraft/world/level/material/Fluid;I)Lnet/minecraftforge/fluids/FluidStack;"))
    private FluidStack createdelightcore$inheritFruitQuality(FluidStack output) {
        return QualityFoodFluidsApi.withQuality(output, QualityUtils.getQuality(getItems().getStackInSlot(0)));
    }
}
