package io.github.jasonsimpart.compat.cmr;

import fr.iglee42.cmr.init.CMRRegistries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;

public final class CmrCompat {
    public static final String MOD_ID = "cmr";

    private CmrCompat() {
    }

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(CmrCompat::commonSetup);
        modEventBus.addListener(CmrCompat::registerCapabilities);
        NeoForge.EVENT_BUS.addListener(CmrCompat::addReloadListeners);
    }

    private static void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(DrainableFuelLoader::load);
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlock(
                Capabilities.FluidHandler.BLOCK,
                (level, pos, state, blockEntity, side) -> blockEntity instanceof CoolerStomachAccess access
                        ? access.createdelightcore$getStomach()
                        : null,
                CMRRegistries.SNOWMAN_COOLER.get()
        );
    }

    private static void addReloadListeners(AddReloadListenerEvent event) {
        event.addListener(LiquidCoolerFuelJsonLoader.INSTANCE);
    }
}
