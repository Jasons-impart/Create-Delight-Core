package io.github.jasonsimpart.createdelightcore.mixin.combat;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import io.github.jasonsimpart.createdelightcore.compat.combat.diagnostics.DamageDiagnostics;
import io.github.jasonsimpart.createdelightcore.compat.combat.diagnostics.DamageTrace;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;

/** Keep a scope alive across both Forge events, including exceptional and nested attacks. */
@Mixin(value = {LivingEntity.class, Player.class}, priority = 10000)
public abstract class LivingDamagePipelineScopeMixin {
    @WrapMethod(method = "actuallyHurt")
    private void createdelightcore$tracePipeline(DamageSource source, float amount, Operation<Void> original) {
        LivingEntity self = (LivingEntity) (Object) this;
        DamageTrace trace = DamageDiagnostics.begin(self instanceof Player
                ? "Player.actuallyHurt" : "LivingEntity.actuallyHurt", self, source, amount);
        try {
            original.call(source, amount);
        } catch (RuntimeException | Error failure) {
            DamageDiagnostics.failed(trace, failure);
            throw failure;
        } finally {
            DamageDiagnostics.end(trace);
        }
    }
}
