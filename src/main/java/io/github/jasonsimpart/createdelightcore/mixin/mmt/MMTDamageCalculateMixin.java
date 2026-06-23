package io.github.jasonsimpart.createdelightcore.mixin.mmt;

import io.github.jasonsimpart.createdelightcore.compat.mmt.MmtDamageLogContext;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "com.inolia_zaicek.more_mod_tetra.Event.MMTDamageCalculate", remap = false)
public class MMTDamageCalculateMixin {
    @Inject(method = "hurt", at = @At("HEAD"), require = 0)
    private static void createdelightcore$beginDamageLog(LivingHurtEvent event, CallbackInfo ci) {
        MmtDamageLogContext.begin(event);
    }

    @Inject(method = "hurt", at = @At("RETURN"), require = 0)
    private static void createdelightcore$finishDamageLog(LivingHurtEvent event, CallbackInfo ci) {
        MmtDamageLogContext.finish(event);
    }
}
