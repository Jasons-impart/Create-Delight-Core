package io.github.jasonsimpart.mixin.alexscaves;

import com.github.alexmodguy.alexscaves.server.level.biome.ACBiomeRarity;
import com.github.alexmodguy.alexscaves.server.level.biome.ACWorldSeedHolder;
import io.github.jasonsimpart.compat.alexscaves.AlexsCavesDimensionOverrides;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ACBiomeRarity.class, remap = false)
public class ACBiomeRarityMixin {
    @Inject(method = "getACBiomeForPosition", at = @At("HEAD"), cancellable = true)
    private static void createdelightcore$getACBiomeForDimension(long worldSeed, int blockX, int blockZ, CallbackInfoReturnable<ResourceKey<Biome>> cir) {
        ResourceKey<Biome> biome = AlexsCavesDimensionOverrides.biomeForDimension(ACWorldSeedHolder.getDimension());
        if (biome != null) {
            cir.setReturnValue(biome);
        }
    }

    @Inject(method = "getACBiomeCenterForPosition", at = @At("HEAD"), cancellable = true)
    private static void createdelightcore$getACBiomeCenterForDimension(long worldSeed, int blockX, int blockZ, CallbackInfoReturnable<Vec3> cir) {
        ResourceKey<Level> dimension = ACWorldSeedHolder.getDimension();
        if (AlexsCavesDimensionOverrides.isAlexsCavesDimension(dimension)) {
            cir.setReturnValue(new Vec3(blockX >> 2, 0.0D, blockZ >> 2));
        }
    }
}
