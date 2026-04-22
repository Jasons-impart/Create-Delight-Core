package io.github.jasonsimpart.createdelightcore.network;

import io.github.jasonsimpart.createdelightcore.CreateDelightCore;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class CDNetwork {
    private static final String PROTOCOL_VERSION = "1";

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
    }
}
