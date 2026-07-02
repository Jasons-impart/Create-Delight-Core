package io.github.jasonsimpart.createdelightcore.content.order;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public record OrderInfo(String type, List<OrderEntry> entries, int generatedReputationLevel,
                        String ownerName, String ownerUUID) {
    public static final String ORDER_ITEM_ID = "createdelight:order";
    public static final String ORDER_INFO_TAG = "createdelightOrderInfo";

    public static boolean isOrder(ItemStack stack) {
        return !stack.isEmpty()
                && ORDER_ITEM_ID.equals(stack.getItem().builtInRegistryHolder().key().location().toString())
                && stack.hasTag()
                && stack.getOrCreateTag().contains(ORDER_INFO_TAG, Tag.TAG_COMPOUND);
    }

    public static Optional<OrderInfo> fromStack(ItemStack stack) {
        if (!isOrder(stack)) {
            return Optional.empty();
        }
        return Optional.of(read(stack.getOrCreateTag().getCompound(ORDER_INFO_TAG)));
    }

    public static OrderInfo read(CompoundTag tag) {
        List<OrderEntry> entries = new ArrayList<>();
        Map<String, Integer> seenIds = new HashMap<>();
        ListTag list = tag.getList("entries", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            OrderEntry entry = OrderEntry.read(list.getCompound(i));
            int occurrence = seenIds.merge(entry.id(), 1, Integer::sum);
            String key = occurrence == 1 ? entry.id() : entry.id() + "#" + occurrence;
            entries.add(new OrderEntry(entry.id(), entry.count(), entry.minQuality(), key));
        }

        return new OrderInfo(
                tag.getString("type"),
                List.copyOf(entries),
                tag.getInt("generatedReputationLevel"),
                tag.getString("ownerName"),
                tag.getString("ownerUUID")
        );
    }

    public CompoundTag write() {
        CompoundTag tag = new CompoundTag();
        tag.putString("type", type);
        tag.putInt("generatedReputationLevel", generatedReputationLevel);
        tag.putString("ownerName", ownerName);
        tag.putString("ownerUUID", ownerUUID);

        ListTag list = new ListTag();
        entries.forEach(entry -> list.add(entry.write()));
        tag.put("entries", list);
        return tag;
    }
}
