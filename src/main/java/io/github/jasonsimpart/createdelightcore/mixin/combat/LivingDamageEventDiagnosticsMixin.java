package io.github.jasonsimpart.createdelightcore.mixin.combat;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import io.github.jasonsimpart.createdelightcore.compat.combat.diagnostics.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = LivingDamageEvent.class, remap = false)
public abstract class LivingDamageEventDiagnosticsMixin implements DamageTraceAccess {
    @Unique private DamageTrace createdelightcore$damageTrace;

    @Inject(method = "<init>", at = @At("RETURN"), require = 1)
    private void createdelightcore$created(LivingEntity entity, DamageSource source, float amount, CallbackInfo ci) {
        DamageDiagnostics.created(this, "LivingDamageEvent", entity, source, amount);
    }

    @WrapMethod(method = "setAmount")
    private void createdelightcore$observeWrite(float amount, Operation<Void> original) {
        float before = ((LivingDamageEvent) (Object) this).getAmount();
        original.call(amount);
        DamageDiagnostics.setAmount(this, before, ((LivingDamageEvent) (Object) this).getAmount());
    }

    @Override public DamageTrace createdelightcore$getDamageTrace() { return createdelightcore$damageTrace; }
    @Override public void createdelightcore$setDamageTrace(DamageTrace trace) { createdelightcore$damageTrace = trace; }
}
