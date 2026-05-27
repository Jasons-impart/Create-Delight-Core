package io.github.jasonsimpart.network;

import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public final class ClientFuelCache {
    public static final Map<Fluid, FuelData> BURNER_MAP = new HashMap<>();
    public static final Map<Fluid, FuelData> COOLER_MAP = new HashMap<>();

    @Nullable
    public static Runnable onUpdate;

    private ClientFuelCache() {
    }

    public record FuelData(int burnTime, boolean strongHeat, int amountConsumed) {
    }
}
