package io.github.jasonsimpart.createdelightcore.content.order.data;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import io.github.jasonsimpart.createdelightcore.CreateDelightCore;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class OrderDataManager extends SimpleJsonResourceReloadListener {
    public static final String DIRECTORY = "createdelightcore_order";
    private static final Gson GSON = new Gson();
    public static final OrderDataManager INSTANCE = new OrderDataManager();
    private static volatile OrderData data = OrderData.defaults();
    private static volatile long version = 0L;

    private OrderDataManager() {
        super(GSON, DIRECTORY);
    }

    public static Optional<OrderTypeData> orderType(String id) {
        return Optional.ofNullable(data.orderTypes().get(id));
    }

    public static boolean hasOrderType(String id) {
        return data.orderTypes().containsKey(id);
    }

    public static Map<String, OrderTypeData> orderTypes() {
        return data.orderTypes();
    }

    public static Set<String> orderTypeIds() {
        return data.orderTypes().keySet();
    }

    public static Map<String, Double> categoryGroup(String id) {
        return data.categoryGroups().getOrDefault(id, Map.of());
    }

    public static Map<String, Map<String, Double>> categoryGroups() {
        return data.categoryGroups();
    }

    public static Optional<OrderDraftSealData> draftSeal(String id) {
        return Optional.ofNullable(data.draftSeals().get(id));
    }

    public static Map<String, OrderDraftSealData> draftSeals() {
        return data.draftSeals();
    }

    public static Optional<OrderCustomerData> customer(String id) {
        return Optional.ofNullable(data.customers().get(id));
    }

    public static Map<String, OrderCustomerData> customers() {
        return data.customers();
    }

    public static String customerGroupPrefix(String id) {
        return data.customerGroupPrefixes().get(id);
    }

    public static Map<String, String> customerGroupPrefixes() {
        return data.customerGroupPrefixes();
    }

    public static OrderMarketSaturationData marketSaturation() {
        return data.marketSaturation();
    }

    public static long version() {
        return version;
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> resources,
                         @NotNull ResourceManager resourceManager,
                         @NotNull ProfilerFiller profiler) {
        OrderData.Builder builder = OrderData.builder();
        resources.forEach((id, element) -> {
            if (!element.isJsonObject()) {
                throw new JsonParseException("Order data " + id + " must be a JSON object");
            }
            String path = id.getPath();
            int separator = path.indexOf('/');
            JsonObject object = element.getAsJsonObject();
            if (separator < 0) {
                parseAggregate(builder, id, path, object);
                return;
            }
            if (separator == path.length() - 1) {
                throw new JsonParseException("Order data " + id + " must not use an empty key");
            }

            String type = path.substring(0, separator);
            String key = path.substring(separator + 1);
            switch (type) {
                case "order_types" -> builder.putOrderType(key, parseOrderType(id, object));
                case "category_groups" -> builder.putCategoryGroup(key, parseWeightedMap(id, object, "entries"));
                case "customer_groups" -> builder.putCustomerGroupPrefix(key, GsonHelper.getAsString(object, "prefix"));
                case "draft_seals" -> builder.putDraftSeal(key, parseDraftSeal(id, object));
                case "customers" -> builder.putCustomer(key, parseCustomer(id, object));
                case "market_saturation" -> builder.setMarketSaturation(parseMarketSaturation(object));
                default -> CreateDelightCore.LOGGER.warn("Ignoring unknown order data type {} from {}", type, id);
            }
        });
        data = builder.build();
        version++;
        CreateDelightCore.LOGGER.info("Loaded {} order types, {} category groups, {} draft seals and {} order customers",
                data.orderTypes().size(), data.categoryGroups().size(), data.draftSeals().size(), data.customers().size());
    }

    private static void parseAggregate(OrderData.Builder builder, ResourceLocation id, String type, JsonObject object) {
        switch (type) {
            case "order_types" -> object.entrySet().forEach(entry -> builder.putOrderType(
                    entry.getKey(),
                    parseOrderType(childId(id, type, entry.getKey()), GsonHelper.convertToJsonObject(entry.getValue(), entry.getKey()))
            ));
            case "category_groups" -> object.entrySet().forEach(entry -> builder.putCategoryGroup(
                    entry.getKey(),
                    parseWeightedMap(childId(id, type, entry.getKey()), GsonHelper.convertToJsonObject(entry.getValue(), entry.getKey()), "entries")
            ));
            case "customer_groups" -> object.entrySet().forEach(entry ->
                    builder.putCustomerGroupPrefix(entry.getKey(), GsonHelper.convertToString(entry.getValue(), entry.getKey())));
            case "draft_seals" -> object.entrySet().forEach(entry -> builder.putDraftSeal(
                    entry.getKey(),
                    parseDraftSeal(childId(id, type, entry.getKey()), GsonHelper.convertToJsonObject(entry.getValue(), entry.getKey()))
            ));
            case "customers" -> object.entrySet().forEach(entry -> builder.putCustomer(
                    entry.getKey(),
                    parseCustomer(childId(id, type, entry.getKey()), GsonHelper.convertToJsonObject(entry.getValue(), entry.getKey()))
            ));
            case "market_saturation" -> builder.setMarketSaturation(parseMarketSaturation(object));
            default -> CreateDelightCore.LOGGER.warn("Ignoring unknown aggregate order data type {} from {}", type, id);
        }
    }

    private static ResourceLocation childId(ResourceLocation id, String type, String key) {
        return new ResourceLocation(id.getNamespace(), type + "/" + key);
    }

    private static OrderTypeData parseOrderType(ResourceLocation id, JsonObject object) {
        JsonArray array = GsonHelper.getAsJsonArray(object, "diversity");
        if (array.isEmpty()) {
            throw new JsonParseException("Order type " + id + " has empty diversity array");
        }
        double[] diversity = new double[array.size()];
        for (int i = 0; i < array.size(); i++) {
            diversity[i] = array.get(i).getAsDouble();
        }
        return new OrderTypeData(diversity, GsonHelper.getAsInt(object, "base_count"));
    }

    private static OrderDraftSealData parseDraftSeal(ResourceLocation id, JsonObject object) {
        String type = GsonHelper.getAsString(object, "type");
        String key = GsonHelper.getAsString(object, "key", id.getPath());
        OrderSpecData spec = object.has("spec") && object.get("spec").isJsonObject()
                ? parseSpec(object.getAsJsonObject("spec"))
                : OrderSpecData.EMPTY;
        return new OrderDraftSealData(type, key, spec);
    }

    private static OrderCustomerData parseCustomer(ResourceLocation id, JsonObject object) {
        Map<String, OrderCustomerData.EntryData> entries = new LinkedHashMap<>();
        JsonObject entriesObject = GsonHelper.getAsJsonObject(object, "entries");
        entriesObject.entrySet().forEach(entry -> {
            JsonArray array = GsonHelper.convertToJsonArray(entry.getValue(), "entry " + entry.getKey());
            if (array.size() < 2) {
                throw new JsonParseException("Customer " + id + " entry " + entry.getKey()
                        + " must contain weight and min_quality");
            }
            entries.put(entry.getKey(), new OrderCustomerData.EntryData(array.get(0).getAsDouble(), array.get(1).getAsInt()));
        });
        return new OrderCustomerData(
                entries,
                GsonHelper.getAsInt(object, "max_count"),
                GsonHelper.getAsDouble(object, "base_continue_rate"),
                GsonHelper.getAsString(object, "rarity", "COMMON"),
                GsonHelper.getAsDouble(object, "chance"),
                GsonHelper.getAsString(object, "reward", ""),
                GsonHelper.getAsInt(object, "reward_count", 1),
                GsonHelper.getAsInt(object, "reward_money", 0)
        );
    }

    private static Map<String, Double> parseWeightedMap(ResourceLocation id, JsonObject object, String field) {
        JsonObject entries = GsonHelper.getAsJsonObject(object, field);
        Map<String, Double> result = new LinkedHashMap<>();
        entries.entrySet().forEach(entry -> result.put(entry.getKey(), entry.getValue().getAsDouble()));
        if (result.isEmpty()) {
            throw new JsonParseException("Weighted order data " + id + " has no entries");
        }
        return result;
    }

    private static OrderSpecData parseSpec(JsonObject object) {
        return new OrderSpecData(
                stringList(object, "customer_groups"),
                stringList(object, "category_groups"),
                doubleMap(object, "customer_weight_bonus"),
                doubleMap(object, "category_weight_bonus"),
                nullableDouble(object, "count_multiplier"),
                nullableDouble(object, "entry_count_multiplier"),
                nullableInteger(object, "min_quality_bonus"),
                nullableDouble(object, "money_multiplier"),
                nullableDouble(object, "reputation_multiplier")
        );
    }

    private static OrderMarketSaturationData parseMarketSaturation(JsonObject object) {
        return new OrderMarketSaturationData(
                GsonHelper.getAsString(object, "storage_key", "createdelight_order_market_saturation"),
                GsonHelper.getAsDouble(object, "decay_per_day", 0.72),
                GsonHelper.getAsDouble(object, "category_penalty", 0.08),
                GsonHelper.getAsDouble(object, "customer_penalty", 0.05),
                GsonHelper.getAsDouble(object, "max_penalty", 0.35),
                GsonHelper.getAsDouble(object, "category_completion_gain", 0.35),
                GsonHelper.getAsDouble(object, "category_completion_scale_max", 2.0),
                GsonHelper.getAsDouble(object, "customer_completion_gain", 0.4),
                GsonHelper.getAsDouble(object, "category_cross_recovery", 0.94),
                GsonHelper.getAsDouble(object, "customer_cross_recovery", 0.94)
        );
    }

    private static List<String> stringList(JsonObject object, String key) {
        if (!object.has(key)) {
            return List.of();
        }
        JsonArray array = GsonHelper.getAsJsonArray(object, key);
        List<String> result = new ArrayList<>();
        for (JsonElement element : array) {
            result.add(element.getAsString());
        }
        return List.copyOf(result);
    }

    private static Map<String, Double> doubleMap(JsonObject object, String key) {
        if (!object.has(key)) {
            return Map.of();
        }
        Map<String, Double> result = new LinkedHashMap<>();
        GsonHelper.getAsJsonObject(object, key).entrySet().forEach(entry -> result.put(entry.getKey(), entry.getValue().getAsDouble()));
        return Collections.unmodifiableMap(result);
    }

    private static Double nullableDouble(JsonObject object, String key) {
        return object.has(key) ? object.get(key).getAsDouble() : null;
    }

    private static Integer nullableInteger(JsonObject object, String key) {
        return object.has(key) ? object.get(key).getAsInt() : null;
    }
}
