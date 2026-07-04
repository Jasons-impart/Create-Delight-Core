package io.github.jasonsimpart.createdelightcore.content.order.data;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public record OrderData(Map<String, OrderTypeData> orderTypes,
                        Map<String, Map<String, Double>> categoryGroups,
                        Map<String, String> customerGroupPrefixes,
                        Map<String, OrderDraftSealData> draftSeals,
                        Map<String, OrderCustomerData> customers,
                        OrderMarketSaturationData marketSaturation) {
    public OrderData {
        orderTypes = immutableCopy(orderTypes);
        categoryGroups = immutableNestedCopy(categoryGroups);
        customerGroupPrefixes = immutableCopy(customerGroupPrefixes);
        draftSeals = immutableCopy(draftSeals);
        customers = immutableCopy(customers);
        marketSaturation = marketSaturation == null ? OrderMarketSaturationData.DEFAULT : marketSaturation;
    }

    public static OrderData defaults() {
        return builder().build();
    }

    public static Builder builder() {
        return new Builder();
    }

    private static <T> Map<String, T> immutableCopy(Map<String, T> source) {
        return source == null || source.isEmpty()
                ? Map.of()
                : Collections.unmodifiableMap(new LinkedHashMap<>(source));
    }

    private static Map<String, Map<String, Double>> immutableNestedCopy(Map<String, Map<String, Double>> source) {
        if (source == null || source.isEmpty()) {
            return Map.of();
        }
        Map<String, Map<String, Double>> result = new LinkedHashMap<>();
        source.forEach((key, value) -> result.put(key, immutableCopy(value)));
        return Collections.unmodifiableMap(result);
    }

    public static final class Builder {
        private final Map<String, OrderTypeData> orderTypes = new LinkedHashMap<>();
        private final Map<String, Map<String, Double>> categoryGroups = new LinkedHashMap<>();
        private final Map<String, String> customerGroupPrefixes = new LinkedHashMap<>();
        private final Map<String, OrderDraftSealData> draftSeals = new LinkedHashMap<>();
        private final Map<String, OrderCustomerData> customers = new LinkedHashMap<>();
        private OrderMarketSaturationData marketSaturation = OrderMarketSaturationData.DEFAULT;

        public void putOrderType(String key, OrderTypeData value) {
            orderTypes.put(key, value);
        }

        public void putCategoryGroup(String key, Map<String, Double> value) {
            categoryGroups.put(key, value);
        }

        public void putCustomerGroupPrefix(String key, String value) {
            customerGroupPrefixes.put(key, value);
        }

        public void putDraftSeal(String key, OrderDraftSealData value) {
            draftSeals.put(key, value);
        }

        public void putCustomer(String key, OrderCustomerData value) {
            customers.put(key, value);
        }

        public void setMarketSaturation(OrderMarketSaturationData value) {
            marketSaturation = value == null ? OrderMarketSaturationData.DEFAULT : value;
        }

        public OrderData build() {
            return new OrderData(orderTypes, categoryGroups, customerGroupPrefixes, draftSeals, customers, marketSaturation);
        }
    }
}
