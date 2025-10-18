package io.github.jasonsimpart.createdelightcore.content.block;

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

    public SyrupBlock(Properties pProperties) {
        super(pProperties);
        this.registerDefaultState((this.stateDefinition.any()).setValue(COVERED, false));
    }
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(COVERED);
    }

    public boolean isStickyBlock(BlockState state) {
        return true;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext context) {
        if (state.getValue(COVERED)) {
            return Shapes.empty();
        } else if (context.isAbove(Shapes.block(), pos, true) && !context.isDescending()) {
            return Block.box(0, 15, 0, 16, 16, 16);
        }
        return Shapes.empty();
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState state1, LevelAccessor level, BlockPos pos, BlockPos pos1) {
        if (direction == Direction.UP) {
            boolean isEmpty = level.getBlockState(pos.above()).isAir();
            return state.setValue(COVERED, !isEmpty);
        }
        return super.updateShape(state, direction, state1, level, pos, pos1);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState state1, boolean b) {
        if (!state.is(state1.getBlock())) {
            level.setBlock(pos, this.updateShape(state, Direction.UP, level.getBlockState(pos.above()), level, pos, pos.above()), 3);
        }
        super.onPlace(state, level, pos, state1, b);
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        double vec3 = Math.abs(entity.getDeltaMovement().y);
        if (vec3 < 0.1 && !entity.isSteppingCarefully()) {
            double newVec3 = 0.4 + vec3 * 0.2;
            entity.setDeltaMovement(entity.getDeltaMovement().multiply(newVec3, 1.0F, newVec3));
        }

        super.stepOn(level, pos, state, entity);
    }

    @Override
    public void entityInside(BlockState state, Level world, BlockPos pos, Entity entity) {
        entity.makeStuckInBlock(state, new Vec3(0.5D, 0.5D, 0.5D));
    }

}
