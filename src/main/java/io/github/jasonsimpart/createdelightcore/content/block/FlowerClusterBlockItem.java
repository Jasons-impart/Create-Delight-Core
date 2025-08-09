package io.github.jasonsimpart.createdelightcore.content.block;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import vectorwing.farmersdelight.common.block.MushroomColonyBlock;

import javax.annotation.Nullable;

public class FlowerClusterBlockItem extends BlockItem
{
    public FlowerClusterBlockItem(Block blockIn, Properties properties) {
        super(blockIn, properties);
    }

    @Override
    @Nullable
    protected BlockState getPlacementState(BlockPlaceContext context) {
        BlockState originalState = this.getBlock().getStateForPlacement(context);
        if (originalState != null) {
            BlockState matureState = originalState.setValue(MushroomColonyBlock.COLONY_AGE, 2);
            return this.canPlace(context, matureState) ? matureState : null;
        }
        return null;
    }
}