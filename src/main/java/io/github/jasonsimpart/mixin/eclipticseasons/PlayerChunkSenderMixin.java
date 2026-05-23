package io.github.jasonsimpart.mixin.eclipticseasons;

import io.github.jasonsimpart.compat.eclipticseasons.EclipticSeasonsChunkSyncTracker;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.PlayerChunkSender;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerChunkSender.class)
public abstract class PlayerChunkSenderMixin {
    @Inject(method = "sendNextChunks", at = @At("HEAD"))
    private void cdc$beginEclipticSeasonsChunkBatch(ServerPlayer player, CallbackInfo ci) {
        EclipticSeasonsChunkSyncTracker.beginBatch(player);
    }

    @Inject(method = "sendNextChunks", at = @At("TAIL"))
    private void cdc$finishEclipticSeasonsChunkBatch(ServerPlayer player, CallbackInfo ci) {
        EclipticSeasonsChunkSyncTracker.finishBatch(player);
    }

    @Inject(method = "sendChunk", at = @At("HEAD"))
    private static void cdc$recordEclipticSeasonsSentChunk(ServerGamePacketListenerImpl packetListener, ServerLevel level, LevelChunk chunk, CallbackInfo ci) {
        EclipticSeasonsChunkSyncTracker.recordChunkInCurrentBatch(packetListener.player, chunk);
    }

    @Inject(
            method = "sendChunk",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/neoforged/neoforge/event/EventHooks;fireChunkSent(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/level/chunk/LevelChunk;Lnet/minecraft/server/level/ServerLevel;)V"
            )
    )
    private static void cdc$beginEclipticSeasonsInitialAttachmentSync(ServerGamePacketListenerImpl packetListener, ServerLevel level, LevelChunk chunk, CallbackInfo ci) {
        EclipticSeasonsChunkSyncTracker.beginInitialChunkAttachmentSync(packetListener.player, chunk);
    }

    @Inject(
            method = "sendChunk",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/neoforged/neoforge/event/EventHooks;fireChunkSent(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/level/chunk/LevelChunk;Lnet/minecraft/server/level/ServerLevel;)V",
                    shift = At.Shift.AFTER
            )
    )
    private static void cdc$endEclipticSeasonsInitialAttachmentSync(ServerGamePacketListenerImpl packetListener, ServerLevel level, LevelChunk chunk, CallbackInfo ci) {
        EclipticSeasonsChunkSyncTracker.endInitialChunkAttachmentSync();
    }

    @Inject(method = "dropChunk", at = @At("HEAD"))
    private void cdc$dropEclipticSeasonsKnownChunk(ServerPlayer player, ChunkPos chunkPos, CallbackInfo ci) {
        EclipticSeasonsChunkSyncTracker.dropChunk(player, chunkPos);
    }
}
