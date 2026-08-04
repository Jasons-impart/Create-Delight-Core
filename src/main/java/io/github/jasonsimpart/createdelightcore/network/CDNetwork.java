package io.github.jasonsimpart.createdelightcore.network;

import io.github.jasonsimpart.createdelightcore.CreateDelightCore;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class CDNetwork {
    private static final String PROTOCOL_VERSION = "2";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            ResourceLocation.fromNamespaceAndPath(CreateDelightCore.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void register() {
        int id = 0;
        CHANNEL.registerMessage(
                id++,
                SyncFuelMapsPacket.class,
                SyncFuelMapsPacket::encode,
                SyncFuelMapsPacket::decode,
                SyncFuelMapsPacket::handle
        );
        CHANNEL.registerMessage(
                id++,
                RequestOrderCandidatesPacket.class,
                RequestOrderCandidatesPacket::encode,
                RequestOrderCandidatesPacket::decode,
                RequestOrderCandidatesPacket::handle
        );
        CHANNEL.registerMessage(
                id++,
                SyncOrderCandidatesPacket.class,
                SyncOrderCandidatesPacket::encode,
                SyncOrderCandidatesPacket::decode,
                SyncOrderCandidatesPacket::handle
        );
        CHANNEL.registerMessage(
                id++,
                SetOrderRequestPacket.class,
                SetOrderRequestPacket::encode,
                SetOrderRequestPacket::decode,
                SetOrderRequestPacket::handle
        );
        CHANNEL.registerMessage(
                id++,
                CycleConfigurationModePacket.class,
                CycleConfigurationModePacket::encode,
                CycleConfigurationModePacket::decode,
                CycleConfigurationModePacket::handle
        );
    }
}
