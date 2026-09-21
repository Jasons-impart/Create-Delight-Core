package io.github.jasonsimpart.compat.create;

import com.simibubi.create.infrastructure.config.AllConfigs;
import com.simibubi.create.api.behaviour.movement.MovementBehaviour;
import com.simibubi.create.content.contraptions.actors.harvester.HarvesterMovementBehaviour;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.foundation.utility.BlockHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.satisfy.vinery.core.block.GrapeBush;
import net.satisfy.vinery.core.block.GrapeVineBlock;
import net.satisfy.vinery.core.block.StemBlock;
import net.satisfy.vinery.core.registry.GrapeTypeRegistry;
import net.satisfy.vinery.core.registry.ObjectRegistry;
import net.minecraft.world.level.block.Block;

public class VineryHarvesterBehaviour extends HarvesterMovementBehaviour {
    public static void registerBlocks() {
        var behaviour = new VineryHarvesterBehaviour();
        Block[] blocks = {
                ObjectRegistry.RED_GRAPE_BUSH.get(),
                ObjectRegistry.WHITE_GRAPE_BUSH.get(),
                ObjectRegistry.SAVANNA_RED_GRAPE_BUSH.get(),
                ObjectRegistry.SAVANNA_WHITE_GRAPE_BUSH.get(),
                ObjectRegistry.TAIGA_RED_GRAPE_BUSH.get(),
                ObjectRegistry.TAIGA_WHITE_GRAPE_BUSH.get(),
                ObjectRegistry.JUNGLE_RED_GRAPE_BUSH.get(),
                ObjectRegistry.JUNGLE_WHITE_GRAPE_BUSH.get(),
                ObjectRegistry.GRAPEVINE_STEM.get(),
        };
        for (Block block : blocks) {
            MovementBehaviour.REGISTRY.register(block, behaviour);
        }
    }

    @Override
    public void visitNewPosition(MovementContext context, BlockPos pos) {
        Level level = context.world;
        if (level.isClientSide()) {
            return;
        }
        BlockState state = level.getBlockState(pos);
        boolean replant = AllConfigs.server().kinetics.harvesterReplants.get();
        boolean partial = AllConfigs.server().kinetics.harvestPartiallyGrown.get();
        if (state.getBlock() instanceof GrapeVineBlock) {
            harvestGrapeVine(context, pos, state, replant, partial);
        } else if (state.getBlock() instanceof StemBlock) {
            harvestGrapeStem(context, pos, state, replant, partial);
        } else if (state.getBlock() instanceof GrapeBush) {
            harvestGrapeBush(context, pos, state, replant, partial);
        }
    }

    private void harvestGrapeBush(MovementContext context, BlockPos pos, BlockState state, boolean replant, boolean partial) {
        if (!(state.getBlock() instanceof GrapeBush bush)) {
            return;
        }
        int age = state.getValue(GrapeBush.AGE);
        if (age <= 0 || (!partial && age < 3)) {
            return;
        }
        Level level = context.world;
        if (replant) {
            level.playSound(null, pos, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.BLOCKS, 1.0F, 0.8F + level.random.nextFloat() * 0.4F);
            level.setBlock(pos, state.setValue(GrapeBush.AGE, 1), 2);
        } else {
            BlockHelper.destroyBlock(level, pos, 1, stack -> {
            });
        }
        collectOrDropItem(context, new ItemStack(bush.type.getFruit(), level.random.nextInt(2) + 1));
    }

    private void harvestGrapeVine(MovementContext context, BlockPos pos, BlockState state, boolean replant, boolean partial) {
        if (!(state.getBlock() instanceof GrapeVineBlock vine)) {
            return;
        }
        int age = state.getValue(GrapeVineBlock.AGE);
        if (age <= 0 || (!partial && age < 3)) {
            return;
        }
        Level level = context.world;
        if (replant) {
            level.playSound(null, pos, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.BLOCKS, 1.0F, 0.8F + level.random.nextFloat() * 0.4F);
            level.setBlock(pos, state.setValue(GrapeVineBlock.AGE, 1), 2);
        } else {
            BlockHelper.destroyBlock(level, pos, 1, stack -> {
            });
        }
        collectOrDropItem(context, new ItemStack(vine.type.getFruit(), level.random.nextInt(2) + 1));
    }

    private void harvestGrapeStem(MovementContext context, BlockPos pos, BlockState state, boolean replant, boolean partial) {
        if (!(state.getBlock() instanceof StemBlock stem)) {
            return;
        }
        int age = state.getValue(StemBlock.AGE);
        if (age <= 0 || (!partial && age < 4)) {
            return;
        }
        Level level = context.world;
        boolean mature = stem.isMature(state);
        collectOrDropItem(context, new ItemStack(state.getValue(StemBlock.GRAPE).getFruit(), 1 + level.random.nextInt(mature ? 2 : 1) + (mature ? 2 : 1)));
        if (replant) {
            level.playSound(null, pos, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.BLOCKS, 1.0F, 0.8F + level.random.nextFloat() * 0.4F);
            level.setBlock(pos, state.setValue(StemBlock.AGE, 2), 2);
        } else {
            collectOrDropItem(context, new ItemStack(state.getValue(StemBlock.GRAPE).getSeeds(), 1));
            level.setBlock(pos, state.setValue(StemBlock.GRAPE, GrapeTypeRegistry.NONE), 2);
        }
    }
}
