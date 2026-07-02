package io.github.jasonsimpart.createdelightcore.content.order;

import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class OrderRequestResolver {
    private OrderRequestResolver() {
    }

    public static RatioResult resolveRatio(List<OrderEntryCandidates> groups, List<OrderRequestRatioSelection> ratios) {
        List<OrderRequestSelection> selections = new ArrayList<>();
        List<String> failedEntryIds = new ArrayList<>();
        Map<String, Integer> remainingStock = new HashMap<>();

        for (OrderEntryCandidates group : groups) {
            OrderEntry entry = group.entry();
            List<OrderRequestRatioSelection> entryRatios = ratios.stream()
                    .filter(selection -> entry.id().equals(selection.entryId()) && selection.isValid())
                    .sorted(Comparator.comparing(OrderRequestResolver::stackKey))
                    .toList();

            if (entryRatios.isEmpty()) {
                failedEntryIds.add(entry.key());
                continue;
            }

            List<Allocation> allocations = allocate(entry, entryRatios);
            boolean failed = false;
            List<OrderRequestSelection> entrySelections = new ArrayList<>();
            Map<String, Integer> entryStockUse = new HashMap<>();
            for (Allocation allocation : allocations) {
                if (allocation.count <= 0) {
                    continue;
                }

                OrderCandidate candidate = findCandidate(group, allocation.ratio);
                if (candidate == null || candidate.quality() < entry.minQuality()) {
                    failed = true;
                    break;
                }
                String stackKey = stackKey(candidate.stack());
                int available = remainingStock.computeIfAbsent(stackKey, key -> candidate.count());
                int alreadyUsed = entryStockUse.getOrDefault(stackKey, 0);
                if (available - alreadyUsed < allocation.count) {
                    failed = true;
                    break;
                }
                entryStockUse.put(stackKey, alreadyUsed + allocation.count);
                entrySelections.add(new OrderRequestSelection(entry.key(), candidate.stack(), allocation.count, candidate.quality()));
            }

            if (failed) {
                failedEntryIds.add(entry.key());
            } else {
                entryStockUse.forEach((stackKey, used) -> remainingStock.put(stackKey, remainingStock.getOrDefault(stackKey, 0) - used));
                selections.addAll(entrySelections);
            }
        }

        return new RatioResult(List.copyOf(selections), List.copyOf(failedEntryIds));
    }

    public static List<OrderRequestSelection> previewRatio(List<OrderEntryCandidates> groups, List<OrderRequestRatioSelection> ratios) {
        List<OrderRequestSelection> selections = new ArrayList<>();

        for (OrderEntryCandidates group : groups) {
            OrderEntry entry = group.entry();
            List<OrderRequestRatioSelection> entryRatios = ratios.stream()
                    .filter(selection -> entry.id().equals(selection.entryId()) && selection.isValid())
                    .sorted(Comparator.comparing(OrderRequestResolver::stackKey))
                    .toList();

            for (Allocation allocation : allocate(entry, entryRatios)) {
                if (allocation.count <= 0) {
                    continue;
                }

                OrderCandidate candidate = findCandidate(group, allocation.ratio);
                ItemStack stack = candidate == null ? allocation.ratio.stack() : candidate.stack();
                int quality = candidate == null ? allocation.ratio.quality() : candidate.quality();
                selections.add(new OrderRequestSelection(entry.key(), stack, allocation.count, quality));
            }
        }

        return List.copyOf(selections);
    }

    private static List<Allocation> allocate(OrderEntry entry, List<OrderRequestRatioSelection> ratios) {
        int totalWeight = ratios.stream().mapToInt(OrderRequestRatioSelection::weight).sum();
        if (totalWeight <= 0 || entry.count() <= 0) {
            return List.of();
        }

        List<Allocation> allocations = new ArrayList<>();
        int assigned = 0;
        for (OrderRequestRatioSelection ratio : ratios) {
            double exact = entry.count() * (ratio.weight() / (double) totalWeight);
            int count = (int) Math.floor(exact);
            assigned += count;
            allocations.add(new Allocation(ratio, count, exact - count));
        }

        int remaining = entry.count() - assigned;
        allocations.stream()
                .sorted(Comparator
                        .comparingDouble(Allocation::fraction).reversed()
                        .thenComparing(allocation -> stackKey(allocation.ratio)))
                .limit(Math.max(0, remaining))
                .forEach(allocation -> allocation.count++);
        return allocations;
    }

    private static OrderCandidate findCandidate(OrderEntryCandidates group, OrderRequestRatioSelection selection) {
        for (OrderCandidate candidate : group.candidates()) {
            if (ItemStack.isSameItemSameTags(candidate.stack(), selection.stack())) {
                return candidate;
            }
        }
        return null;
    }

    public static String stackKey(OrderRequestRatioSelection selection) {
        return stackKey(selection.stack());
    }

    public static String stackKey(ItemStack stack) {
        return stack.getItem().builtInRegistryHolder().key().location().toString()
                + (stack.hasTag() ? stack.getTag() : "");
    }

    private static final class Allocation {
        private final OrderRequestRatioSelection ratio;
        private int count;
        private final double fraction;

        private Allocation(OrderRequestRatioSelection ratio, int count, double fraction) {
            this.ratio = ratio;
            this.count = count;
            this.fraction = fraction;
        }

        private double fraction() {
            return fraction;
        }
    }

    public record RatioResult(List<OrderRequestSelection> selections, List<String> failedEntryIds) {
        public boolean complete() {
            return failedEntryIds.isEmpty();
        }

        public boolean hasAnySelection() {
            return !selections.isEmpty();
        }

        public boolean failedEntry(String entryId) {
            return failedEntryIds.contains(entryId);
        }
    }
}
