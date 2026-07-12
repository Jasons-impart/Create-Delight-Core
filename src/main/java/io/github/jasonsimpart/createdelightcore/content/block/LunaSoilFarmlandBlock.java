package io.github.jasonsimpart.createdelightcore.content.block;

import io.github.jasonsimpart.createdelightcore.CDConfig;
import io.github.jasonsimpart.createdelightcore.registry.CDBlocks;
import io.github.jasonsimpart.createdelightcore.registry.CDTags;
import net.jadenxgamer.netherexp.registry.fluid.JNEFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.common.ForgeHooks;
import vectorwing.farmersdelight.common.block.RichSoilFarmlandBlock;
import vectorwing.farmersdelight.common.tag.ModTags;
import vectorwing.farmersdelight.common.utility.MathUtils;

import javax.annotation.Nullable;

public class LunaSoilFarmlandBlock extends RichSoilFarmlandBlock {
    public LunaSoilFarmlandBlock(Properties properties) {
        super(properties);
    }


    private static boolean hasMoistSource(LevelReader level, BlockPos pos) {
        for (BlockPos nearbyPos : BlockPos.betweenClosed(pos.offset(-4, 0, -4), pos.offset(4, 1, 4))) {
            if (level.getFluidState(nearbyPos).is(JNEFluids.ECTOPLASM_SOURCE.get())) {
                return true;
            }
        }
        return false;
    }

    public static void turnToLunaSoil(@Nullable Entity entity, BlockState state, Level level, BlockPos pos) {
        level.setBlockAndUpdate(pos, pushEntitiesUp(state, CDBlocks.LUNA_SOIL.get().defaultBlockState(), level, pos));
        level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(entity, state));
    }

    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockState aboveState = level.getBlockState(pos.above());
        return super.canSurvive(state, level, pos) || aboveState.getBlock() instanceof StemGrownBlock;
    }

    public boolean isFertile(BlockState state, BlockGetter world, BlockPos pos) {
        if (state.is(CDBlocks.LUNA_SOIL_FARMLAND.get())) {
            return state.getValue(MOISTURE) > 0;
        } else {
            return false;
        }
    }


    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource rand) {
        if (!state.canSurvive(level, pos)) {
            turnToLunaSoil(null, state, level, pos);
        }
    }


    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int moisture = state.getValue(MOISTURE);
        if (!hasMoistSource(level, pos)) {
            if (moisture > 0) {
                level.setBlock(pos, state.setValue(MOISTURE, moisture - 1), 2);
            }
        } else if (moisture < 7) {
            level.setBlock(pos, state.setValue(MOISTURE, 7), 2);
        } else if (moisture == 7) {
            if (CDConfig.lunaSoilBoostChance == 0.0) {
                return;
            }

            if (!CDTags.isAlienPlanet(level))
                return;

            BlockState aboveState = level.getBlockState(pos.above());
            Block aboveBlock = aboveState.getBlock();
            if (aboveState.is(ModTags.UNAFFECTED_BY_RICH_SOIL) || aboveBlock instanceof TallFlowerBlock) {
                return;
            }

            if (aboveBlock instanceof BonemealableBlock growable) {
                if ((double) MathUtils.RAND.nextFloat() <= CDConfig.lunaSoilBoostChance && growable.isValidBonemealTarget(level, pos.above(), aboveState, false) && ForgeHooks.onCropsGrowPre(level, pos.above(), aboveState, true)) {
                    growable.performBonemeal(level, level.random, pos.above(), aboveState);
                    if (!level.isClientSide) {
                        level.levelEvent(LevelEvent.PARTICLES_PLANT_GROWTH, pos.above(), 0);
                    }

                    ForgeHooks.onCropsGrowPost(level, pos.above(), aboveState);
                }
            }
        }

    }
}
