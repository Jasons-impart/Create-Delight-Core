package io.github.jasonsimpart.compat.alexscaves;

import com.github.alexmodguy.alexscaves.server.level.biome.ACWorldSeedHolder;
import io.github.jasonsimpart.Config;
import io.github.jasonsimpart.CreateDelightCore;
import java.util.Map;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

public final class AlexsCavesDimensionOverrides {
    private static final Map<ResourceKey<Level>, ResourceKey<Biome>> DIMENSION_BIOMES =
            Map.of(
                    dimension("magnetic_caves_dimension"), alexsCavesBiome("magnetic_caves"),
                    dimension("primordial_caves_dimension"), alexsCavesBiome("primordial_caves"),
                    dimension("toxic_caves_dimension"), alexsCavesBiome("toxic_caves"),
                    dimension("abyssal_chasm_dimension"), alexsCavesBiome("abyssal_chasm"),
                    dimension("forlorn_hollows_dimension"), alexsCavesBiome("forlorn_hollows"),
                    dimension("candy_cavity_dimension"), alexsCavesBiome("candy_cavity"));

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

    public static ResourceKey<Level> pushWorldgenContext(ResourceKey<Level> dimension, long seed) {
        if (!isAlexsCavesDimension(dimension)) {
            return null;
        }

        ResourceKey<Level> previousDimension = ACWorldSeedHolder.getDimension();
        ACWorldSeedHolder.setSeed(seed);
        ACWorldSeedHolder.setDimension(dimension);
        return previousDimension;
    }

    public static void popWorldgenContext(ResourceKey<Level> previousDimension) {
        if (previousDimension != null) {
            ACWorldSeedHolder.setDimension(previousDimension);
        }
    }

    private static ResourceKey<Level> dimension(String name) {
        return ResourceKey.create(Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath(CreateDelightCore.MODID, name));
    }

    private static ResourceKey<Biome> alexsCavesBiome(String name) {
        return ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("alexscaves", name));
    }
}
