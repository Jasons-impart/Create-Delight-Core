package io.github.jasonsimpart.content.block;

import com.iafenvoy.iceandfire.registry.IafBlocks;
import io.github.jasonsimpart.Config;
import io.github.jasonsimpart.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;

public class LunaSoilBlock extends Block {
    private static final String NORTHSTAR_MOD_ID = "northstar";
    static final TagKey<Block> UNAFFECTED_BY_RICH_SOIL = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("farmersdelight", "unaffected_by_rich_soil"));

    public LunaSoilBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        BlockPos abovePos = pos.above();
        BlockState aboveState = level.getBlockState(abovePos);

        if (aboveState.is(UNAFFECTED_BY_RICH_SOIL) || !isNorthstarDimension(level)) {
            return;
        }

        Block aboveBlock = aboveState.getBlock();
        if (aboveBlock == IafBlocks.FIRE_LILY.value()) {
            level.setBlockAndUpdate(abovePos, ModBlocks.FIRE_LILY_CLUSTER.get().defaultBlockState());
            return;
        }

        if (aboveBlock == IafBlocks.FROST_LILY.value()) {
            level.setBlockAndUpdate(abovePos, ModBlocks.FROST_LILY_CLUSTER.get().defaultBlockState());
            return;
        }

        if (aboveBlock == IafBlocks.LIGHTNING_LILY.value()) {
            level.setBlockAndUpdate(abovePos, ModBlocks.LIGHTNING_LILY_CLUSTER.get().defaultBlockState());
            return;
        }

        tryBoostCrop(level, abovePos, aboveState, random, 2005);
    }

    @Override
    public BlockState getToolModifiedState(BlockState state, UseOnContext context, ItemAbility itemAbility, boolean simulate) {
        return itemAbility == ItemAbilities.HOE_TILL && context.getLevel().getBlockState(context.getClickedPos().above()).isAir()
                ? ModBlocks.LUNA_SOIL_FARMLAND.get().defaultBlockState()
                : null;
    }

    static boolean isNorthstarDimension(LevelReader level) {
        return level instanceof ServerLevel serverLevel
                && serverLevel.dimension().location().getNamespace().equals(NORTHSTAR_MOD_ID);
    }

    static boolean tryBoostCrop(ServerLevel level, BlockPos pos, BlockState state, RandomSource random, int levelEvent) {
        double chance = Config.LUNA_SOIL_BOOST_CHANCE.get();
        if (chance <= 0.0D || random.nextDouble() > chance || !(state.getBlock() instanceof BonemealableBlock growable)) {
            return false;
        }

        if (growable.isValidBonemealTarget(level, pos, state)
                && CommonHooks.canCropGrow(level, pos, state, true)) {
            growable.performBonemeal(level, random, pos, state);
            level.levelEvent(levelEvent, pos, 0);
            CommonHooks.fireCropGrowPost(level, pos, state);
            return true;
        }
        return false;
    }
}
