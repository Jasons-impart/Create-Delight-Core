package io.github.jasonsimpart.compat.cmr;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;

public final class CmrCompat {
    public static final String MOD_ID = "cmr";
    public static final ResourceLocation SNOWMAN_COOLER = ResourceLocation.fromNamespaceAndPath(MOD_ID, "snowman_cooler");

    private CmrCompat() {
    }

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(CmrCompat::commonSetup);
        modEventBus.addListener(CmrCompat::registerCapabilities);
        NeoForge.EVENT_BUS.addListener(CmrCompat::addReloadListeners);
    }

    private static void commonSetup(FMLCommonSetupEvent event) {
        if (isLoaded()) {
            event.enqueueWork(DrainableFuelLoader::load);
        }
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        if (!isLoaded()) {
            return;
        }

        BuiltInRegistries.BLOCK.getOptional(SNOWMAN_COOLER)
                .ifPresent(block -> event.registerBlock(
                        Capabilities.FluidHandler.BLOCK,
                        (level, pos, state, blockEntity, side) -> blockEntity instanceof CoolerStomachAccess access
                                ? access.createdelightcore$getStomach()
                                : null,
                        block
                ));
    }

    private static void addReloadListeners(AddReloadListenerEvent event) {
        if (isLoaded()) {
            event.addListener(LiquidCoolerFuelJsonLoader.INSTANCE);
        }
    }

    private static boolean isLoaded() {
        return ModList.get().isLoaded(MOD_ID);
    }
}
