package io.github.jasonsimpart.createdelightcore.mixin.eclipticseason;

import com.teamtea.eclipticseasons.api.util.EclipticUtil;
import io.github.jasonsimpart.createdelightcore.content.humidity.SmartHumidityRegulatorSeasonCompat;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = EclipticUtil.class, remap = false)
public abstract class SmartHumidityReadingMixin {
    @Inject(method = "getHumidityAfterCheck", at = @At("HEAD"), cancellable = true)
    private static void createdelightcore$readRegulatedHumidity(ServerLevel level, BlockPos pos, float base,
                                                               CallbackInfoReturnable<Float> cir) {
        Float target = SmartHumidityRegulatorSeasonCompat.target(level, pos);
        if (target != null) cir.setReturnValue(target);
    }
}
