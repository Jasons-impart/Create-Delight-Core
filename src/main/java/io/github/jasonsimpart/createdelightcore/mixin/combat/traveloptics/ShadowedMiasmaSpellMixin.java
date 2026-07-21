package io.github.jasonsimpart.createdelightcore.mixin.combat.traveloptics;

import io.github.jasonsimpart.createdelightcore.CDConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Pseudo
@Mixin(targets = "com.gametechbc.traveloptics.spells.eldritch.ShadowedMiasmaSpell", remap = false)
public abstract class ShadowedMiasmaSpellMixin {
    @ModifyArg(
            method = "onCast",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/effect/MobEffectInstance;<init>(Lnet/minecraft/world/effect/MobEffect;II)V"
            ),
            index = 2,
            require = 1
    )
    private int createdelightcore$capAbyssalStrikeAmplifier(int amplifier) {
        int maxLevel = CDConfig.shadowedMiasmaMaxAbyssalStrikeLevel;
        return maxLevel > 0 ? Math.min(amplifier, maxLevel - 1) : amplifier;
    }

    @ModifyArg(
            method = "getUniqueInfo",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/lang/Integer;valueOf(I)Ljava/lang/Integer;"
            ),
            index = 0,
            require = 1
    )
    private int createdelightcore$showCappedAbyssalStrikeLevel(int level) {
        int maxLevel = CDConfig.shadowedMiasmaMaxAbyssalStrikeLevel;
        return maxLevel > 0 ? Math.min(level, maxLevel) : level;
    }
}
