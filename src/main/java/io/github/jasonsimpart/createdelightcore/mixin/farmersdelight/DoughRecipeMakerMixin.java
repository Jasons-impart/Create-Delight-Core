package io.github.jasonsimpart.createdelightcore.mixin.farmersdelight;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vectorwing.farmersdelight.integration.jei.resource.DoughRecipeMaker;

import java.util.List;

@Mixin(DoughRecipeMaker.class)
public class DoughRecipeMakerMixin {
    @Inject(method = "createRecipe()Ljava/util/List;",at = @At("HEAD"), cancellable = true, remap = false)
    private static void disableDoughRecipe(CallbackInfoReturnable<List<?>> cir) {
        cir.setReturnValue(null);
    }
}