package io.github.jasonsimpart.createdelightcore.mixin.eclipticseason;

import com.teamtea.eclipticseasons.api.util.EclipticUtil;
import com.teamtea.eclipticseasons.common.core.solar.SolarDataManager;
import io.github.jasonsimpart.createdelightcore.network.ClientHumidityRoomCache;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.lang.ref.WeakReference;

/** Mirror the shared facility delta lookup on the client. */
@Mixin(value = SolarDataManager.class, remap = false)
public abstract class SmartHumidityClientModificationMixin {
    @Shadow protected WeakReference<Level> levelWeakReference;

    @Inject(method = "calculateHumidityModification", at = @At("HEAD"), cancellable = true)
    private void createdelightcore$clientRoomModifier(BlockPos pos, CallbackInfoReturnable<Float> cir) {
        Level level = levelWeakReference.get();
        if (level == null || !level.isClientSide) return;
        Float target = ClientHumidityRoomCache.target(level, pos);
        if (target != null) cir.setReturnValue(target - EclipticUtil.getHumidityLevelAt(level, pos));
    }
}
