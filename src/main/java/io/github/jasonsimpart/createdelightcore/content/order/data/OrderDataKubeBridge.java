package io.github.jasonsimpart.createdelightcore.content.order.data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class OrderDataKubeBridge {
    private OrderDataKubeBridge() {
    }

    public static long version() {
        return OrderDataManager.version();
    }

    public static Map<String, Object> orderProperties() {
        Map<String, Object> result = new LinkedHashMap<>();
        OrderDataManager.orderTypes().forEach((key, value) -> {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("diversity", toList(value.diversity()));
            entry.put("base_count", value.baseCount());
            entry.put("reward_weight", value.rewardWeight());
            result.put(key, entry);
        });
        return result;
    }

    public static Map<String, String> customerGroupPrefixes() {
        return new LinkedHashMap<>(OrderDataManager.customerGroupPrefixes());
    }

    public static Map<String, Map<String, Double>> categoryGroups() {
        Map<String, Map<String, Double>> result = new LinkedHashMap<>();
        OrderDataManager.categoryGroups().forEach((key, value) -> result.put(key, new LinkedHashMap<>(value)));
        return result;
    }

    public static Map<String, Object> orderDraftSeals() {
        Map<String, Object> result = new LinkedHashMap<>();
        OrderDataManager.draftSeals().forEach((key, value) -> {
            Map<String, Object> seal = new LinkedHashMap<>();
            seal.put("type", value.type());
            seal.put("key", value.key());
            seal.put("spec", spec(value.spec()));
            result.put(key, seal);
        });
        return result;
    }

    public static Map<String, Object> marketSaturationConfig() {
        OrderMarketSaturationData value = OrderDataManager.marketSaturation();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("storageKey", value.storageKey());
        result.put("decayPerDay", value.decayPerDay());
        result.put("categoryPenalty", value.categoryPenalty());
        result.put("customerPenalty", value.customerPenalty());
        result.put("maxBonus", value.maxBonus());
        result.put("categoryCompletionGain", value.categoryCompletionGain());
        result.put("categoryCompletionScaleMax", value.categoryCompletionScaleMax());
        result.put("customerCompletionGain", value.customerCompletionGain());
        result.put("categoryCrossRecovery", value.categoryCrossRecovery());
        result.put("customerCrossRecovery", value.customerCrossRecovery());
        return result;
    }

    public static Map<String, Object> customerProperties() {
        Map<String, Object> result = new LinkedHashMap<>();
        OrderDataManager.customers().forEach((key, value) -> {
            Map<String, Object> customer = new LinkedHashMap<>();
            Map<String, Object> entries = new LinkedHashMap<>();
            value.entries().forEach((entryKey, entryValue) ->
                    entries.put(entryKey, List.of(entryValue.weight(), entryValue.minQuality())));
            customer.put("entries", entries);
            customer.put("max_count", value.maxCount());
            customer.put("base_continue_rate", value.baseContinueRate());
            customer.put("rarity", value.rarity());
            customer.put("chance", value.chance());
            customer.put("reward", List.of(value.reward(), value.rewardCount()));
            customer.put("reward_money", value.rewardMoney());
            result.put(key, customer);
        });
        return result;
    }

    public static Map<String, Object> supplyCatalog() {
        Map<String, Object> result = new LinkedHashMap<>();
        OrderDataManager.supplyCatalog().forEach((key, value) -> {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("item", value.item().toString());
            entry.put("race", value.race());
            entry.put("count", value.count());
            entry.put("tickets", value.tickets());
            entry.put("money", value.money());
            entry.put("days", value.days());
            result.put(key, entry);
        });
        return result;
    }

    public static Map<String, Object> all() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("version", version());
        result.put("orderProperties", orderProperties());
        result.put("customerGroupPrefixes", customerGroupPrefixes());
        result.put("categoryGroups", categoryGroups());
        result.put("orderDraftSeals", orderDraftSeals());
        result.put("marketSaturationConfig", marketSaturationConfig());
        result.put("customerProperties", customerProperties());
        result.put("supplyCatalog", supplyCatalog());
        return result;
    }

    private static Map<String, Object> spec(OrderSpecData value) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (!value.customerGroups().isEmpty()) result.put("customerGroups", value.customerGroups());
        if (!value.categoryGroups().isEmpty()) result.put("categoryGroups", value.categoryGroups());
        if (!value.requiredCategories().isEmpty()) result.put("requiredCategories", value.requiredCategories());
        if (!value.customerWeightBonus().isEmpty()) result.put("customerWeightBonus", value.customerWeightBonus());
        if (!value.categoryWeightBonus().isEmpty()) result.put("categoryWeightBonus", value.categoryWeightBonus());
        if (value.countMultiplier() != null) result.put("countMultiplier", value.countMultiplier());
        if (value.entryCountMultiplier() != null) result.put("entryCountMultiplier", value.entryCountMultiplier());
        if (value.minQualityBonus() != null) result.put("minQualityBonus", value.minQualityBonus());
        if (value.moneyMultiplier() != null) result.put("moneyMultiplier", value.moneyMultiplier());
        if (value.reputationMultiplier() != null) result.put("reputationMultiplier", value.reputationMultiplier());
        return result;
    }

    private static List<Double> toList(double[] values) {
        List<Double> result = new ArrayList<>();
        for (double value : values) {
            result.add(value);
        }
        return List.copyOf(result);
    }
}
