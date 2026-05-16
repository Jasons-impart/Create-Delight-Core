package io.github.jasonsimpart.content.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SyrupBlock extends HalfTransparentBlock {
    public static final BooleanProperty COVERED = BooleanProperty.create("covered");

    public SyrupBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(COVERED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(COVERED);
    }

    @Override
    public boolean isStickyBlock(BlockState state) {
        return true;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (state.getValue(COVERED)) {
            return Shapes.empty();
        }
        if (context.isAbove(Shapes.block(), pos, true) && !context.isDescending()) {
            return box(0, 15, 0, 16, 16, 16);
        }
        return Shapes.empty();
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (direction == Direction.UP) {
            return state.setValue(COVERED, !level.getBlockState(pos.above()).isAir());
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        if (!state.is(oldState.getBlock())) {
            level.setBlock(pos, updateShape(state, Direction.UP, level.getBlockState(pos.above()), level, pos, pos.above()), 3);
        }
        super.onPlace(state, level, pos, oldState, movedByPiston);
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        double yMovement = Math.abs(entity.getDeltaMovement().y);
        if (yMovement < 0.1 && !entity.isSteppingCarefully()) {
            double horizontalMultiplier = 0.4 + yMovement * 0.2;
            entity.setDeltaMovement(entity.getDeltaMovement().multiply(horizontalMultiplier, 1.0F, horizontalMultiplier));
        }
        super.stepOn(level, pos, state, entity);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        entity.makeStuckInBlock(state, new Vec3(0.5D, 0.5D, 0.5D));
    }
}
