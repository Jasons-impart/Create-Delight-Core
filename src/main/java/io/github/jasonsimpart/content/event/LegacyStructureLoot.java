package io.github.jasonsimpart.content.event;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.neoforged.neoforge.event.LootTableLoadEvent;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/** Mechanically extracted 0488 weighted pools; absent tables/items are skipped. */
public final class LegacyStructureLoot {
    private static final Map<ResourceLocation, JsonArray> POOLS = load();
    private LegacyStructureLoot() {}

    private static Map<ResourceLocation, JsonArray> load() {
        Map<ResourceLocation, JsonArray> result = new HashMap<>();
        try (var stream = Objects.requireNonNull(LegacyStructureLoot.class.getResourceAsStream(
                "/data/createdelightcore/legacy_structure_loot.json"));
             var reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            for (var entry : JsonParser.parseReader(reader).getAsJsonArray()) {
                var rule = entry.getAsJsonObject();
                for (var table : rule.getAsJsonArray("tables"))
                    result.put(ResourceLocation.parse(table.getAsString()), rule.getAsJsonArray("pools"));
            }
        } catch (java.io.IOException exception) { throw new IllegalStateException("Cannot load legacy loot pools", exception); }
        return result;
    }

    public static void onLoad(LootTableLoadEvent event) {
        var pools = POOLS.get(event.getName());
        if (pools == null) return;
        for (int index = 0; index < pools.size(); index++) {
            JsonObject source = pools.get(index).getAsJsonObject();
            var rolls = source.getAsJsonArray("rolls");
            var pool = LootPool.lootPool().name("createdelightcore_legacy_" + index)
                    .setRolls(UniformGenerator.between(rolls.get(0).getAsFloat(), rolls.get(1).getAsFloat()));
            boolean populated = false;
            for (var entry : source.getAsJsonArray("entries")) {
                var row = entry.getAsJsonArray();
                var item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(row.get(0).getAsString()));
                if (item == Items.AIR) continue;
                var builder = LootItem.lootTableItem(item).setWeight(row.get(1).getAsInt());
                if (row.size() > 2) {
                    var count = row.get(2);
                    float min = count.isJsonArray() ? count.getAsJsonArray().get(0).getAsFloat() : count.getAsFloat();
                    float max = count.isJsonArray() ? count.getAsJsonArray().get(1).getAsFloat() : min;
                    builder.apply(SetItemCountFunction.setCount(UniformGenerator.between(min, max)));
                }
                pool.add(builder);
                populated = true;
            }
            if (populated) event.getTable().addPool(pool.build());
        }
    }
}
