package io.github.jasonsimpart.createdelightcore.mixin.creategearsandtavern;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.createdelight.qualityfoodfluids.api.QualityFoodFluidsApi;
import mezz.jei.api.ingredients.IIngredientTypeWithSubtypes;
import mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@Mixin(targets = "com.yision.creategearsandtavern.compat.jei.CGTJeiPlugin", remap = false)
public abstract class CgtJeiSubtypeRegistrationMixin {
    @WrapOperation(method = "lambda$registerFluidSubtypes$3", require = 2,
            at = @At(value = "INVOKE",
                    target = "Lmezz/jei/api/registration/ISubtypeRegistration;registerSubtypeInterpreter(Lmezz/jei/api/ingredients/IIngredientTypeWithSubtypes;Ljava/lang/Object;Lmezz/jei/api/ingredients/subtypes/IIngredientSubtypeInterpreter;)V"))
    private static void createdelightcore$registerWithoutQualityDuplicates(ISubtypeRegistration registration,
            IIngredientTypeWithSubtypes<?, ?> type, Object base, IIngredientSubtypeInterpreter<?> interpreter,
            Operation<Void> original) {
        // QFF registers the same source and flowing fluids regardless of JEI plugin order.
        if (base instanceof Fluid fluid && QualityFoodFluidsApi.canCarryQuality(new FluidStack(fluid, 1000))) return;
        original.call(registration, type, base, interpreter);
    }
}
