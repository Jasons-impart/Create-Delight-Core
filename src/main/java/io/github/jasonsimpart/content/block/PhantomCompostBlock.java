package io.github.jasonsimpart.content.block;

import io.github.jasonsimpart.Config;
import io.github.jasonsimpart.CreateDelightCore;
import io.github.jasonsimpart.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class PhantomCompostBlock extends Block {
    public static final IntegerProperty COMPOSTING = IntegerProperty.create("composting", 0, 5);
    private static final TagKey<Block> ACTIVATORS = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(CreateDelightCore.MODID, "phantom_compost_activators"));

    public PhantomCompostBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(COMPOSTING, 0));
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(COMPOSTING);
        super.createBlockStateDefinition(builder);
    }

    public int getMaxCompostingStage() {
        return 5;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        float chance = 0.0F;
        boolean hasEctoplasm = false;
        int maxLight = 0;

        for (BlockPos neighborPos : BlockPos.betweenClosed(pos.offset(-1, -1, -1), pos.offset(1, 1, 1))) {
            BlockState neighborState = level.getBlockState(neighborPos);
            if (neighborState.is(ACTIVATORS)) {
                chance += 0.02F;
            }

            if (neighborState.getFluidState().is(LunaSoilFarmlandBlock.NETHEREXP_ECTOPLASM)) {
                hasEctoplasm = true;
            }

            int light = level.getBrightness(LightLayer.SKY, neighborPos.above());
            if (light > maxLight) {
                maxLight = light;
            }
        }

        chance += maxLight > 12 ? 0.1F : 0.05F;
        if (hasEctoplasm) {
            chance += 0.1F;
        }

        if (random.nextFloat() > chance) {
            return;
        }

        int composting = state.getValue(COMPOSTING);
        if (composting >= getMaxCompostingStage()) {
            level.setBlock(pos, ModBlocks.LUNA_SOIL.get().defaultBlockState(), 3);
        } else {
            level.setBlock(pos, state.setValue(COMPOSTING, composting + 1), 3);
        }
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return getMaxCompostingStage() + 1 - state.getValue(COMPOSTING);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (Config.ENABLE_PHANTOM_COMPOST_PARTICLES.get() && random.nextInt(10) == 0) {
            level.addParticle(ParticleTypes.MYCELIUM, pos.getX() + random.nextFloat(), pos.getY() + 1.1D, pos.getZ() + random.nextFloat(), 0.0D, 0.0D, 0.0D);
        }
    }
}
