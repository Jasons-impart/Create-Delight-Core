package io.github.jasonsimpart.mixin.vintageimprovements;

import com.simibubi.create.content.processing.basin.BasinRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = BasinRecipe.class, remap = false)
public abstract class VacuumizingOutputLimitMixin {
    @Inject(method = "getMaxOutputCount", at = @At("RETURN"), cancellable = true)
    private void createdelightcore$vacuumOutputSlots(CallbackInfoReturnable<Integer> cir) {
        if (getClass().getName().equals("com.negodya1.vintageimprovements.content.kinetics.vacuum_chamber.VacuumizingRecipe"))
            cir.setReturnValue(9);
    }
}
