package io.github.jasonsimpart.createdelightcore.mixin.combat;

import io.github.jasonsimpart.createdelightcore.compat.combat.OriginalDamageAccess;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import io.github.jasonsimpart.createdelightcore.compat.combat.diagnostics.DamageDiagnostics;
import io.github.jasonsimpart.createdelightcore.compat.combat.diagnostics.DamageTrace;
import io.github.jasonsimpart.createdelightcore.compat.combat.diagnostics.DamageTraceAccess;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = LivingHurtEvent.class, remap = false)
public abstract class LivingHurtEventMixin implements OriginalDamageAccess, DamageTraceAccess {
    @Unique
    private float createdelightcore$originalDamage;
    @Unique
    private DamageTrace createdelightcore$damageTrace;

    @Inject(method = "<init>", at = @At("RETURN"), require = 1)
    private void createdelightcore$captureOriginalDamage(
            LivingEntity entity,
            DamageSource source,
            float amount,
            CallbackInfo ci
    ) {
        this.createdelightcore$originalDamage = amount;
        DamageDiagnostics.created(this, "LivingHurtEvent", entity, source, amount);
    }

    @WrapMethod(method = "setAmount")
    private void createdelightcore$observeDamageWrite(float amount, Operation<Void> original) {
        float before = ((LivingHurtEvent) (Object) this).getAmount();
        original.call(amount);
        DamageDiagnostics.setAmount(this, before, ((LivingHurtEvent) (Object) this).getAmount());
    }

    @Override
    public DamageTrace createdelightcore$getDamageTrace() { return createdelightcore$damageTrace; }

    @Override
    public void createdelightcore$setDamageTrace(DamageTrace trace) { createdelightcore$damageTrace = trace; }

    @Override
    public float createdelightcore$getOriginalDamage() {
        return this.createdelightcore$originalDamage;
    }
}
