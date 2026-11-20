package io.github.jasonsimpart.createdelightcore.mixin.creategearsandtavern;

import io.github.jasonsimpart.createdelightcore.compat.creategearsandtavern.CgtJeiFluidEntries;
import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.Collection;

@Pseudo
@Mixin(targets = "com.yision.creategearsandtavern.compat.jei.CGTJeiPlugin", remap = false)
public abstract class CgtJeiPluginMixin {
    @ModifyArg(
            method = "registerExtraIngredients",
            at = @At(value = "INVOKE", target = "Lmezz/jei/api/registration/IExtraIngredientRegistration;addExtraIngredients(Lmezz/jei/api/ingredients/IIngredientType;Ljava/util/Collection;)V"),
            index = 1,
            remap = false
    )
    private Collection<FluidStack> createdelightcore$oneBaseEntryPerDrink(Collection<FluidStack> variants) {
        return CgtJeiFluidEntries.baseEntries(variants);
    }
}
