package io.github.jasonsimpart.createdelightcore.mixin.combat;

import net.minecraft.world.damagesource.CombatRules;
import org.spongepowered.asm.mixin.Mixin;

/** Observe merged vanilla/AttributesLib armor and magic-reduction arithmetic. */
@Mixin(value = CombatRules.class, priority = 1)
public abstract class CombatRulesDiagnosticsMixin {}
