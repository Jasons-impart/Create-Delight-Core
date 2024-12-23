package io.github.jasonsimpart.createdelightcore.content.block;

import com.github.alexmodguy.alexscaves.client.particle.ACParticleRegistry;
import com.github.alexmodguy.alexscaves.server.entity.item.FallingGuanoEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Fallable;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class CoinPileBlock extends SnowLayerBlock implements Fallable {

    public CoinPileBlock(Properties properties) {
        super(properties);
    }

    public boolean canSurvive(BlockState state, LevelReader levelReader, BlockPos pos) {
        return true;
    }

    @NotNull
    public BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState1, LevelAccessor levelAccessor, BlockPos pos, BlockPos pos1) {
        levelAccessor.scheduleTick(pos, this, this.getDelayAfterPlace());
        return super.updateShape(blockState, direction, blockState1, levelAccessor, pos, pos1);
    }

    @Override
    public boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        if (!context.getItemInHand().isEmpty() && context.getItemInHand().is(this.asItem())) {
            return state.getBlock() instanceof SnowLayerBlock && state.getValue(LAYERS) < 8;
        } else {
            return false;
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel blockState, BlockPos blockPos, RandomSource randomSource) {
        if (isFree(blockState.getBlockState(blockPos.below())) && blockPos.getY() >= blockState.getMinBuildHeight()) {
            FallingGuanoEntity.fall(blockState, blockPos, state);
        }

    }

    public static boolean isFree(BlockState belowState) {
        return belowState.getBlock() instanceof SnowLayerBlock && belowState.getValue(LAYERS) < 8 || FallingBlock.isFree(belowState);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState blockState, boolean b) {
        level.scheduleTick(pos, this, this.getDelayAfterPlace());
    }

    public void onBrokenAfterFall(Level level, BlockPos fallenOn, FallingBlockEntity fallingBlockEntity) {
    }

    protected int getDelayAfterPlace() {
        return 2;
    }

    @Override
    public void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
    }

    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState blockstate = context.getLevel().getBlockState(context.getClickedPos());
        if (blockstate.is(this)) {
            int i = blockstate.getValue(LAYERS);
            return blockstate.setValue(LAYERS, Math.min(8, i + 1));
        } else {
            return this.defaultBlockState();
        }
    }

    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource randomSource) {
        if (randomSource.nextInt(50) == 0) {
            Vec3 center = Vec3.upFromBottomCenterOf(pos, 1.0F).add(randomSource.nextFloat() - 0.5F, randomSource.nextFloat() * 0.5F + 0.2F, randomSource.nextFloat() - 0.5F);
            level.addParticle(ACParticleRegistry.FLY.get(), center.x, center.y, center.z, center.x, center.y, center.z);
        }

    }

}