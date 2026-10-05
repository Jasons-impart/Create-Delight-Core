package io.github.jasonsimpart.createdelightcore.mixin.eclipticseason;

import com.teamtea.eclipticseasons.client.util.ClientExtraUtil;
import io.github.jasonsimpart.createdelightcore.network.ClientHumidityRoomCache;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ClientExtraUtil.class, remap = false)
public abstract class SmartHumidityClientReadingMixin {
    @Inject(method = "modifyHumidity", at = @At("HEAD"), cancellable = true)
    private static void createdelightcore$roomReading(Level level, BlockPos pos, float natural,
                                                      CallbackInfoReturnable<Float> cir) {
        Float target = ClientHumidityRoomCache.target(level, pos);
        if (target != null) cir.setReturnValue(target);
    }
}
