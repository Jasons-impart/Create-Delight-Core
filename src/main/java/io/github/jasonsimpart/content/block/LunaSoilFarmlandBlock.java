package io.github.jasonsimpart.content.block;

import io.github.jasonsimpart.CreateDelightCore;
import io.github.jasonsimpart.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.TallFlowerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import vectorwing.farmersdelight.common.block.RichSoilFarmlandBlock;

public class LunaSoilFarmlandBlock extends RichSoilFarmlandBlock {
    static final TagKey<Fluid> NETHEREXP_ECTOPLASM = TagKey.create(Registries.FLUID, ResourceLocation.fromNamespaceAndPath(CreateDelightCore.MODID, "compat/netherexp_ectoplasm"));

    public LunaSoilFarmlandBlock(Properties properties) {
        super(properties);
    }

    private static boolean hasMoistSource(LevelReader level, BlockPos pos) {
        for (BlockPos nearbyPos : BlockPos.betweenClosed(pos.offset(-4, 0, -4), pos.offset(4, 1, 4))) {
            if (level.getFluidState(nearbyPos).is(NETHEREXP_ECTOPLASM)) {
                return true;
            }
        }
        return false;
    }

    public static void turnToLunaSoil(Entity entity, BlockState state, Level level, BlockPos pos) {
        level.setBlockAndUpdate(pos, pushEntitiesUp(state, ModBlocks.LUNA_SOIL.get().defaultBlockState(), level, pos));
        level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(entity, state));
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockState aboveState = level.getBlockState(pos.above());
        return super.canSurvive(state, level, pos) || aboveState.is(Blocks.MELON) || aboveState.is(Blocks.PUMPKIN);
    }

    @Override
    public boolean isFertile(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(ModBlocks.LUNA_SOIL_FARMLAND.get()) && state.getValue(MOISTURE) > 0;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        return state != null && state.is(ModBlocks.LUNA_SOIL_FARMLAND.get())
                ? state
                : ModBlocks.LUNA_SOIL.get().defaultBlockState();
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!state.canSurvive(level, pos)) {
            turnToLunaSoil(null, state, level, pos);
        }
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int moisture = state.getValue(MOISTURE);
        if (!hasMoistSource(level, pos)) {
            if (moisture > 0) {
                level.setBlock(pos, state.setValue(MOISTURE, moisture - 1), 2);
            }
            return;
        }

        if (moisture < 7) {
            level.setBlock(pos, state.setValue(MOISTURE, 7), 2);
            return;
        }

        if (!LunaSoilBlock.isNorthstarDimension(level)) {
            return;
        }

        BlockPos abovePos = pos.above();
        BlockState aboveState = level.getBlockState(abovePos);
        if (aboveState.is(LunaSoilBlock.UNAFFECTED_BY_RICH_SOIL) || aboveState.getBlock() instanceof TallFlowerBlock) {
            return;
        }

        LunaSoilBlock.tryBoostCrop(level, abovePos, aboveState, random, LevelEvent.PARTICLES_AND_SOUND_PLANT_GROWTH);
    }
}
