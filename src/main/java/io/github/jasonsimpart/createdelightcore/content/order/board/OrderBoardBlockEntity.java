package io.github.jasonsimpart.createdelightcore.content.order.board;

import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.packager.InventorySummary;
import com.simibubi.create.content.logistics.stockTicker.StockCheckingBlockEntity;
import io.github.jasonsimpart.createdelightcore.content.order.OrderGoodsQuality;
import io.github.jasonsimpart.createdelightcore.content.order.data.OrderDataManager;
import io.github.jasonsimpart.createdelightcore.content.order.data.OrderSpecData;
import io.github.jasonsimpart.createdelightcore.registry.CDBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.LinkedHashSet;
import java.util.Set;

public class OrderBoardBlockEntity extends StockCheckingBlockEntity {
    public OrderBoardBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public OrderBoardBlockEntity(BlockPos pos, BlockState state) {
        this(CDBlockEntities.ORDER_BOARD.get(), pos, state);
    }

    public InventorySummary getOrderBoardSummary() {
        return getAccurateSummary();
    }

    public int countAvailable(OrderSpecData spec, InventorySummary summary) {
        if (spec == null || spec.categoryGroups().isEmpty()) {
            return 0;
        }
        if (summary == null || summary.isEmpty()) {
            return 0;
        }

        Set<String> categories = new LinkedHashSet<>();
        spec.categoryGroups().forEach(group -> categories.addAll(OrderDataManager.categoryGroup(group).keySet()));
        long count = 0L;
        for (BigItemStack stack : summary.getStacks()) {
            for (String category : categories) {
                if (OrderGoodsQuality.getQuality(stack.stack, category) > 0) {
                    count += Math.max(0, stack.count);
                    break;
                }
            }
        }
        return (int) Math.min(Integer.MAX_VALUE, count);
    }

    public int countAvailable(String category, InventorySummary summary) {
        if (category == null || category.isBlank() || summary == null || summary.isEmpty()) {
            return 0;
        }
        long count = 0L;
        for (BigItemStack stack : summary.getStacks()) {
            if (OrderGoodsQuality.getQuality(stack.stack, category) > 0) {
                count += Math.max(0, stack.count);
            }
        }
        return (int) Math.min(Integer.MAX_VALUE, count);
    }
}
