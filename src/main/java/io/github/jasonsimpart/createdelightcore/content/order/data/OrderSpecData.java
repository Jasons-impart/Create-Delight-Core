package io.github.jasonsimpart.createdelightcore.content.order.data;

import java.util.List;
import java.util.Map;

public record OrderSpecData(List<String> customerGroups, List<String> categoryGroups,
                            Map<String, Double> customerWeightBonus, Map<String, Double> categoryWeightBonus,
                            Double countMultiplier, Double entryCountMultiplier, Integer minQualityBonus,
                            Double moneyMultiplier, Double reputationMultiplier) {
    public static final OrderSpecData EMPTY = new OrderSpecData(
            List.of(), List.of(), Map.of(), Map.of(),
            null, null, null, null, null
    );

    public OrderSpecData {
        customerGroups = customerGroups == null ? List.of() : List.copyOf(customerGroups);
        categoryGroups = categoryGroups == null ? List.of() : List.copyOf(categoryGroups);
        customerWeightBonus = customerWeightBonus == null ? Map.of() : Map.copyOf(customerWeightBonus);
        categoryWeightBonus = categoryWeightBonus == null ? Map.of() : Map.copyOf(categoryWeightBonus);
    }
}
