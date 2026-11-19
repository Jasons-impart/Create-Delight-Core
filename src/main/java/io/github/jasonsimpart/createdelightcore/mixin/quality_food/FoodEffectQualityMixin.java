package io.github.jasonsimpart.createdelightcore.mixin.quality_food;

import de.cadentem.quality_food.core.Quality;
import de.cadentem.quality_food.util.FoodUtils;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.registries.tags.ITag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Pseudo
@Mixin(value = FoodUtils.class, remap = false)
public abstract class FoodEffectQualityMixin {
    @ModifyConstant(
            method = "modifyEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lde/cadentem/quality_food/core/Quality;Lnet/minecraftforge/registries/tags/ITag;)Ljava/util/Optional;",
            constant = @Constant(intValue = 0, expandZeroConditions = Constant.Condition.LESS_THAN_OR_EQUAL_TO_ZERO),
            require = 1, allow = 1)
    private static int createdelightcore$instantDurationThreshold(int threshold, MobEffectInstance instance,
                                                                  Quality quality, ITag<MobEffect> blacklist) {
        // QF still owns amplifier/probability scaling; instantaneous effects do not need a positive duration.
        return instance.getEffect().isInstantenous() ? -1 : threshold;
    }
}
