package io.github.jasonsimpart.createdelightcore.content.block;

import com.github.alexthe666.iceandfire.block.IafBlockRegistry;
import earth.terrarium.adastra.AdAstra;
import io.github.jasonsimpart.createdelightcore.CDConfig;
import io.github.jasonsimpart.createdelightcore.registry.CDBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import vectorwing.farmersdelight.common.tag.ModTags;
import vectorwing.farmersdelight.common.utility.MathUtils;

import javax.annotation.Nullable;

public class LunaSoilBlock extends Block {
    public LunaSoilBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource rand) {
        if (!level.isClientSide) {
            BlockPos abovePos = pos.above();
            BlockState aboveState = level.getBlockState(abovePos);
            Block aboveBlock = aboveState.getBlock();
            if (aboveState.is(ModTags.UNAFFECTED_BY_RICH_SOIL)) {
                return;
            }

            if (!level.dimension().location().getNamespace().equals(AdAstra.MOD_ID))
                return;

            if (aboveBlock == IafBlockRegistry.FIRE_LILY.get()) {
                level.setBlockAndUpdate(pos.above(), CDBlocks.FIRE_LILY_CLUSTER.get().defaultBlockState());
                return;
            }

            if (aboveBlock == IafBlockRegistry.FROST_LILY.get()) {
                level.setBlockAndUpdate(pos.above(), CDBlocks.FROST_LILY_CLUSTER.get().defaultBlockState());
                return;
            }

            if (aboveBlock == IafBlockRegistry.LIGHTNING_LILY.get()) {
                level.setBlockAndUpdate(pos.above(), CDBlocks.LIGHTNING_LILY_CLUSTER.get().defaultBlockState());
                return;
            }

            if (CDConfig.lunaSoilBoostChance == 0.0) {
                return;
            }

            if (aboveBlock instanceof BonemealableBlock growable) {
                if ((double) MathUtils.RAND.nextFloat() <= CDConfig.lunaSoilBoostChance && growable.isValidBonemealTarget(level, pos.above(), aboveState, false) && ForgeHooks.onCropsGrowPre(level, pos.above(), aboveState, true)) {
                    growable.performBonemeal(level, level.random, pos.above(), aboveState);
                    level.levelEvent(2005, pos.above(), 0);
                    ForgeHooks.onCropsGrowPost(level, pos.above(), aboveState);
                }
            }
        }

    }

    @Nullable
    public BlockState getToolModifiedState(BlockState state, UseOnContext context, ToolAction toolAction, boolean simulate) {
        return toolAction.equals(ToolActions.HOE_TILL) && context.getLevel().getBlockState(context.getClickedPos().above()).isAir() ? CDBlocks.LUNA_SOIL_FARMLAND.get().defaultBlockState() : null;
    }
}
