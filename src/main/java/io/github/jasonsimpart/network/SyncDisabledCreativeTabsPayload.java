package io.github.jasonsimpart.network;

import io.github.jasonsimpart.CreateDelightCore;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;
import java.util.Set;

public record SyncDisabledCreativeTabsPayload(List<ResourceLocation> tabs) implements CustomPacketPayload {
    public static final Type<SyncDisabledCreativeTabsPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(CreateDelightCore.MODID, "sync_disabled_creative_tabs"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncDisabledCreativeTabsPayload> STREAM_CODEC =
            ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs.list())
                    .map(SyncDisabledCreativeTabsPayload::new, SyncDisabledCreativeTabsPayload::tabs).cast();

    public SyncDisabledCreativeTabsPayload {
        tabs = List.copyOf(tabs);
    }

    @Override
    public Type<SyncDisabledCreativeTabsPayload> type() {
        return TYPE;
    }

    public static void handle(SyncDisabledCreativeTabsPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> ClientCreativeTabCache.replace(Set.copyOf(payload.tabs)));
    }
}
