package io.github.jasonsimpart.createdelightcore.content.order;

import com.simibubi.create.content.logistics.BigItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

public record OrderRequestSelection(String entryId, ItemStack stack, int count, int quality) {
    public OrderRequestSelection {
        entryId = entryId == null ? "" : entryId;
        stack = stack == null ? ItemStack.EMPTY : stack.copyWithCount(1);
        count = Math.max(0, count);
        quality = Math.max(0, quality);
    }

    public boolean isValid() {
        return !entryId.isBlank() && !stack.isEmpty() && count > 0 && quality > 0;
    }

    public BigItemStack asBigItemStack() {
        return new BigItemStack(stack.copy(), count);
    }

    public CompoundTag write() {
        CompoundTag tag = new CompoundTag();
        tag.putString("EntryId", entryId);
        tag.put("Stack", stack.save(new CompoundTag()));
        tag.putInt("Count", count);
        tag.putInt("Quality", quality);
        return tag;
    }

    public static OrderRequestSelection read(CompoundTag tag) {
        return new OrderRequestSelection(
                tag.getString("EntryId"),
                ItemStack.of(tag.getCompound("Stack")),
                tag.getInt("Count"),
                tag.getInt("Quality")
        );
    }

    public void send(FriendlyByteBuf buf) {
        buf.writeUtf(entryId, 128);
        buf.writeItem(stack);
        buf.writeVarInt(count);
        buf.writeVarInt(quality);
    }

    public static OrderRequestSelection receive(FriendlyByteBuf buf) {
        return new OrderRequestSelection(buf.readUtf(128), buf.readItem(), buf.readVarInt(), buf.readVarInt());
    }
}
