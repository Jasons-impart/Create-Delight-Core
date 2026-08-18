package io.github.jasonsimpart.createdelightcore.content.order.supply;

import io.github.jasonsimpart.createdelightcore.content.order.data.OrderSupplyData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;
import java.util.UUID;

public record SupplyCommissionRecord(UUID id,
                                     UUID owner,
                                     String ownerName,
                                     ResourceLocation targetItem,
                                     int count,
                                     String race,
                                     long createdTime,
                                     long dueTime,
                                     int tickets,
                                     int money) {
    public static SupplyCommissionRecord create(UUID owner, String ownerName, OrderSupplyData entry, long now) {
        return create(owner, ownerName, entry, now, entry.count());
    }

    public static SupplyCommissionRecord create(UUID owner, String ownerName, OrderSupplyData entry, long now,
                                                 int count) {
        return new SupplyCommissionRecord(UUID.randomUUID(), owner, ownerName, entry.item(), Math.max(1, count), entry.race(),
                now, now + Math.max(1L, entry.days() * 24000L), entry.tickets(), Math.max(0, entry.money()));
    }

    public CompoundTag write() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("Id", id);
        tag.putUUID("Owner", owner);
        tag.putString("OwnerName", ownerName);
        tag.putString("TargetItem", targetItem.toString());
        tag.putInt("Count", count);
        tag.putString("Race", race);
        tag.putLong("CreatedTime", createdTime);
        tag.putLong("DueTime", dueTime);
        tag.putInt("Tickets", tickets);
        tag.putInt("Money", money);
        return tag;
    }

    public static Optional<SupplyCommissionRecord> read(CompoundTag tag) {
        if (!tag.hasUUID("Id") || !tag.hasUUID("Owner")) {
            return Optional.empty();
        }
        ResourceLocation target = ResourceLocation.tryParse(tag.getString("TargetItem"));
        if (target == null) {
            return Optional.empty();
        }
        return Optional.of(new SupplyCommissionRecord(
                tag.getUUID("Id"),
                tag.getUUID("Owner"),
                tag.getString("OwnerName"),
                target,
                Math.max(1, tag.getInt("Count")),
                tag.getString("Race"),
                tag.getLong("CreatedTime"),
                tag.getLong("DueTime"),
                Math.max(0, tag.getInt("Tickets")),
                Math.max(0, tag.getInt("Money"))
        ));
    }
}
