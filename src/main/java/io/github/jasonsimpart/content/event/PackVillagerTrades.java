package io.github.jasonsimpart.content.event;

import com.google.gson.JsonParser;
import io.github.jasonsimpart.CreateDelightCore;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.TagsUpdatedEvent;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

/** Runs after NeoForge/LC rebuild their listings, matching the former MoreJS post-load hook. */
public final class PackVillagerTrades {
    private static final Set<String> VANILLA_TYPES = Set.of("DyedArmorForEmeralds", "EnchantBookForEmeralds",
            "EnchantedItemForEmeralds", "ItemsForEmeralds", "ItemsAndEmeraldsToItems", "EmeraldForItems",
            "TippedArrowForItemsAndEmeralds", "SuspiciousStewForEmerald", "TreasureMapForEmeralds");

    private PackVillagerTrades() {}

    public static void register() {
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, PackVillagerTrades::onTags);
    }

    private static void onTags(TagsUpdatedEvent event) {
        if (event.getUpdateCause() != TagsUpdatedEvent.UpdateCause.SERVER_DATA_LOAD) return;
        try (var stream = PackVillagerTrades.class.getResourceAsStream("/data/createdelightcore/villager_trades.json")) {
            if (stream == null) throw new IllegalStateException("Missing pack villager trades");
            for (var entry : JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonArray()) {
                var group = entry.getAsJsonObject();
                if (!ModList.get().isLoaded(group.get("requires").getAsString())) continue;
                var professionId = ResourceLocation.parse(group.get("profession").getAsString());
                var profession = BuiltInRegistries.VILLAGER_PROFESSION.getOptional(professionId);
                if (profession.isEmpty() || !VillagerTrades.TRADES.containsKey(profession.get())) continue;
                int level = group.get("level").getAsInt();
                var tiers = VillagerTrades.TRADES.get(profession.get());
                var additions = new ArrayList<VillagerTrades.ItemListing>();
                for (var row : group.getAsJsonArray("trades")) {
                    var trade = row.getAsJsonObject();
                    var inputs = trade.getAsJsonArray("inputs");
                    var first = stack(inputs.get(0).getAsString());
                    var second = inputs.size() == 2 ? stack(inputs.get(1).getAsString()) : ItemStack.EMPTY;
                    var output = stack(trade.get("output").getAsString());
                    if (first.isEmpty() || output.isEmpty() || inputs.size() == 2 && second.isEmpty()) {
                        CreateDelightCore.LOGGER.info("Skipping unavailable pack trade for {}: {}", professionId, trade);
                        continue;
                    }
                    additions.add((entity, random) -> new MerchantOffer(new ItemCost(first.getItem(), first.getCount()),
                            second.isEmpty() ? Optional.empty() : Optional.of(new ItemCost(second.getItem(), second.getCount())),
                            output.copy(), 16, 2, 0.05F));
                }
                // An unavailable group must not erase the profession's usable trades.
                if (additions.isEmpty()) continue;
                var listings = new ArrayList<>(Arrays.asList(tiers.getOrDefault(level, new VillagerTrades.ItemListing[0])));
                if (group.get("replace").getAsBoolean()) listings.removeIf(PackVillagerTrades::isModdedType);
                listings.addAll(additions);
                tiers.put(level, listings.toArray(VillagerTrades.ItemListing[]::new));
            }
        } catch (java.io.IOException exception) {
            throw new IllegalStateException("Unable to read pack villager trades", exception);
        }
    }

    private static boolean isModdedType(VillagerTrades.ItemListing listing) {
        var type = listing.getClass();
        if (type.getEnclosingClass() == VillagerTrades.class && VANILLA_TYPES.contains(type.getSimpleName())) return false;
        // Preserve other packs' MoreJS custom listings without a hard dependency on MoreJS.
        for (Class<?> parent = type; parent != null; parent = parent.getSuperclass()) {
            String name = parent.getName();
            if (name.equals("com.almostreliable.morejs.features.villager.trades.TransformableTrade")
                    || name.equals("com.almostreliable.morejs.features.villager.trades.CustomTrade")) return false;
        }
        return true;
    }

    private static ItemStack stack(String specification) {
        var parts = specification.split("x ", 2);
        int count = parts.length == 2 ? Integer.parseInt(parts[0]) : 1;
        return BuiltInRegistries.ITEM.getOptional(ResourceLocation.parse(parts[parts.length - 1]))
                .map(item -> new ItemStack(item, count)).orElse(ItemStack.EMPTY);
    }
}
