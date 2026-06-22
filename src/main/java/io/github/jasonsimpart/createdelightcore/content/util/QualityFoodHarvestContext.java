package io.github.jasonsimpart.createdelightcore.content.util;

import de.cadentem.quality_food.capability.LevelData;
import de.cadentem.quality_food.util.QualityUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public final class QualityFoodHarvestContext {
    private static final ThreadLocal<BlockPos> CURRENT_CROP_POS = new ThreadLocal<>();
    private static final ThreadLocal<HarvestData> CURRENT_HARVEST = new ThreadLocal<>();

    private QualityFoodHarvestContext() {
    }

    public static @Nullable BlockPos getCropPos() {
        HarvestData harvestData = CURRENT_HARVEST.get();
        if (harvestData != null) {
            return harvestData.pos();
        }
        return CURRENT_CROP_POS.get();
    }

    public static @Nullable BlockPos push(BlockPos pos) {
        BlockPos previous = CURRENT_CROP_POS.get();
        CURRENT_CROP_POS.set(pos.immutable());
        return previous;
    }

    public static void pop(@Nullable BlockPos previous) {
        if (previous == null) {
            CURRENT_CROP_POS.remove();
            return;
        }
        CURRENT_CROP_POS.set(previous);
    }

    public static @Nullable HarvestData push(ServerPlayer player, BlockPos pos, BlockState state) {
        HarvestData previous = CURRENT_HARVEST.get();
        CURRENT_HARVEST.set(new HarvestData(player, pos.immutable(), state));
        return previous;
    }

    public static void pop(@Nullable HarvestData previous) {
        if (previous == null) {
            CURRENT_HARVEST.remove();
            return;
        }
        CURRENT_HARVEST.set(previous);
    }

    public static void applyQuality(ItemStack stack) {
        HarvestData harvestData = CURRENT_HARVEST.get();
        if (harvestData == null || stack.isEmpty()) {
            return;
        }

        ServerPlayer player = harvestData.player();
        BlockPos pos = harvestData.pos();
        QualityUtils.applyQuality(stack, harvestData.state(), LevelData.get(player.level(), pos), player, player.level().getBlockState(pos.below()));
    }

    public record HarvestData(ServerPlayer player, BlockPos pos, BlockState state) {
    }
}
