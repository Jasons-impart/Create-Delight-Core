package io.github.jasonsimpart.createdelightcore.content.order;

import net.minecraft.core.registries.BuiltInRegistries;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class OrderRequestEstimator {
    public static final double GREAT_THRESHOLD = 2.5D;

    private OrderRequestEstimator() {
    }

    public static double estimate(List<OrderEntry> entries, List<OrderRequestSelection> selections) {
        double weightedSum = 0;
        int totalCount = 0;

        for (OrderEntry entry : entries) {
            EntryScore score = estimateEntry(entry, selections);
            if (!score.complete()) {
                return 0;
            }
            weightedSum += score.score() * score.count();
            totalCount += score.count();
        }

        return totalCount <= 0 ? 0 : weightedSum / totalCount;
    }

    public static EntryScore estimateEntry(OrderEntry entry, List<OrderRequestSelection> selections) {
        int remaining = entry.count();
        int totalCount = 0;
        double totalQuality = 0;
        Map<String, Integer> countByType = new HashMap<>();

        for (OrderRequestSelection selection : selections) {
            if (!entry.key().equals(selection.entryId()) || !selection.isValid()) {
                continue;
            }
            int take = Math.min(remaining, selection.count());
            if (take <= 0) {
                continue;
            }

            totalQuality += (selection.quality() - entry.minQuality() + 1) * take;
            totalCount += take;
            remaining -= take;
            String typeKey = BuiltInRegistries.ITEM.getKey(selection.stack().getItem()).toString();
            countByType.merge(typeKey, take, Integer::sum);

            if (remaining <= 0) {
                break;
            }
        }

        if (remaining > 0 || totalCount <= 0) {
            return new EntryScore(false, totalCount, 0);
        }

        double averageQuality = totalQuality / totalCount;
        return new EntryScore(true, totalCount, averageQuality * typeBonus(countByType));
    }

    public static EstimateTier tier(double score) {
        if (score <= 0) {
            return EstimateTier.INCOMPLETE;
        }
        if (score < 1.25D) {
            return EstimateTier.NORMAL;
        }
        if (score < 1.75D) {
            return EstimateTier.GOOD;
        }
        if (score < GREAT_THRESHOLD) {
            return EstimateTier.EXCELLENT;
        }
        return EstimateTier.GREAT;
    }

    public static double greatOverflow(double score) {
        return Math.max(0, score - GREAT_THRESHOLD);
    }

    private static double typeBonus(Map<String, Integer> countByType) {
        int total = countByType.values().stream().mapToInt(Integer::intValue).sum();
        if (total <= 0) {
            return 1;
        }

        double sumSquares = 0;
        for (int count : countByType.values()) {
            double p = count / (double) total;
            sumSquares += p * p;
        }
        return 1 + (1 - sumSquares);
    }

    public enum EstimateTier {
        INCOMPLETE("createdelightcore.gui.estimate.incomplete"),
        NORMAL("createdelightcore.gui.estimate.normal"),
        GOOD("createdelightcore.gui.estimate.good"),
        EXCELLENT("createdelightcore.gui.estimate.excellent"),
        GREAT("createdelightcore.gui.estimate.great");

        private final String translationKey;

        EstimateTier(String translationKey) {
            this.translationKey = translationKey;
        }

        public String translationKey() {
            return translationKey;
        }
    }

    public record EntryScore(boolean complete, int count, double score) {
    }
}
