package io.github.jasonsimpart.createdelightcore.mixin.eclipticseason;

import com.teamtea.eclipticseasons.common.block.HygrometerBlock;
import com.teamtea.eclipticseasons.api.constant.biome.Humidity;
import io.github.jasonsimpart.createdelightcore.content.humidity.SmartHumidityRegulatorSeasonCompat;
import io.github.jasonsimpart.createdelightcore.network.ClientHumidityRoomCache;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = HygrometerBlock.class, remap = false)
public abstract class SmartHumidityHygrometerMixin {
    @Inject(method = "getNewState", at = @At("HEAD"), cancellable = true)
    private static void createdelightcore$absoluteReading(Level level, BlockState state, BlockPos pos,
                                                           CallbackInfoReturnable<BlockState> cir) {
        // The native meter samples modifiers in front, but natural humidity at its own wall cell.
        // Read the controlled interior directly to avoid blending two different positions.
        BlockPos sample = pos.relative(state.getValue(BlockStateProperties.HORIZONTAL_FACING));
        Float target = level instanceof ServerLevel server
                ? SmartHumidityRegulatorSeasonCompat.target(server, sample)
                : ClientHumidityRoomCache.target(level, sample);
        if (target != null) cir.setReturnValue(state.setValue(BlockStateProperties.POWER,
                HygrometerBlock.getPowerFromHumidityLevel(Humidity.getHumid(target).ordinal())));
    }
}
