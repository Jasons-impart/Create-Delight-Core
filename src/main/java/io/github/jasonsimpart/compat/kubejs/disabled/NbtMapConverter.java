package io.github.jasonsimpart.compat.kubejs.disabled;

import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

import java.util.List;
import java.util.Map;

final class NbtMapConverter {
    private NbtMapConverter() {
    }

    static CompoundTag toCompound(Map<?, ?> map) {
        CompoundTag tag = new CompoundTag();
        if (map == null) {
            return tag;
        }

        for (Map.Entry<?, ?> entry : map.entrySet()) {
            tag.put(String.valueOf(entry.getKey()), toTag(entry.getValue()));
        }
        return tag;
    }

    @SuppressWarnings("unchecked")
    private static Tag toTag(Object value) {
        if (value instanceof Map<?, ?> map) {
            return toCompound(map);
        }
        if (value instanceof List<?> list) {
            ListTag tag = new ListTag();
            list.forEach(element -> tag.add(toTag(element)));
            return tag;
        }
        if (value instanceof Boolean bool) {
            return ByteTag.valueOf(bool);
        }
        if (value instanceof Integer integer) {
            return IntTag.valueOf(integer);
        }
        if (value instanceof Long longValue) {
            return LongTag.valueOf(longValue);
        }
        if (value instanceof Number number) {
            return DoubleTag.valueOf(number.doubleValue());
        }
        return StringTag.valueOf(String.valueOf(value));
    }
}
