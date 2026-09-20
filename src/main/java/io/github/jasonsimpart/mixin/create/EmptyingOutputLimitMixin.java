package io.github.jasonsimpart.mixin.create;

import com.simibubi.create.content.fluids.transfer.EmptyingRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = EmptyingRecipe.class, remap = false)
public abstract class EmptyingOutputLimitMixin {
    @Inject(method = "getMaxOutputCount", at = @At("HEAD"), cancellable = true)
    private void createdelightcore$allowQueuedOutputs(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(4);
    }
}
