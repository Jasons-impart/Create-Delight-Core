package io.github.jasonsimpart.compat.northstar;

import io.github.jasonsimpart.CreateDelightCore;
import net.neoforged.fml.ModList;

public final class NorthstarCompat {
    private static final String NORTHSTAR_MOD_ID = "northstar";

    private NorthstarCompat() {
    }

    public static void register() {
        if (!ModList.get().isLoaded(NORTHSTAR_MOD_ID)) {
            return;
        }

        try {
            Class.forName("io.github.jasonsimpart.compat.northstar.NorthstarPlanetIntegration")
                    .getMethod("register")
                    .invoke(null);
        } catch (ReflectiveOperationException exception) {
            CreateDelightCore.LOGGER.error("Failed to register Create Delight Core Northstar planet integration", exception);
        }
    }
}
