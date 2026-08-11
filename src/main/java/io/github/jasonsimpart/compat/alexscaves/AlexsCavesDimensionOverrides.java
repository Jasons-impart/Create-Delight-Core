package io.github.jasonsimpart.compat.alexscaves;

import io.github.jasonsimpart.Config;
import io.github.jasonsimpart.CreateDelightCore;
import java.util.Map;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

public final class AlexsCavesDimensionOverrides {
    private static final ThreadLocal<ResourceKey<Level>> WORLDGEN_DIMENSION = new ThreadLocal<>();
    private static final Map<ResourceKey<Level>, ResourceKey<Biome>> DIMENSION_BIOMES =
            Map.of(
                    dimension("ceres_dimension"), alexsCavesBiome("candy_cavity"),
                    dimension("enceladus_dimension"), alexsCavesBiome("abyssal_chasm"),
                    dimension("pluto_dimension"), alexsCavesBiome("forlorn_hollows"),
                    northstarDimension("mercury"), alexsCavesBiome("magnetic_caves"),
                    northstarDimension("venus"), alexsCavesBiome("toxic_caves"),
                    northstarDimension("mars"), alexsCavesBiome("primordial_caves"));

    private AlexsCavesDimensionOverrides() {
    }

    public static ResourceKey<Biome> biomeForDimension(ResourceKey<Level> dimension) {
        if (!Config.ENABLE_ALEXSCAVES_DIMENSION_BIOME_OVERRIDES.get()) {
            return null;
        }
        return DIMENSION_BIOMES.get(dimension);
    }

    public static boolean isAlexsCavesDimension(ResourceKey<Level> dimension) {
        return biomeForDimension(dimension) != null;
    }

    public static WorldgenContext pushWorldgenContext(ResourceKey<Level> dimension, long seed) {
        ResourceKey<Level> previousDimension = WORLDGEN_DIMENSION.get();
        boolean replaced = isAlexsCavesDimension(dimension);
        if (replaced) {
            WORLDGEN_DIMENSION.set(dimension);
        }
        return new WorldgenContext(previousDimension, replaced);
    }

    public static void popWorldgenContext(WorldgenContext context) {
        if (!context.replaced()) {
            return;
        }
        if (context.previousDimension() == null) {
            WORLDGEN_DIMENSION.remove();
        } else {
            WORLDGEN_DIMENSION.set(context.previousDimension());
        }
    }

    public static ResourceKey<Biome> biomeForCurrentWorldgenContext() {
        return biomeForDimension(WORLDGEN_DIMENSION.get());
    }

    private static ResourceKey<Level> dimension(String name) {
        return ResourceKey.create(Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath(CreateDelightCore.MODID, name));
    }

    private static ResourceKey<Level> northstarDimension(String name) {
        return ResourceKey.create(Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath("northstar", name));
    }

    private static ResourceKey<Biome> alexsCavesBiome(String name) {
        return ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("alexscavesup", name));
    }

    public record WorldgenContext(ResourceKey<Level> previousDimension, boolean replaced) {
    }
}
