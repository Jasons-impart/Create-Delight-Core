package io.github.jasonsimpart.network;

import io.github.jasonsimpart.CreateDelightCore;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public final class ModNetwork {
    private static final String PROTOCOL_VERSION = "1";

    private ModNetwork() {
    }

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(ModNetwork::registerPayloadHandlers);
        NeoForge.EVENT_BUS.addListener(ModNetwork::syncFuelMaps);
    }

    private static void registerPayloadHandlers(RegisterPayloadHandlersEvent event) {
        event.registrar(CreateDelightCore.MODID)
                .versioned(PROTOCOL_VERSION)
                .playToClient(SyncFuelMapsPayload.TYPE, SyncFuelMapsPayload.STREAM_CODEC, SyncFuelMapsPayload::handle);
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
