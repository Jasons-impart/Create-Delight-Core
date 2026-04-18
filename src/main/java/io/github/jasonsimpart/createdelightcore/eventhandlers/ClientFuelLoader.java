package io.github.jasonsimpart.createdelightcore.eventhandlers;

import com.forsteri.createliquidfuel.core.BurnerStomachHandler;
import io.github.jasonsimpart.createdelightcore.compat.cmr.CoolerStomachHandler;
import io.github.jasonsimpart.createdelightcore.compat.cmr.LiquidCoolerFuelJsonLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import static io.github.jasonsimpart.createdelightcore.CreateDelightCore.MODID;

/**
 * Client-side fuel data loader.
 * Loads liquid fuel data from local ResourceManager on client startup,
 * ensuring tooltip data is available when playing on servers.
 */
@Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientFuelLoader {

    private static boolean loaded = false;

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            if (loaded) return;
            loadClientFuelData();
            loaded = true;
        });
    }

    /**
     * Load fuel data from client's local ResourceManager.
     * This ensures tooltip info is available when connecting to servers.
     */
    public static void loadClientFuelData() {
        ResourceManager rm = Minecraft.getInstance().getResourceManager();

        // Load snowman cooler fuel (CDC mod)
        LiquidCoolerFuelJsonLoader.loadFromResourceManager(rm);

        // Load blaze burner fuel (create-liquid-fuel mod)
        loadBlazeBurnerFuel(rm);
    }

    /**
     * Load blaze burner fuel data from datapack.
     * Handles data for create-liquid-fuel mod's BurnerStomachHandler.
     */
    private static void loadBlazeBurnerFuel(ResourceManager rm) {
        // Only load if the map is empty (client-side)
        if (!BurnerStomachHandler.LIQUID_BURNER_FUEL_MAP.isEmpty()) {
            return;
        }

        LiquidCoolerFuelJsonLoader.loadBlazeBurnerFuelFromResourceManager(rm);
    }
}