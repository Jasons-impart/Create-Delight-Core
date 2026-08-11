package io.github.jasonsimpart.mixin.alexscaves;

import com.github.alexmodguy.alexscaves.server.level.biome.BiomeSourceAccessor;
import io.github.jasonsimpart.compat.alexscaves.AlexsCavesDimensionOverrides;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiNoiseBiomeSource.class)
public abstract class MultiNoiseBiomeSourceMixin {
    @Inject(method = "getNoiseBiome", at = @At("HEAD"), cancellable = true)
    private void createdelightcore$selectBiomeForConfiguredDimension(
            int quartX,
            int quartY,
            int quartZ,
            Climate.Sampler sampler,
            CallbackInfoReturnable<Holder<Biome>> cir) {
        ResourceKey<Biome> biomeKey = AlexsCavesDimensionOverrides.biomeForCurrentWorldgenContext();
        if (biomeKey == null) {
            return;
        }

        Holder<Biome> biome = ((BiomeSourceAccessor) (Object) this).getResourceKeyMap().get(biomeKey);
        if (biome != null) {
            cir.setReturnValue(biome);
        }
    }
}
