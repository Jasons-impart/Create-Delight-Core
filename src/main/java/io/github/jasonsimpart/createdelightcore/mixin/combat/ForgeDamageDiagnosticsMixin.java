package io.github.jasonsimpart.createdelightcore.mixin.combat;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import io.github.jasonsimpart.createdelightcore.compat.combat.diagnostics.DamageDiagnostics;
import io.github.jasonsimpart.createdelightcore.compat.combat.diagnostics.DamageTrace;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.ForgeHooks;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = ForgeHooks.class, remap = false)
public abstract class ForgeDamageDiagnosticsMixin {
    @WrapMethod(method = "onLivingHurt")
    private static float createdelightcore$traceHurt(LivingEntity entity, DamageSource source, float amount,
                                                    Operation<Float> original) {
        DamageTrace trace = DamageDiagnostics.begin("LivingHurtEvent", entity, source, amount);
        try {
            float result = original.call(entity, source, amount);
            DamageDiagnostics.returned(trace, result);
            return result;
        } catch (RuntimeException | Error failure) {
            DamageDiagnostics.failed(trace, failure);
            throw failure;
        } finally {
            DamageDiagnostics.end(trace);
        }
    }

    @WrapMethod(method = "onLivingDamage")
    private static float createdelightcore$traceDamage(LivingEntity entity, DamageSource source, float amount,
                                                      Operation<Float> original) {
        DamageTrace trace = DamageDiagnostics.begin("LivingDamageEvent", entity, source, amount);
        try {
            float result = original.call(entity, source, amount);
            DamageDiagnostics.returned(trace, result);
            return result;
        } catch (RuntimeException | Error failure) {
            DamageDiagnostics.failed(trace, failure);
            throw failure;
        } finally {
            DamageDiagnostics.end(trace);
        }
    }
}
