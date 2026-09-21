package io.github.jasonsimpart.network;

import io.github.jasonsimpart.CreateDelightCore;
import io.github.jasonsimpart.content.disabled.DisabledCreativeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

import java.util.List;
import java.util.Set;

public final class ModNetwork {
    private static final String PROTOCOL_VERSION = "1";

    private ModNetwork() {
    }

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(ModNetwork::registerPayloadHandlers);
        NeoForge.EVENT_BUS.addListener(ModNetwork::syncFuelMaps);
        NeoForge.EVENT_BUS.addListener(ModNetwork::syncCreativeTabs);
        NeoForge.EVENT_BUS.addListener(ModNetwork::clearCreativeTabs);
    }

    private static void registerPayloadHandlers(RegisterPayloadHandlersEvent event) {
        event.registrar(CreateDelightCore.MODID)
                .versioned(PROTOCOL_VERSION)
                .playToServer(MiningKeyHintPayload.TYPE, MiningKeyHintPayload.STREAM_CODEC, MiningKeyHintPayload::handle)
                .playToServer(ChainCasingModifierPayload.TYPE, ChainCasingModifierPayload.STREAM_CODEC, ChainCasingModifierPayload::handle)
                .playToClient(SyncFoodValuesPayload.TYPE, SyncFoodValuesPayload.STREAM_CODEC, SyncFoodValuesPayload::handle)
                .playToClient(SyncFuelMapsPayload.TYPE, SyncFuelMapsPayload.STREAM_CODEC, SyncFuelMapsPayload::handle)
                .playToClient(SyncDisabledCreativeTabsPayload.TYPE, SyncDisabledCreativeTabsPayload.STREAM_CODEC,
                        SyncDisabledCreativeTabsPayload::handle);
    }

    private static void syncCreativeTabs(OnDatapackSyncEvent event) {
        var payload = new SyncDisabledCreativeTabsPayload(List.copyOf(DisabledCreativeTabs.snapshot()));
        if (event.getPlayer() != null) {
            PacketDistributor.sendToPlayer(event.getPlayer(), payload);
        } else {
            event.getRelevantPlayers().forEach(player -> PacketDistributor.sendToPlayer(player, payload));
        }
    }

    private static void clearCreativeTabs(ServerStoppedEvent event) {
        DisabledCreativeTabs.replace(Set.of());
    }

    private static void syncFuelMaps(OnDatapackSyncEvent event) {
        SyncFuelMapsPayload payload = SyncFuelMapsPayload.snapshot();
        if (event.getPlayer() != null) {
            PacketDistributor.sendToPlayer(event.getPlayer(), payload);
            return;
        }

        event.getRelevantPlayers().forEach(player -> PacketDistributor.sendToPlayer(player, payload));
    }
}
