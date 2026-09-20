package io.github.jasonsimpart.mixin.vintageimprovements;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Match Vintage's actual nine-slot output inventory; preserve every ore roll. */
@Mixin(targets = "com.negodya1.vintageimprovements.content.kinetics.vibration.VibratingRecipe", remap = false)
public abstract class VibratingOutputLimitMixin {
    @Inject(method = "getMaxOutputCount", at = @At("RETURN"), cancellable = true)
    private void createdelightcore$outputSlots(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(9);
    }
}
