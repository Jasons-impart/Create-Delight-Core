package io.github.jasonsimpart.createdelightcore.content.order;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class OrderTypeProperties {
    public record Properties(double[] diversity, int baseCount) {
        public Properties {
            diversity = diversity.clone();
        }

        @Override
        public double[] diversity() {
            return diversity.clone();
        }
    }

    private static final Map<String, Properties> ORDER_TYPES = createOrderTypes();

    private OrderTypeProperties() {
    }

    public static Optional<Properties> get(String id) {
        return Optional.ofNullable(ORDER_TYPES.get(id));
    }

    public static boolean contains(String id) {
        return ORDER_TYPES.containsKey(id);
    }

    public static int getBaseCount(String id) {
        return get(id).map(Properties::baseCount).orElse(0);
    }

    public static Set<String> ids() {
        return ORDER_TYPES.keySet();
    }

    public static Map<String, Properties> all() {
        return ORDER_TYPES;
    }

    private static Map<String, Properties> createOrderTypes() {
        Map<String, Properties> types = new LinkedHashMap<>();
        put(types, "food", new double[]{1, 2, 3, 4}, 32);
        put(types, "burger", new double[]{4, 4.25, 4.5, 5}, 64);
        put(types, "sandwich", new double[]{3.5, 4, 4.5, 5}, 64);
        put(types, "fast_food", new double[]{4, 4.25, 4.5, 5}, 64);
        put(types, "milk_tea", new double[]{-1, 0, 99, 99}, 32);
        put(types, "cookie", new double[]{-1, 0, 99, 99}, 16);
        put(types, "tea", new double[]{-1, -1, 99, 99}, 32);
        put(types, "western_wine", new double[]{-1, 0, 99, 99}, 16);
        put(types, "eastern_wine", new double[]{-1, -1, 99, 99}, 16);
        put(types, "drink", new double[]{-1, -1, 99, 99}, 32);
        put(types, "ice_cream", new double[]{-1, 0.6, 3, 99}, 16);
        put(types, "fried_food", new double[]{-1, 1.5, 3, 99}, 32);
        put(types, "bread", new double[]{-1, 1, 2.5, 99}, 32);
        put(types, "fruit", new double[]{0, 1, 99, 99}, 16);
        put(types, "vegetable", new double[]{0, 1, 99, 99}, 16);
        put(types, "crop", new double[]{0, 1, 99, 99}, 16);
        put(types, "jello", new double[]{-1, -1, 99, 99}, 16);
        put(types, "jam", new double[]{-1, -1, 99, 99}, 16);
        put(types, "gummy", new double[]{-1, -1, 99, 99}, 16);
        put(types, "coffee", new double[]{-1, -1, 99, 99}, 32);
        put(types, "snack", new double[]{-1, 0.3, 1, 99}, 32);
        put(types, "sushi", new double[]{-1, 1, 3.5, 99}, 32);
        put(types, "popsicle", new double[]{-1, 0.3, 0.7, 99}, 32);
        put(types, "noodle", new double[]{-1, 3.5, 4.5, 99}, 32);
        put(types, "staple_food", new double[]{0, 2, 4, 99}, 64);
        put(types, "barbecue", new double[]{0, 1, 3, 99}, 16);
        put(types, "dessert", new double[]{0, 1, 3, 99}, 16);
        put(types, "meat_dish", new double[]{0, 2, 4, 99}, 32);
        put(types, "vegetarian_dish", new double[]{0, 1, 3, 99}, 32);
        put(types, "mixed_dish", new double[]{0, 2, 4, 99}, 32);
        put(types, "soup", new double[]{0, 2, 4, 99}, 16);
        put(types, "rice", new double[]{0, 2, 4, 99}, 64);
        put(types, "dumpling", new double[]{0, 1, 3, 99}, 32);
        put(types, "monster", new double[]{0, 1, 3, 99}, 64);
        put(types, "sauce", new double[]{0, 1, 3, 99}, 16);
        put(types, "salad", new double[]{0, 2, 4, 99}, 16);
        put(types, "wrap", new double[]{0, 2, 4, 99}, 64);
        put(types, "cake", new double[]{0, 0, 99, 99}, 64);
        put(types, "juice", new double[]{0, 0, 99, 99}, 32);
        put(types, "sausage", new double[]{0, 1, 3, 99}, 32);
        put(types, "milkshake", new double[]{-1, -1, 3, 99}, 16);
        put(types, "pie", new double[]{0, 0, 99, 99}, 64);
        return Collections.unmodifiableMap(types);
    }

    private static void put(Map<String, Properties> types, String id, double[] diversity, int baseCount) {
        types.put(id, new Properties(diversity, baseCount));
    }
}
