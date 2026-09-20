package io.github.jasonsimpart.network;

import io.github.jasonsimpart.CreateDelightCore;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Only an input modifier; normal vanilla interaction still validates position/reach. */
public record ChainCasingModifierPayload(boolean down) implements CustomPacketPayload {
    public static final String KEY = "createdelightcore:chain_casing_modifier";
    public static final Type<ChainCasingModifierPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(CreateDelightCore.MODID, "chain_casing_modifier"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ChainCasingModifierPayload> STREAM_CODEC =
            StreamCodec.of((buf, value) -> buf.writeBoolean(value.down()),
                    buf -> new ChainCasingModifierPayload(buf.readBoolean()));

    @Override
    public Type<ChainCasingModifierPayload> type() { return TYPE; }

    public static void handle(ChainCasingModifierPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> context.player().getPersistentData().putBoolean(KEY, payload.down()));
    }
}
