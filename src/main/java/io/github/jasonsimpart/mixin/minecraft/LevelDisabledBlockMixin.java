package io.github.jasonsimpart.mixin.minecraft;

import io.github.jasonsimpart.compat.northstar.NorthstarSurfaceFreezeGuard;
import io.github.jasonsimpart.disabled.DisabledContentManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Level.class)
public abstract class LevelDisabledBlockMixin {
    @ModifyVariable(method = "setBlock", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private BlockState createdelightcore$replaceDisabledBlock(BlockState state, BlockPos pos) {
        Level level = (Level) (Object) this;
        if (level.isClientSide) {
            return state;
        }

        return DisabledContentManager.replacementFor(keepEnceladusDeepWaterLiquid(state, level, pos), level, pos);
    }

    private static BlockState keepEnceladusDeepWaterLiquid(BlockState state, Level level, BlockPos pos) {
        if (!state.is(Blocks.ICE) && !removesWaterlogging(state)) {
            return state;
        }

        if (!NorthstarSurfaceFreezeGuard.shouldKeepWaterLiquid(level, pos)) {
            return state;
        }

        BlockState currentState = level.getBlockState(pos);
        if (!currentState.getFluidState().is(Fluids.WATER)) {
            return state;
        }

        if (state.is(Blocks.ICE) || removesWaterlogging(currentState, state)) {
            return currentState;
        }

        return state;
    }

    private static boolean removesWaterlogging(BlockState nextState) {
        return nextState.hasProperty(BlockStateProperties.WATERLOGGED)
                && !nextState.getValue(BlockStateProperties.WATERLOGGED);
    }

    private static boolean removesWaterlogging(BlockState currentState, BlockState nextState) {
        return currentState.hasProperty(BlockStateProperties.WATERLOGGED)
                && nextState.hasProperty(BlockStateProperties.WATERLOGGED)
                && currentState.getValue(BlockStateProperties.WATERLOGGED)
                && !nextState.getValue(BlockStateProperties.WATERLOGGED);
    }
}
