package io.github.jasonsimpart.createdelightcore.mixin.ironsspellbooks;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.createdelight.qualityfoodfluids.api.QualityFoodFluidsApi;
import io.redspace.ironsspellbooks.recipe_types.alchemist_cauldron.BrewAlchemistCauldronRecipe;
import io.redspace.ironsspellbooks.recipe_types.alchemist_cauldron.EmptyAlchemistCauldronRecipe;
import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Ignore only quality when testing recipes; storage still separates different quality batches. */
@Mixin(value = {BrewAlchemistCauldronRecipe.class, EmptyAlchemistCauldronRecipe.class}, remap = false)
public abstract class AlchemistRecipeQualityMixin {
    @WrapOperation(method = "matches", at = @At(value = "INVOKE",
            target = "Lio/redspace/ironsspellbooks/api/backwards_compat/FluidHelper;isSameFluidSameComponents(Lnet/minecraftforge/fluids/FluidStack;Lnet/minecraftforge/fluids/FluidStack;)Z"))
    private boolean createdelightcore$matchWithoutQuality(FluidStack expected, FluidStack actual,
                                                          Operation<Boolean> original) {
        return original.call(QualityFoodFluidsApi.stripQualityForComparison(expected),
                QualityFoodFluidsApi.stripQualityForComparison(actual));
    }
}
