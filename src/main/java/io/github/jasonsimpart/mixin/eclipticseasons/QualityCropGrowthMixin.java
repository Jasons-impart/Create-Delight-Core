package io.github.jasonsimpart.mixin.eclipticseasons;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.jasonsimpart.compat.eclipticseasons.QualityCropGrowth;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "com.teamtea.eclipticseasons.common.core.crop.CropGrowthHandler", remap = false)
public abstract class QualityCropGrowthMixin {
    // Retain Ecliptic's event-dependent (natural growth / bonemeal) base probability.
    @ModifyExpressionValue(method = {
            "beforeCropGrowUp(Lnet/neoforged/bus/api/Event;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V",
            "checkHumidity"
    }, at = @At(value = "INVOKE", target = "Lcom/teamtea/eclipticseasons/common/core/crop/CropGrowthHandler;getGrowChance(Lnet/neoforged/bus/api/Event;Lcom/teamtea/eclipticseasons/api/data/crop/GrowParameter;)F"))
    private static float createdelightcore$qualityGrowth(float chance, @Local(argsOnly = true) Level level,
                                                        @Local(argsOnly = true) BlockPos pos) {
        return QualityCropGrowth.apply(chance, level, pos);
    }
}
