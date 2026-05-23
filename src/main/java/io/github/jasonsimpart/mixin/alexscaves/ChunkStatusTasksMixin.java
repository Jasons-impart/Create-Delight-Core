package io.github.jasonsimpart.mixin.alexscaves;

import io.github.jasonsimpart.compat.alexscaves.AlexsCavesDimensionOverrides;
import java.util.concurrent.CompletableFuture;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.GenerationChunkHolder;
import net.minecraft.util.StaticCache2D;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatusTasks;
import net.minecraft.world.level.chunk.status.ChunkStep;
import net.minecraft.world.level.chunk.status.WorldGenContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkStatusTasks.class)
public class ChunkStatusTasksMixin {
    @Unique
    private static final ThreadLocal<ResourceKey<Level>> CDC_PREVIOUS_DIMENSION = new ThreadLocal<>();

    @Inject(
            method = "generateStructureStarts(Lnet/minecraft/world/level/chunk/status/WorldGenContext;Lnet/minecraft/world/level/chunk/status/ChunkStep;Lnet/minecraft/util/StaticCache2D;Lnet/minecraft/world/level/chunk/ChunkAccess;)Ljava/util/concurrent/CompletableFuture;",
            at = @At("HEAD"))
    private static void createdelightcore$rememberAlexsCavesStructureDimension(
            WorldGenContext worldGenContext,
            ChunkStep step,
            StaticCache2D<GenerationChunkHolder> cache,
            ChunkAccess chunk,
            CallbackInfoReturnable<CompletableFuture<ChunkAccess>> cir) {
        ResourceKey<Level> previousDimension = AlexsCavesDimensionOverrides.pushWorldgenContext(
                worldGenContext.level().dimension(),
                worldGenContext.level().getSeed());
        if (previousDimension != null) {
            CDC_PREVIOUS_DIMENSION.set(previousDimension);
        }
    }

    @Inject(
            method = "generateStructureStarts(Lnet/minecraft/world/level/chunk/status/WorldGenContext;Lnet/minecraft/world/level/chunk/status/ChunkStep;Lnet/minecraft/util/StaticCache2D;Lnet/minecraft/world/level/chunk/ChunkAccess;)Ljava/util/concurrent/CompletableFuture;",
            at = @At("RETURN"))
    private static void createdelightcore$restoreAlexsCavesStructureDimension(
            WorldGenContext worldGenContext,
            ChunkStep step,
            StaticCache2D<GenerationChunkHolder> cache,
            ChunkAccess chunk,
            CallbackInfoReturnable<CompletableFuture<ChunkAccess>> cir) {
        AlexsCavesDimensionOverrides.popWorldgenContext(CDC_PREVIOUS_DIMENSION.get());
        CDC_PREVIOUS_DIMENSION.remove();
    }

}
