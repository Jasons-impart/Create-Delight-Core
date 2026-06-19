package io.github.jasonsimpart.compat.createliquidfuel;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

public final class CreateLiquidFuelCompat {
    private CreateLiquidFuelCompat() {
    }

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(CreateLiquidFuelCompat::commonSetup);
    }

    private static void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(DrainableBurnerFuelLoader::load);
    }
}
