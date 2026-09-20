package io.github.jasonsimpart.mixin.eclipticseasons;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.jasonsimpart.compat.eclipticseasons.QualityCropGrowth;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "com.teamtea.eclipticseasons.common.item.GrowthDetectorItem", remap = false)
public abstract class QualityGrowthDetectorMixin {
    @ModifyExpressionValue(method = "getGrowChance", at = @At(value = "INVOKE",
            target = "Lcom/teamtea/eclipticseasons/api/data/crop/GrowParameter;grow_chance()F"))
    private static float createdelightcore$qualitySeason(float chance, @Local(argsOnly = true) Level level,
                                                        @Local(argsOnly = true) BlockPos pos) {
        return QualityCropGrowth.apply(chance, level, pos);
    }

    @Inject(method = "getHumidityGrowChance", at = @At("RETURN"), cancellable = true)
    private static void createdelightcore$qualityHumidity(CallbackInfoReturnable<Float> result,
            @Local(argsOnly = true) Level level, @Local(argsOnly = true) BlockPos pos,
            @Local(argsOnly = true) boolean hasUpdate) {
        // Rain/greenhouse corrections recurse: apply the quality boost only at the outer return.
        if (!hasUpdate) result.setReturnValue(QualityCropGrowth.apply(result.getReturnValue(), level, pos));
    }
}
