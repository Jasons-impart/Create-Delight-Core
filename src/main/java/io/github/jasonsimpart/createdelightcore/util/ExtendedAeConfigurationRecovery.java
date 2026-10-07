package io.github.jasonsimpart.createdelightcore.util;

import com.mojang.logging.LogUtils;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ConfigTracker;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import org.slf4j.Logger;

import java.util.Objects;
import java.util.function.Consumer;

/** Reuses ExtendedAE's cache loader only when Forge has already loaded its real configuration. */
public final class ExtendedAeConfigurationRecovery {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static ForgeConfigSpec targetSpec;
    private static Consumer<ModConfigEvent> configLoader;

    private ExtendedAeConfigurationRecovery() {
    }

    public static synchronized void register(ForgeConfigSpec spec, Consumer<ModConfigEvent> loader) {
        targetSpec = Objects.requireNonNull(spec, "spec");
        configLoader = Objects.requireNonNull(loader, "loader");
    }

    public static void loadRegisteredConfig() {
        ForgeConfigSpec spec;
        Consumer<ModConfigEvent> loader;
        synchronized (ExtendedAeConfigurationRecovery.class) {
            spec = targetSpec;
            loader = configLoader;
        }
        if (spec == null || loader == null) {
            throw new IllegalStateException("ExtendedAE configuration recovery hook did not run");
        }
        ModConfig config = ConfigTracker.INSTANCE.configSets().get(ModConfig.Type.COMMON).stream()
                .filter(candidate -> "expatternprovider".equals(candidate.getModId()) && candidate.getSpec() == spec)
                .findFirst().orElseThrow(() -> new IllegalStateException("ExtendedAE common configuration is not registered"));
        if (config.getConfigData() == null || !spec.isLoaded() || spec.isCorrecting()) {
            throw new IllegalStateException("ExtendedAE configuration is not ready for cache initialization: "
                    + config.getFileName());
        }
        LOGGER.warn("[CDCore][ExtendedAE] Config data is loaded but its initialization notification is missing; "
                + "rebuilding caches from {}", config.getFileName());
        // The original onLoad method still validates/converts values and signals readiness through its mixin.
        loader.accept(new ModConfigEvent.Loading(config));
    }
}
