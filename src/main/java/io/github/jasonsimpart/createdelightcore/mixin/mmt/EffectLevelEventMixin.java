package io.github.jasonsimpart.createdelightcore.mixin.mmt;

import io.github.jasonsimpart.createdelightcore.compat.mmt.MmtDamageLogContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Pseudo
@Mixin(targets = "com.inolia_zaicek.more_mod_tetra.Event.Post.EffectLevelEvent", remap = false)
public class EffectLevelEventMixin {
    @Shadow
    private float fixedDamage;

    @Shadow
    private float normalMulti;

    @Shadow
    private List<Float> independentMulti;

    @Inject(method = "addFixedDamage", at = @At("RETURN"), require = 0)
    private void createdelightcore$recordFixedDamage(float amount, CallbackInfo ci) {
        MmtDamageLogContext.recordFixedDamage(amount, fixedDamage, "add");
    }

    @Inject(method = "setFixedDamage", at = @At("RETURN"), require = 0)
    private void createdelightcore$recordSetFixedDamage(float amount, CallbackInfo ci) {
        MmtDamageLogContext.recordFixedDamage(amount, fixedDamage, "set");
    }

    @Inject(method = "addNormalMulti", at = @At("RETURN"), require = 0)
    private void createdelightcore$recordNormalMultiplier(float amount, CallbackInfo ci) {
        MmtDamageLogContext.recordNormalMultiplier(amount, normalMulti, "add");
    }

    @Inject(method = "setNormalMulti", at = @At("RETURN"), require = 0)
    private void createdelightcore$recordSetNormalMultiplier(float amount, CallbackInfo ci) {
        MmtDamageLogContext.recordNormalMultiplier(amount, normalMulti, "set");
    }

    @Inject(method = "addIndependentMulti", at = @At("RETURN"), require = 0)
    private void createdelightcore$recordIndependentDamageMultiplier(float multiplier, CallbackInfo ci) {
        MmtDamageLogContext.recordIndependentMultiplier(multiplier, product(independentMulti));
    }

    private static float product(List<Float> values) {
        float result = 1.0F;
        for (float value : values) {
            result *= value;
        }
        return result;
    }
}
