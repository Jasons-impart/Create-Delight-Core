package io.github.jasonsimpart.createdelightcore.mixin.combat.apothicattributes;

import dev.shadowsoffire.attributeslib.AttributesLib;
import dev.shadowsoffire.attributeslib.api.ALObjects;
import dev.shadowsoffire.attributeslib.impl.AttributeEvents;
import dev.shadowsoffire.attributeslib.packet.CritParticleMessage;
import dev.shadowsoffire.placebo.network.PacketDistro;
import io.github.jasonsimpart.createdelightcore.CDConfig;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = AttributeEvents.class, remap = false)
public abstract class AttributeEventsMixin {
    @Inject(method = "apothCriticalStrike", at = @At("HEAD"), cancellable = true, require = 1)
    private void createdelightcore$useAdditiveMulticrit(LivingHurtEvent event, CallbackInfo ci) {
        if (!CDConfig.enableAdditiveMulticrit) {
            return;
        }

        Entity sourceEntity = event.getSource().getEntity();
        if (!(sourceEntity instanceof LivingEntity attacker)) {
            ci.cancel();
            return;
        }

        double critChance = attacker.getAttributeValue(ALObjects.Attributes.CRIT_CHANCE.get());
        float critDamage = (float) attacker.getAttributeValue(ALObjects.Attributes.CRIT_DAMAGE.get());
        RandomSource random = event.getEntity().getRandom();
        float originalDamage = event.getAmount();
        float modifiedDamage = originalDamage;
        boolean didCrit = false;

        while (random.nextFloat() <= critChance && critDamage > 1.0F) {
            critChance--;
            modifiedDamage += originalDamage * (critDamage - 1.0F);
            critDamage *= 0.85F;
            didCrit = true;
        }

        event.setAmount(modifiedDamage);
        if (didCrit && !attacker.level().isClientSide) {
            PacketDistro.sendToTracking(
                    AttributesLib.CHANNEL,
                    new CritParticleMessage(event.getEntity().getId()),
                    (ServerLevel) attacker.level(),
                    event.getEntity().blockPosition()
            );
        }
        ci.cancel();
    }
}
