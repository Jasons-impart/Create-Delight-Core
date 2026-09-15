package io.github.jasonsimpart.createdelightcore.mixin.combat.apothicattributes;

import io.github.jasonsimpart.createdelightcore.compat.combat.diagnostics.DamageDiagnostics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "dev.shadowsoffire.attributeslib.api.ALCombatRules", remap = false)
public abstract class ArmorFormulaDiagnosticsMixin {
    @Inject(method = "getAValue", at = @At("HEAD"), require = 1)
    private static void createdelightcore$observeAValueInput(float damage, CallbackInfoReturnable<Float> cir) {
        DamageDiagnostics.checkpoint("AttributesLib.getAValue input", damage);
    }
}
