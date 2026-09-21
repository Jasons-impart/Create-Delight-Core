package io.github.jasonsimpart.mixin.minecraft;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.jasonsimpart.Config;
import io.github.jasonsimpart.content.worldgen.ShallowCaveDensityFunction;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.NoiseRouterData;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(NoiseRouterData.class)
public abstract class NoiseRouterDataMixin {
    private static final ResourceKey<DensityFunction> SLOPED_CHEESE = ResourceKey.create(
            Registries.DENSITY_FUNCTION,
            ResourceLocation.withDefaultNamespace("overworld/sloped_cheese")
    );

    @ModifyReturnValue(method = "entrances", at = @At("RETURN"))
    private static DensityFunction createdelightcore$suppressShallowCaveEntrances(
            DensityFunction original,
            HolderGetter<DensityFunction> densityFunctions,
            HolderGetter<NormalNoise.NoiseParameters> noiseParameters
    ) {
        int depth = Config.SPEC.isLoaded()
                ? Config.SURFACE_CAVE_OPTIMIZATION_DEPTH.get()
                : Config.SURFACE_CAVE_OPTIMIZATION_DEPTH.getDefault();
        if (depth <= 0) {
            return original;
        }

        DensityFunction surfaceDensity = new DensityFunctions.HolderHolder(densityFunctions.getOrThrow(SLOPED_CHEESE));
        return DensityFunctions.cacheOnce(new ShallowCaveDensityFunction(original, surfaceDensity, depth));
    }
}
