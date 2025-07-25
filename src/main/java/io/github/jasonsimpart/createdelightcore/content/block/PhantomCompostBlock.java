package io.github.jasonsimpart.createdelightcore.content.block;

import io.github.jasonsimpart.createdelightcore.registry.CDBlocks;
import io.github.jasonsimpart.createdelightcore.registry.CDTags;
import net.jadenxgamer.netherexp.registry.fluid.JNEFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.NotNull;
import vectorwing.farmersdelight.common.registry.ModBlocks;
import vectorwing.farmersdelight.common.tag.ModTags;

import java.util.Iterator;

public class PhantomCompostBlock extends Block {
    public static IntegerProperty COMPOSTING = IntegerProperty.create("composting", 0, 5);

    public PhantomCompostBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(super.defaultBlockState().setValue(COMPOSTING, 0));
    }

    public boolean isRandomlyTicking(@NotNull BlockState state) {
        return true;
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(COMPOSTING);
        super.createBlockStateDefinition(builder);
    }

    public int getMaxCompostingStage() {
        return 5;
    }

    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!level.isClientSide) {
            float chance = 0.0F;
            boolean hasEctoplasm = false;
            int maxLight = 0;

            for (BlockPos neighborPos : BlockPos.betweenClosed(pos.offset(-1, -1, -1), pos.offset(1, 1, 1))) {
                BlockState neighborState = level.getBlockState(neighborPos);
                if (neighborState.is(CDTags.AllBlockTags.PHANTOM_COMPOST_ACTIVATORS.tag)) {
                    chance += 0.02F;
                }

                if (neighborState.getFluidState().is(JNEFluids.ECTOPLASM_SOURCE.get())) {
                    hasEctoplasm = true;
                }

                int light = level.getBrightness(LightLayer.SKY, neighborPos.above());
                if (light > maxLight) {
                    maxLight = light;
                }
            }

            chance += maxLight > 12 ? 0.1F : 0.05F;
            chance += hasEctoplasm ? 0.1F : 0.0F;
            if (level.getRandom().nextFloat() <= chance) {
                if (state.getValue(COMPOSTING) == this.getMaxCompostingStage()) {
                    level.setBlock(pos, CDBlocks.LUNA_SOIL.get().defaultBlockState(), 3);
                } else {
                    level.setBlock(pos, state.setValue(COMPOSTING, state.getValue(COMPOSTING) + 1), 3);
                }
            }

        }
    }

    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    public int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos pos) {
        return this.getMaxCompostingStage() + 1 - blockState.getValue(COMPOSTING);
    }

    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (random.nextInt(10) == 0) {
            level.addParticle(ParticleTypes.MYCELIUM, (double)pos.getX() + (double)random.nextFloat(), (double)pos.getY() + 1.1, (double)pos.getZ() + (double)random.nextFloat(), 0.0, 0.0, 0.0);
        }

    }
}
