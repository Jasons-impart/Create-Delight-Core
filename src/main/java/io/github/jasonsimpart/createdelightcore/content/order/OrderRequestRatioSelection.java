package io.github.jasonsimpart.createdelightcore.content.order;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

public record OrderRequestRatioSelection(String entryId, ItemStack stack, int weight, int quality) {
    public OrderRequestRatioSelection {
        entryId = entryId == null ? "" : entryId;
        stack = stack == null ? ItemStack.EMPTY : stack.copyWithCount(1);
        weight = Math.max(0, weight);
        quality = Math.max(0, quality);
    }

    public boolean isValid() {
        return !entryId.isBlank() && !stack.isEmpty() && weight > 0;
    }

    public CompoundTag write() {
        CompoundTag tag = new CompoundTag();
        tag.putString("EntryId", entryId);
        tag.put("Stack", stack.save(new CompoundTag()));
        tag.putInt("Weight", weight);
        tag.putInt("Quality", quality);
        return tag;
    }

    public static OrderRequestRatioSelection read(CompoundTag tag) {
        return new OrderRequestRatioSelection(
                tag.getString("EntryId"),
                ItemStack.of(tag.getCompound("Stack")),
                tag.getInt("Weight"),
                tag.getInt("Quality")
        );
    }

    public void send(FriendlyByteBuf buf) {
        buf.writeUtf(entryId, 128);
        buf.writeItem(stack);
        buf.writeVarInt(weight);
        buf.writeVarInt(quality);
    }

    public static OrderRequestRatioSelection receive(FriendlyByteBuf buf) {
        return new OrderRequestRatioSelection(buf.readUtf(128), buf.readItem(), buf.readVarInt(), buf.readVarInt());
    }
}
