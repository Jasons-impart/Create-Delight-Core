package io.github.jasonsimpart.createdelightcore.content.block;

import com.simibubi.create.content.decoration.encasing.CasingBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

public class GlassCassing extends CasingBlock {
    public GlassCassing(Properties p_i48440_1_) {
        super(p_i48440_1_);
    }

    @Override
    public boolean skipRendering(BlockState pState, BlockState pAdjacentBlockState, Direction side) {
        return ((pState.getBlock() instanceof GlassCassing) && (pAdjacentBlockState.getBlock() instanceof GlassCassing));
    }

    @Override
    public boolean propagatesSkylightDown(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
        return true;
    }

    @Override
    public float getShadeBrightness(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
        return 1.0F;
    }
}
