package io.github.jasonsimpart.content.item;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import vectorwing.farmersdelight.common.block.MushroomColonyBlock;

public class FlowerClusterBlockItem extends BlockItem {
    public FlowerClusterBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    @Nullable
    protected BlockState getPlacementState(BlockPlaceContext context) {
        BlockState state = getBlock().getStateForPlacement(context);
        if (state == null) {
            return null;
        }

        BlockState matureState = state.setValue(MushroomColonyBlock.COLONY_AGE, 2);
        return canPlace(context, matureState) ? matureState : null;
    }
}
