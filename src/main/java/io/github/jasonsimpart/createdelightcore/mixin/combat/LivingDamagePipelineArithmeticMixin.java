package io.github.jasonsimpart.createdelightcore.mixin.combat;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;

/** Late postApply marker: observe the merged damage methods and their local Mixin callees. */
@Mixin(value = {LivingEntity.class, Player.class}, priority = 1)
public abstract class LivingDamagePipelineArithmeticMixin {}
