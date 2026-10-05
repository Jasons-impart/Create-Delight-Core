package io.github.jasonsimpart.createdelightcore.mixin.eclipticseason;

import com.teamtea.eclipticseasons.api.util.EclipticUtil;
import com.teamtea.eclipticseasons.common.core.solar.SolarDataManager;
import io.github.jasonsimpart.createdelightcore.content.humidity.SmartHumidityRegulatorSeasonCompat;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.lang.ref.WeakReference;

/** Legacy additive readers receive the delta corresponding to the absolute target. */
@Mixin(value = SolarDataManager.class, remap = false)
public abstract class SmartHumidityModificationMixin {
    @Shadow protected WeakReference<Level> levelWeakReference;

    @Inject(method = "calculateHumidityModification", at = @At("HEAD"), cancellable = true)
    private void createdelightcore$regulate(BlockPos pos, CallbackInfoReturnable<Float> cir) {
        if (!(levelWeakReference.get() instanceof ServerLevel level)) return;
        Float target = SmartHumidityRegulatorSeasonCompat.target(level, pos);
        if (target != null) cir.setReturnValue(target - EclipticUtil.getHumidityLevelAt(level, pos));
    }
}
