package io.github.jasonsimpart.createdelightcore.mixin.combat.ironsspellbooks;

import io.github.jasonsimpart.createdelightcore.CDConfig;
import io.github.jasonsimpart.createdelightcore.compat.combat.OriginalDamageAccess;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(targets = "io.redspace.ironsspellbooks.effect.EchoingStrikesEffect", remap = false)
public abstract class EchoingStrikesEffectMixin {
    @Redirect(
            method = "createEcho",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraftforge/event/entity/living/LivingHurtEvent;getAmount()F"
            ),
            require = 1
    )
    private static float createdelightcore$useOriginalDamage(LivingHurtEvent event) {
        if (CDConfig.echoingStrikesUseOriginalDamage && event instanceof OriginalDamageAccess originalDamageAccess) {
            return originalDamageAccess.createdelightcore$getOriginalDamage();
        }
        return event.getAmount();
    }
}
