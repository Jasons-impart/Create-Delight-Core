package io.github.jasonsimpart.createdelightcore.content.block;

import de.cadentem.quality_food.capability.LevelData;
import de.cadentem.quality_food.util.DropData;
import io.github.jasonsimpart.createdelightcore.registry.CDBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.ToolActions;
import vectorwing.farmersdelight.common.utility.ItemUtils;

import java.util.function.Supplier;

public class FlowerClusterBlock extends BushBlock implements BonemealableBlock {
    public static final IntegerProperty CLUSTER_AGE = IntegerProperty.create("age", 0, 2);

    protected static final VoxelShape[] SHAPE_BY_AGE = new VoxelShape[]{
            Block.box(4.0D, 0.0D, 4.0D, 12.0D, 8.0D, 12.0D),
            Block.box(3.0D, 0.0D, 3.0D, 13.0D, 10.0D, 13.0D),
            Block.box(2.0D, 0.0D, 2.0D, 14.0D, 12.0D, 14.0D)
    };

    public final Supplier<Item> flowerType;

    public FlowerClusterBlock(Properties properties, Supplier<Item> flowerType) {
        super(properties);
        this.flowerType = flowerType;
        this.registerDefaultState(this.stateDefinition.any().setValue(CLUSTER_AGE, 0));
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        int age = state.getValue(CLUSTER_AGE);
        ItemStack heldStack = player.getItemInHand(hand);
        if (age <= 0) {
            return InteractionResult.PASS;
        }

        ItemStack dropStack = this.getCloneItemStack(level, pos, state);
        if (ItemUtils.isValidTool(heldStack, ToolActions.SHEARS_HARVEST, Tags.Items.SHEARS)) {
            level.setBlock(pos, state.setValue(CLUSTER_AGE, age - 1), 2);
            level.playSound(null, pos, SoundEvents.SHEEP_SHEAR, SoundSource.BLOCKS, 1.0F, 1.0F);
            popResourceWithQualityData(state, level, pos, player, dropStack);
            if (!level.isClientSide) {
                heldStack.hurtAndBreak(1, player, user -> user.broadcastBreakEvent(hand));
                ((ServerLevel) level).sendParticles(new net.minecraft.core.particles.BlockParticleOption(net.minecraft.core.particles.ParticleTypes.BLOCK, state),
                        (double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D, (double) pos.getZ() + 0.5D,
                        3, 0.1D, 0.1D, 0.1D, 0.001D);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if (ItemUtils.isKnife(heldStack)) {
            dropStack.setCount(age);
            level.setBlock(pos, state.setValue(CLUSTER_AGE, 0), 2);
            level.playSound(null, pos, this.soundType.getBreakSound(), SoundSource.BLOCKS, 1.0F, 1.0F);
            popResourceWithQualityData(state, level, pos, player, dropStack);
            if (!level.isClientSide) {
                heldStack.hurtAndBreak(1, player, user -> user.broadcastBreakEvent(hand));
                ((ServerLevel) level).sendParticles(new net.minecraft.core.particles.BlockParticleOption(net.minecraft.core.particles.ParticleTypes.BLOCK, state),
                        (double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D, (double) pos.getZ() + 0.5D,
                        10, 0.2D, 0.2D, 0.2D, 0.1D);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        return InteractionResult.PASS;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE_BY_AGE[state.getValue(CLUSTER_AGE)];
    }

    public int getMaxAge() {
        return 2;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return isLunaSoil(level.getBlockState(pos.below()));
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int age = state.getValue(CLUSTER_AGE);
        BlockState floorState = level.getBlockState(pos.below());
        if (age < this.getMaxAge()
                && isLunaSoil(floorState)
                && ForgeHooks.onCropsGrowPre(level, pos, state, random.nextInt(4) == 0)) {
            level.setBlock(pos, state.setValue(CLUSTER_AGE, age + 1), 2);
            ForgeHooks.onCropsGrowPost(level, pos, state);
        }
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        return new ItemStack(this.flowerType.get());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(CLUSTER_AGE);
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state, boolean isClient) {
        return state.getValue(CLUSTER_AGE) < this.getMaxAge();
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        int age = Math.min(this.getMaxAge(), state.getValue(CLUSTER_AGE) + this.getBonemealAgeIncrease(level));
        level.setBlock(pos, state.setValue(CLUSTER_AGE, age), 2);
    }

    protected int getBonemealAgeIncrease(Level level) {
        return Mth.nextInt(level.random, 1, 2);
    }

    private static boolean isLunaSoil(BlockState state) {
        return state.is(CDBlocks.LUNA_SOIL.get()) || state.is(CDBlocks.LUNA_SOIL_FARMLAND.get());
    }

    private static void popResourceWithQualityData(BlockState state, Level level, BlockPos pos, Player player, ItemStack dropStack) {
        DropData.CURRENT.set(new DropData(LevelData.get(level, pos, true), pos, state, player, level.getBlockState(pos.below())));
        try {
            popResource(level, pos, dropStack);
        } finally {
            DropData.CURRENT.remove();
        }
    }
}
