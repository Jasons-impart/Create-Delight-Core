package io.github.jasonsimpart.createdelightcore.mixin.combat;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;

/** Single late postApply marker: repair resistance, then observe merged damage methods. */
@Mixin(value = {LivingEntity.class, Player.class}, priority = 1)
public abstract class LivingDamagePipelineArithmeticMixin {}
