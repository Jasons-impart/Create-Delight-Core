package io.github.jasonsimpart.createdelightcore.content.order.supply;

import io.github.jasonsimpart.createdelightcore.content.order.data.OrderDataManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.ArrayList;
import java.util.List;

public final class SupplyCommissionPlayerData {
    private static final String DISCOVERED_SUPPLIES = "createdelightcore_discovered_supplies";

    private SupplyCommissionPlayerData() {
    }

    public static boolean isSupplyUnlocked(ServerPlayer player, ResourceLocation item) {
        return player.isCreative() || discoveredSupplies(player).getBoolean(item.toString());
    }

    public static List<ResourceLocation> discoverInventorySupplies(ServerPlayer player) {
        CompoundTag discovered = discoveredSupplies(player);
        List<ResourceLocation> newlyDiscovered = new ArrayList<>();
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            var stack = player.getInventory().getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }
            ResourceLocation item = BuiltInRegistries.ITEM.getKey(stack.getItem());
            String key = item.toString();
            if (!discovered.contains(key) && OrderDataManager.supply(item).isPresent()) {
                discovered.putBoolean(key, true);
                newlyDiscovered.add(item);
            }
        }
        return List.copyOf(newlyDiscovered);
    }

    private static CompoundTag discoveredSupplies(ServerPlayer player) {
        CompoundTag playerData = kubeJsPersistentData(player);
        if (!playerData.contains(DISCOVERED_SUPPLIES, Tag.TAG_COMPOUND)) {
            playerData.put(DISCOVERED_SUPPLIES, new CompoundTag());
        }
        return playerData.getCompound(DISCOVERED_SUPPLIES);
    }

    private static CompoundTag kubeJsPersistentData(ServerPlayer player) {
        try {
            Object value = player.getClass().getMethod("kjs$getPersistentData").invoke(player);
            if (value instanceof CompoundTag tag) {
                return tag;
            }
        } catch (ReflectiveOperationException | SecurityException ignored) {
        }
        return player.getPersistentData();
    }
}
