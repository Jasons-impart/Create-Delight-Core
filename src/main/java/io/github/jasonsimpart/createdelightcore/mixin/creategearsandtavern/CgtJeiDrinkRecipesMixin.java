package io.github.jasonsimpart.createdelightcore.mixin.creategearsandtavern;

import io.github.jasonsimpart.createdelightcore.compat.creategearsandtavern.CgtJeiFluidEntries;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Pseudo
@Mixin(targets = "com.yision.creategearsandtavern.compat.jei.CGTJeiDrinkRecipes", remap = false)
public abstract class CgtJeiDrinkRecipesMixin {
    @Inject(method = "samples", at = @At("RETURN"), cancellable = true, remap = false)
    private static void createdelightcore$baseBottleSamples(int minBrew,
            CallbackInfoReturnable<List<ItemStack>> cir) {
        cir.setReturnValue(CgtJeiFluidEntries.baseBottleSamples(cir.getReturnValue()));
    }

    @ModifyArg(
            method = "registerRecipes",
            at = @At(value = "INVOKE", target = "Lcom/simibubi/create/foundation/fluid/FluidIngredient;fromFluidStack(Lnet/minecraftforge/fluids/FluidStack;)Lcom/simibubi/create/foundation/fluid/FluidIngredient;"),
            index = 0,
            remap = false
    )
    private FluidStack createdelightcore$baseFillingFluid(FluidStack stack) {
        return CgtJeiFluidEntries.baseRecipeFluid(stack);
    }

    @ModifyArg(
            method = "registerRecipes",
            at = @At(value = "INVOKE", target = "Lcom/simibubi/create/content/processing/recipe/ProcessingRecipeBuilder;withFluidOutputs([Lnet/minecraftforge/fluids/FluidStack;)Lcom/simibubi/create/content/processing/recipe/ProcessingRecipeBuilder;"),
            index = 0,
            remap = false
    )
    private FluidStack[] createdelightcore$baseEmptyingFluids(FluidStack[] stacks) {
        return CgtJeiFluidEntries.baseRecipeOutputs(stacks);
    }
}
