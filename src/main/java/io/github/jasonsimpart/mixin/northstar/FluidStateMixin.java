package io.github.jasonsimpart.mixin.northstar;

import io.github.jasonsimpart.compat.northstar.NorthstarSurfaceFreezeGuard;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = FluidState.class, priority = 500)
public class FluidStateMixin {
    @Inject(method = "tick", at = @At("TAIL"))
    private void createdelightcore$keepEnceladusFreezeNearSurface(Level level, BlockPos pos, CallbackInfo ci) {
        if (level.getBlockState(pos).is(Blocks.ICE) && NorthstarSurfaceFreezeGuard.shouldRestoreFrozenWater(level, pos)) {
            level.setBlockAndUpdate(pos, Blocks.WATER.defaultBlockState());
        }
    }
}
