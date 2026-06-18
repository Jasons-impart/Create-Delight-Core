package io.github.jasonsimpart.createdelightcore.mixin.eclipticseason;

import com.teamtea.eclipticseasons.common.block.GreenHouseCoreBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = GreenHouseCoreBlock.class, remap = false)
public class GreenHouseCoreBlockMixin {
    @Inject(method = {"getSignal", "m_6378_"}, at = @At("HEAD"), cancellable = true, require = 0)
    private void disableSeasonRedstoneSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction, CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(0);
    }
}
