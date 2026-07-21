package io.github.jasonsimpart.createdelightcore.mixin.combat;

import io.github.jasonsimpart.createdelightcore.compat.combat.OriginalDamageAccess;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = LivingHurtEvent.class, remap = false)
public abstract class LivingHurtEventMixin implements OriginalDamageAccess {
    @Unique
    private float createdelightcore$originalDamage;

    @Inject(method = "<init>", at = @At("RETURN"), require = 1)
    private void createdelightcore$captureOriginalDamage(
            LivingEntity entity,
            DamageSource source,
            float amount,
            CallbackInfo ci
    ) {
        this.createdelightcore$originalDamage = amount;
    }

    @Override
    public float createdelightcore$getOriginalDamage() {
        return this.createdelightcore$originalDamage;
    }
}
