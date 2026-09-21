package io.github.jasonsimpart.compat.mbd2;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.jasonsimpart.compat.qualityfood.QualityFoodCompat;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.fml.ModList;
import net.minecraft.network.chat.Component;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import io.github.jasonsimpart.util.ModIds;

/** Legacy order arithmetic, independent of the table-cloth delivery transport. */
public final class MbdOrders {
    public static final JsonObject DATA = readData("orders");
    private MbdOrders() {}

    static JsonObject readData(String name) {
        try (var stream = MbdOrders.class.getResourceAsStream("/assets/createdelightcore/economy/" + name + ".json")) {
            if (stream == null) throw new IllegalStateException("Missing economy data: " + name);
            return JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
        } catch (java.io.IOException exception) { throw new IllegalStateException(exception); }
    }

    public static JsonObject customer(CompoundTag order) { return DATA.getAsJsonObject("customers").getAsJsonObject(order.getString("type")); }
    public static TagKey<net.minecraft.world.item.Item> tag(String category) {
        return TagKey.create(Registries.ITEM, MbdCompat.id("order/" + category));
    }
    public static int reputationLevel(int value) {
        int[] thresholds = {0, 10, 20, 40, 60, 100};
        for (int i = thresholds.length - 1; i >= 0; i--) if (value >= thresholds[i]) return i + 1;
        return 1;
    }
    static int rarity(JsonObject customer) {
        return switch (customer.get("rarity").getAsString()) { case "UNCOMMON" -> 1; case "RARE" -> 2; case "EPIC" -> 3; default -> 0; };
    }
    public static CompoundTag info(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getCompound("createdelightOrderInfo");
    }
    public static int quality(ItemStack stack, String category) {
        if (category.equals("western_wine")) {
            var data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
            return data.contains("EffectAmplifier") ? Math.min(data.getInt("EffectAmplifier") + 2, 3) : 2;
        }
        var properties = DATA.getAsJsonObject("categories").getAsJsonObject(category);
        if (properties == null || !stack.is(tag(category))) return 0;
        double complexity = MbdFoodEconomy.complexity(stack);
        int level = 0;
        for (var threshold : properties.getAsJsonArray("diversity")) {
            if (complexity <= threshold.getAsDouble()) break;
            level++;
        }
        if (ModList.get().isLoaded(ModIds.QUALITY_FOOD)) level += QualityFoodCompat.getQualityLevel(stack).level();
        return Math.clamp(level, 1, 3);
    }

    public static CompoundTag create(Player player) {
        int level = reputationLevel(player.getPersistentData().getInt("order_reputation"));
        var random = player.getRandom();
        var candidates = new LinkedHashMap<String, Double>();
        DATA.getAsJsonObject("customers").entrySet().forEach(entry -> {
            var customer = entry.getValue().getAsJsonObject();
            int unlock = new int[]{1, 2, 4, 6}[rarity(customer)];
            if (level >= unlock && !availableEntries(customer).isEmpty())
                candidates.put(entry.getKey(), customer.get("chance").getAsDouble() * (1 + Math.max(0, level - unlock) * .15));
        });
        var order = new CompoundTag();
        if (candidates.isEmpty()) return order;
        String selected = choose(candidates, random);
        order.putString("type", selected);
        order.putString("ownerUUID", player.getUUID().toString());
        order.putString("ownerName", player.getGameProfile().getName());
        order.putInt("generatedReputationLevel", level);
        var customer = customer(order);
        var choices = availableEntries(customer);
        var entries = new ListTag();
        do {
            String category = choose(choices, random);
            var entry = new CompoundTag();
            var config = customer.getAsJsonObject("entries").get(category);
            entry.putString("id", category);
            int base = DATA.getAsJsonObject("categories").getAsJsonObject(category).get("base_count").getAsInt();
            entry.putInt("count", (int) (base * (1 + random.nextFloat() * (Math.sqrt(level) * 1.25 - 1))) * 4);
            entry.putInt("minQuality", config.isJsonArray() ? Math.clamp(config.getAsJsonArray().get(1).getAsInt(), 1, 3) : 1);
            entries.add(entry);
        } while (entries.size() < customer.get("max_count").getAsInt()
                && random.nextFloat() < Math.min(.95, customer.get("base_continue_rate").getAsDouble() * Math.sqrt(level)));
        order.put("entries", entries);
        return order;
    }

    private static Map<String, Double> availableEntries(JsonObject customer) {
        var choices = new LinkedHashMap<String, Double>();
        customer.getAsJsonObject("entries").entrySet().forEach(entry -> {
            var value = entry.getValue();
            int minimum = value.isJsonArray() ? Math.max(1, value.getAsJsonArray().get(1).getAsInt()) : 1;
            // Do not issue impossible requests for absent optional food mods.
            boolean available = BuiltInRegistries.ITEM.getTag(tag(entry.getKey())).map(items -> items.stream().anyMatch(item ->
                    quality(item.value().getDefaultInstance(), entry.getKey()) >= minimum
                            || ModList.get().isLoaded(ModIds.QUALITY_FOOD))).orElse(false);
            if (available) choices.put(entry.getKey(), value.isJsonArray() ? value.getAsJsonArray().get(0).getAsDouble() : value.getAsDouble());
        });
        return choices;
    }
    private static String choose(Map<String, Double> choices, RandomSource random) {
        double value = random.nextDouble() * choices.values().stream().mapToDouble(Double::doubleValue).sum();
        String last = null;
        for (var entry : choices.entrySet()) { last = entry.getKey(); value -= entry.getValue(); if (value <= 0) break; }
        return Objects.requireNonNull(last);
    }

    public static double money(CompoundTag order) {
        var customer = customer(order);
        if (customer == null) return 0;
        double goods = 0;
        for (var raw : order.getList("entries", Tag.TAG_COMPOUND)) {
            var entry = (CompoundTag) raw;
            var category = DATA.getAsJsonObject("categories").getAsJsonObject(entry.getString("id"));
            if (category == null || entry.getInt("count") <= 0) return 0;
            goods += (1 + .2 * (entry.getInt("minQuality") - 1)) * entry.getInt("count") / category.get("base_count").getAsDouble();
        }
        return goods * new double[]{1, 1.25, 1.5, 2}[rarity(customer)] / customer.get("chance").getAsDouble()
                * customer.get("reward_money").getAsDouble();
    }

    record Fulfillment(double score, List<ItemStack> remaining) {}

    public static double score(CompoundTag order, List<ItemStack> goods) {
        var fulfillment = fulfill(order, goods);
        return fulfillment == null ? 0 : fulfillment.score();
    }

    /** Computes both scoring and exact leftovers without modifying the submitted goods. */
    static Fulfillment fulfill(CompoundTag order, List<ItemStack> goods) {
        if (customer(order) == null) return null;
        var entries = order.getList("entries", Tag.TAG_COMPOUND);
        if (entries.isEmpty()) return null;
        int[] required = new int[entries.size()];
        for (int i = 0; i < entries.size(); i++) {
            var entry = entries.getCompound(i);
            required[i] = entry.getInt("count");
            if (required[i] <= 0 || entry.getInt("minQuality") < 1 || entry.getInt("minQuality") > 3
                    || !DATA.getAsJsonObject("categories").has(entry.getString("id"))) return null;
        }
        var allocation = io.github.jasonsimpart.util.ItemAllocation.allocate(goods, required, (index, stack) -> {
            var entry = entries.getCompound(index);
            return stack.is(tag(entry.getString("id"))) && quality(stack, entry.getString("id")) >= entry.getInt("minQuality");
        });
        if (allocation == null) return null;
        var inventory = goods.stream().map(ItemStack::copy).toList();
        var totals = new LinkedHashMap<String, double[]>();
        var counts = new HashMap<String, Map<ResourceLocation, Integer>>();
        for (int i = 0; i < entries.size(); i++) {
            var entry = entries.getCompound(i);
            String category = entry.getString("id");
            var total = totals.computeIfAbsent(category, ignored -> new double[2]);
            var types = counts.computeIfAbsent(category, ignored -> new HashMap<>());
            for (int slot = 0; slot < inventory.size(); slot++) {
                int take = allocation[i][slot];
                if (take == 0) continue;
                var stack = inventory.get(slot);
                total[0] += (quality(stack, category) - entry.getInt("minQuality") + 1) * take;
                total[1] += take;
                types.merge(BuiltInRegistries.ITEM.getKey(stack.getItem()), take, Integer::sum);
                stack.shrink(take);
            }
        }
        double weighted = 0, totalCount = 0;
        for (var entry : totals.entrySet()) {
            var total = entry.getValue();
            double bonus = 2 - counts.get(entry.getKey()).values().stream().mapToDouble(count -> Math.pow(count / total[1], 2)).sum();
            weighted += total[0] * bonus;
            totalCount += total[1];
        }
        return totalCount > 0 ? new Fulfillment(weighted / totalCount, inventory) : null;
    }

    static net.minecraft.server.level.ServerPlayer owner(ServerLevel world, CompoundTag order) {
        return world.getServer().getPlayerList().getPlayers().stream().filter(p -> order.contains("ownerUUID")
                ? p.getUUID().toString().equals(order.getString("ownerUUID"))
                : p.getGameProfile().getName().equals(order.getString("ownerName"))).findFirst().orElse(null);
    }

    static void award(ServerLevel world, CompoundTag order, double score) {
        var player = owner(world, order);
        if (player == null) return;
        int size = order.getList("entries", Tag.TAG_COMPOUND).size();
        int gain = Math.max(1, (int) Math.round(size * .5)) + rarity(customer(order))
                + Math.clamp((int) Math.round(Math.max(0, score - 1) * (.75 + size * .5)), 0, 6);
        int before = Math.max(0, player.getPersistentData().getInt("order_reputation"));
        int after = (int) Math.min(Integer.MAX_VALUE, (long) before + gain);
        player.getPersistentData().putInt("order_reputation", after);
        player.sendSystemMessage(Component.translatable("message.createdelight.order_reputation_gain", gain, Math.clamp((int) Math.round(Math.max(0, score - 1) * (.75 + size * .5)), 0, 6), after, reputationLevel(after)));
        if (reputationLevel(after) > reputationLevel(before)) player.sendSystemMessage(Component.translatable("message.createdelight.order_reputation_level_up", reputationLevel(after)));
    }
}
