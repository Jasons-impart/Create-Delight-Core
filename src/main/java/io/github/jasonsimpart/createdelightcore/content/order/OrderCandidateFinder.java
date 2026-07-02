package io.github.jasonsimpart.createdelightcore.content.order;

import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.packager.InventorySummary;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class OrderCandidateFinder {
    private OrderCandidateFinder() {
    }

    public static List<OrderCandidate> findCandidates(InventorySummary summary, OrderEntry entry) {
        return findCandidates(summary, entry.id(), entry.minQuality());
    }

    public static List<OrderCandidate> findCandidates(InventorySummary summary, String orderType, int minQuality) {
        if (summary == null || summary.isEmpty()) {
            return List.of();
        }

        List<OrderCandidate> candidates = new ArrayList<>();
        for (BigItemStack stack : summary.getStacks()) {
            int quality = OrderGoodsQuality.getQuality(stack.stack, orderType);
            if (quality < minQuality) {
                continue;
            }
            candidates.add(new OrderCandidate(stack.stack, stack.count, quality));
        }

        candidates.sort(Comparator
                .comparingInt(OrderCandidate::quality)
                .thenComparing(candidate -> candidate.stack().getHoverName().getString())
                .thenComparing(candidate -> candidate.stack().getItem().builtInRegistryHolder().key().location().toString()));
        return List.copyOf(candidates);
    }
}
