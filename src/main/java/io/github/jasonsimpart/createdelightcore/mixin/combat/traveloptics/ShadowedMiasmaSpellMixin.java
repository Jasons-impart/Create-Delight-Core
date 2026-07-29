package io.github.jasonsimpart.createdelightcore.mixin.combat.traveloptics;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.jasonsimpart.createdelightcore.compat.combat.MiasmaPowerScaling;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@Mixin(targets = "com.gametechbc.traveloptics.spells.eldritch.ShadowedMiasmaSpell", remap = false)
public abstract class ShadowedMiasmaSpellMixin {
    @ModifyExpressionValue(
            method = {"onCast", "getUniqueInfo"},
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/gametechbc/traveloptics/spells/eldritch/ShadowedMiasmaSpell;getSpellPower(ILnet/minecraft/world/entity/Entity;)F"
            ),
            require = 1
    )
    private float createdelightcore$useDiminishingMiasmaPower(
            float originalPower,
            @Local(argsOnly = true) int spellLevel
    ) {
        float base = 2.0F + spellLevel - 1.0F;
        float rawMultiplier = base == 0.0F ? 0.0F : originalPower / base;
        return MiasmaPowerScaling.getMiasmaPower(spellLevel, rawMultiplier);
    }
}
