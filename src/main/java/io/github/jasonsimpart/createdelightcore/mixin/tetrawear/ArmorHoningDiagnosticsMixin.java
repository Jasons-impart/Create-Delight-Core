package io.github.jasonsimpart.createdelightcore.mixin.tetrawear;

import io.github.jasonsimpart.createdelightcore.compat.combat.diagnostics.DamageDiagnostics;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "se.mickelus.tetrawear.effects.ArmorHoning", remap = false)
public abstract class ArmorHoningDiagnosticsMixin {
    @Inject(method = "onLivingHurt", at = @At("HEAD"), require = 1)
    private static void createdelightcore$observeHoningInput(LivingHurtEvent event, CallbackInfo ci) {
        DamageDiagnostics.checkpoint("TetraWear.ArmorHoning input", event.getAmount());
    }
}
