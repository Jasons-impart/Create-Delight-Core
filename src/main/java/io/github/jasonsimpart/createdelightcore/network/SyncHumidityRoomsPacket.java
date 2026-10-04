package io.github.jasonsimpart.createdelightcore.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;
import java.util.Map;
import java.util.function.Supplier;

/** Active provider centers and absolute targets for fixed humidity ranges. */
public record SyncHumidityRoomsPacket(ResourceLocation dimension, Map<Long, Float> values) {
    public void encode(FriendlyByteBuf buf) {
        buf.writeResourceLocation(dimension);
        buf.writeMap(values, FriendlyByteBuf::writeLong, FriendlyByteBuf::writeFloat);
    }
    public static SyncHumidityRoomsPacket decode(FriendlyByteBuf buf) {
        return new SyncHumidityRoomsPacket(buf.readResourceLocation(),
                buf.readMap(FriendlyByteBuf::readLong, FriendlyByteBuf::readFloat));
    }
    public void handle(Supplier<NetworkEvent.Context> supplier) {
        var context = supplier.get();
        if (context.getDirection().getReceptionSide().isClient())
            context.enqueueWork(() -> ClientHumidityRoomCache.accept(dimension, values));
        context.setPacketHandled(true);
    }
}
