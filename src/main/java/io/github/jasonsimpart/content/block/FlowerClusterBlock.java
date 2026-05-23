package io.github.jasonsimpart.content.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.util.TriState;
import vectorwing.farmersdelight.common.block.MushroomColonyBlock;

public class FlowerClusterBlock extends MushroomColonyBlock {
    public FlowerClusterBlock(Holder<Item> flowerItem, Properties properties) {
        super(flowerItem, properties);
    }

    @Override
    public int getMaxAge() {
        return 2;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos floorPos = pos.below();
        BlockState floorState = level.getBlockState(floorPos);
        TriState sustainPlant = floorState.canSustainPlant(level, floorPos, Direction.UP, state);
        return floorState.is(BlockTags.MUSHROOM_GROW_BLOCK)
                || sustainPlant.isTrue()
                || sustainPlant.isDefault() && mayPlaceOn(floorState, level, floorPos);
    }
}
