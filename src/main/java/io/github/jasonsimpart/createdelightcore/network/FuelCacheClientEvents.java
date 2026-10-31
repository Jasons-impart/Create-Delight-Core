package io.github.jasonsimpart.createdelightcore.network;

import io.github.jasonsimpart.createdelightcore.CreateDelightCore;
import io.github.jasonsimpart.createdelightcore.util.GoggleBurnTimeTicker;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CreateDelightCore.MODID, value = Dist.CLIENT)
public final class FuelCacheClientEvents {
    private FuelCacheClientEvents() {}

    @SubscribeEvent
    public static void logout(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientFuelCache.COOLER_MAP.clear();
        ClientFuelCache.BURNER_MAP.clear();
        GoggleBurnTimeTicker.clear();
        if (ClientFuelCache.onUpdate != null) ClientFuelCache.onUpdate.run();
    }
}
