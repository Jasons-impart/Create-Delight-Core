package io.github.jasonsimpart.createdelightcore.compat.kaleidoscopetavern;

import com.github.ysbbbbbb.kaleidoscopetavern.blockentity.brew.DrinkBlockEntity;
import com.github.ysbbbbbb.kaleidoscopetavern.item.BottleBlockItem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/** Deliver the original assembled serving to the same ground destinations as the native tap. */
public final class TavernBarrelServing {
    private TavernBarrelServing() {}

    public static boolean intoPlacedCarrier(Level level, BlockPos pos, BlockState carrier, ItemStack filled) {
        if (level.getBlockState(pos) != carrier) return false;
        if (filled.getItem() instanceof BlockItem block) return place(level, pos, carrier, block, filled);

        ItemEntity dropped = spawn(level, pos, filled);
        if (dropped == null) return false;
        if (level.removeBlock(pos, false)) return true;
        dropped.discard();
        return false;
    }

    public static boolean intoDroppedCarrier(Level level, BlockPos pos, ItemEntity carrier, ItemStack filled) {
        if (!carrier.isAlive() || carrier.getItem().isEmpty()) return false;
        var below = level.getBlockState(pos);
        boolean delivered = below.isAir() && filled.getItem() instanceof BlockItem block
                ? place(level, pos, below, block, filled) : spawn(level, pos, filled) != null;
        if (!delivered) return false;

        ItemStack remaining = carrier.getItem().copy();
        remaining.shrink(1);
        if (remaining.isEmpty()) carrier.discard();
        else carrier.setItem(remaining);
        return true;
    }

    private static boolean place(Level level, BlockPos pos, BlockState previous, BlockItem item, ItemStack filled) {
        var state = item.getBlock().defaultBlockState();
        var facing = BlockStateProperties.HORIZONTAL_FACING;
        if (state.hasProperty(facing) && previous.hasProperty(facing)) state = state.setValue(facing, previous.getValue(facing));
        if (!level.setBlockAndUpdate(pos, state)) return false;
        if (!(item instanceof BottleBlockItem)) return true;
        if (level.getBlockEntity(pos) instanceof DrinkBlockEntity drink && drink.addItem(filled)) {
            drink.refresh();
            return true;
        }
        level.setBlockAndUpdate(pos, previous);
        return false;
    }

    private static ItemEntity spawn(Level level, BlockPos pos, ItemStack stack) {
        var entity = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack);
        entity.setDefaultPickUpDelay();
        return level.addFreshEntity(entity) ? entity : null;
    }
}
