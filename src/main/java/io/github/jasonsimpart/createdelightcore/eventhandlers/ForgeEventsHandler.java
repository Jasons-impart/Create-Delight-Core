package io.github.jasonsimpart.createdelightcore.eventhandlers;

import io.github.jasonsimpart.createdelightcore.compat.cmr.LiquidCoolerFuelJsonLoader;
import io.github.jasonsimpart.createdelightcore.content.configuration.ConfigurationModuleManager;
import io.github.jasonsimpart.createdelightcore.content.order.data.OrderDataManager;
import io.github.jasonsimpart.createdelightcore.network.CDNetwork;
import io.github.jasonsimpart.createdelightcore.network.SyncFuelMapsPacket;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber
public class ForgeEventsHandler {
    @SubscribeEvent
    public static void addReloadListeners(AddReloadListenerEvent event) {
        event.addListener(LiquidCoolerFuelJsonLoader.INSTANCE);
        event.addListener(OrderDataManager.INSTANCE);
        event.addListener(ConfigurationModuleManager.MODULE_RELOAD_LISTENER);
        event.addListener(ConfigurationModuleManager.MODE_RELOAD_LISTENER);
    }

    @SubscribeEvent
    public static void onDatapackSync(OnDatapackSyncEvent event) {
        SyncFuelMapsPacket packet = new SyncFuelMapsPacket();
        if (event.getPlayer() != null) {
            CDNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(event::getPlayer), packet);
        } else {
            CDNetwork.CHANNEL.send(PacketDistributor.ALL.noArg(), packet);
        }
    }
}
