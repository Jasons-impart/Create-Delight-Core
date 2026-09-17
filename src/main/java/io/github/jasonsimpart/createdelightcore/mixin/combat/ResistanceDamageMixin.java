package io.github.jasonsimpart.createdelightcore.mixin.combat;

import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;

/** Apply the arithmetic fix after normal compatibility mixins, before priority-1 probes. */
@Mixin(value = LivingEntity.class, priority = 2)
public abstract class ResistanceDamageMixin {}
