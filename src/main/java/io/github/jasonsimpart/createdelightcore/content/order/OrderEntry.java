package io.github.jasonsimpart.createdelightcore.content.order;

import net.minecraft.nbt.CompoundTag;

public record OrderEntry(String id, int count, int minQuality, String key) {
    public OrderEntry(String id, int count, int minQuality) {
        this(id, count, minQuality, id);
    }

    public OrderEntry {
        id = id == null ? "" : id;
        key = key == null || key.isBlank() ? id : key;
    }

    public static OrderEntry read(CompoundTag tag) {
        return new OrderEntry(
                tag.getString("id"),
                tag.getInt("count"),
                Math.max(1, tag.getInt("minQuality")),
                tag.contains("key") ? tag.getString("key") : tag.getString("id")
        );
    }

    public CompoundTag write() {
        CompoundTag tag = new CompoundTag();
        tag.putString("id", id);
        tag.putString("key", key);
        tag.putInt("count", count);
        tag.putInt("minQuality", minQuality);
        return tag;
    }
}
