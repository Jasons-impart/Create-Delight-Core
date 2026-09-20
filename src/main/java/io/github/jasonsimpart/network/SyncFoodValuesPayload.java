package io.github.jasonsimpart.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import java.util.HashMap;
import java.util.Map;

public record SyncFoodValuesPayload(Map<ResourceLocation, Integer> values) implements CustomPacketPayload {
    public static final Type<SyncFoodValuesPayload> TYPE = new Type<>(ResourceLocation.parse("createdelightcore:food_values"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncFoodValuesPayload> STREAM_CODEC =
            ByteBufCodecs.map(HashMap::new, ResourceLocation.STREAM_CODEC, ByteBufCodecs.VAR_INT)
                    .map(SyncFoodValuesPayload::new, payload -> new HashMap<>(payload.values())).cast();
    private static Map<ResourceLocation, Integer> clientValues = Map.of();
    public SyncFoodValuesPayload { values = Map.copyOf(values); }
    @Override public Type<SyncFoodValuesPayload> type() { return TYPE; }
    public static int clientValue(ResourceLocation item) { return clientValues.getOrDefault(item, -1); }
    public static void clearClientValues() { clientValues = Map.of(); }
    public static void handle(SyncFoodValuesPayload payload, IPayloadContext context) {
        var connection = context.connection();
        context.enqueueWork(() -> {
            if (connection.isConnected()) clientValues = payload.values;
        });
    }
}
