package io.github.jasonsimpart.test;

import io.github.jasonsimpart.CreateDelightCore;
import io.github.jasonsimpart.util.ModIds;
import io.github.jasonsimpart.util.OptionalMods;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;

@EventBusSubscriber(modid = CreateDelightCore.MODID)
public final class GametestBootstrap {
    private GametestBootstrap() {
    }

    @SubscribeEvent
    public static void register(RegisterGameTestsEvent event) {
        // NeoForge 21.1 discovers holders before filtering namespaces. MBD2's bundled
        // optional integration tests therefore crash discovery without their mods.
        // For an explicitly Core-only development test run, omit those foreign holders.
        if (CreateDelightCore.MODID.equals(System.getProperty("neoforge.enabledGameTestNamespaces"))) {
            for (var scan : ModList.get().getAllScanData()) {
                scan.getAnnotations().removeIf(annotation -> annotation.clazz().getClassName().startsWith("com.lowdragmc.mbd2.test.")
                        && annotation.annotationType().getClassName().equals("net.neoforged.neoforge.gametest.GameTestHolder"));
            }
        }
        if (OptionalMods.isLoaded(ModIds.MBD2)) {
            registerMbd2Tests(event);
        }
        if (OptionalMods.allLoaded(ModIds.IMPROVED_MOBS, ModIds.LIGHTMANS_CURRENCY)) {
            event.register(io.github.jasonsimpart.compat.improvedmobs.MechanicsTests.class);
        }
    }

    private static void registerMbd2Tests(RegisterGameTestsEvent event) {
        event.register(io.github.jasonsimpart.compat.mbd2.MbdMachineTests.class);
        event.register(io.github.jasonsimpart.compat.mbd2.MbdSingleMachineTests.class);
        if (OptionalMods.isLoaded(ModIds.ECLIPTIC_SEASONS)) {
            event.register(io.github.jasonsimpart.compat.mbd2.MbdClimateTests.class);
        }
        if (OptionalMods.isLoaded(ModIds.LIGHTMANS_CURRENCY)) {
            event.register(io.github.jasonsimpart.compat.mbd2.MbdEconomyTests.class);
        }
        event.register(io.github.jasonsimpart.compat.mbd2.MbdHydropowerTests.class);
        event.register(io.github.jasonsimpart.compat.mbd2.MbdAssemblyTests.class);
        event.register(io.github.jasonsimpart.compat.mbd2.MbdCentrifugeTests.class);
        event.register(io.github.jasonsimpart.compat.mbd2.MbdReactorTests.class);
        if (OptionalMods.isLoaded(ModIds.BUTCHERCRAFT)) {
            event.register(io.github.jasonsimpart.compat.mbd2.MbdButcheryTests.class);
        }
    }
}
