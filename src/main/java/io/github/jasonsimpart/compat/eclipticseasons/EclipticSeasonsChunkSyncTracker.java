package io.github.jasonsimpart.compat.eclipticseasons;

import io.github.jasonsimpart.Config;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.ArrayDeque;
import java.util.Map;
import java.util.WeakHashMap;

public final class EclipticSeasonsChunkSyncTracker {
    private static final Map<ServerPlayer, ArrayDeque<LongSet>> PENDING_BATCHES = new WeakHashMap<>();
    private static final Map<ServerPlayer, LongSet> ACKNOWLEDGED_CHUNKS = new WeakHashMap<>();
    private static final ThreadLocal<BatchContext> CURRENT_BATCH = new ThreadLocal<>();
    private static final ThreadLocal<ChunkSendContext> CURRENT_CHUNK_SEND = new ThreadLocal<>();

    private EclipticSeasonsChunkSyncTracker() {
    }

    public static void beginBatch(ServerPlayer player) {
        if (enabled()) {
            CURRENT_BATCH.set(new BatchContext(player));
        }
    }

    public static synchronized void finishBatch(ServerPlayer player) {
        BatchContext context = CURRENT_BATCH.get();
        CURRENT_BATCH.remove();
        if (context != null && context.player == player && !context.chunks.isEmpty()) {
            PENDING_BATCHES.computeIfAbsent(player, ignored -> new ArrayDeque<>()).addLast(new LongOpenHashSet(context.chunks));
        }
    }

    public static void recordChunkInCurrentBatch(ServerPlayer player, LevelChunk chunk) {
        if (!enabled()) {
            return;
        }
        long chunkPos = chunk.getPos().toLong();
        BatchContext batchContext = CURRENT_BATCH.get();
        if (batchContext != null && batchContext.player == player) {
            batchContext.chunks.add(chunkPos);
        }
    }

    public static void beginInitialChunkAttachmentSync(ServerPlayer player, LevelChunk chunk) {
        if (!enabled()) {
            return;
        }
        long chunkPos = chunk.getPos().toLong();
        CURRENT_CHUNK_SEND.set(new ChunkSendContext(player, chunkPos));
    }

    public static void endInitialChunkAttachmentSync() {
        CURRENT_CHUNK_SEND.remove();
    }

    public static synchronized void acknowledgeBatch(ServerPlayer player) {
        if (!enabled()) {
            return;
        }
        ArrayDeque<LongSet> batches = PENDING_BATCHES.get(player);
        if (batches == null) {
            return;
        }
        LongSet acknowledgedBatch = batches.pollFirst();
        if (batches.isEmpty()) {
            PENDING_BATCHES.remove(player);
        }
        if (acknowledgedBatch != null && !acknowledgedBatch.isEmpty()) {
            ACKNOWLEDGED_CHUNKS.computeIfAbsent(player, ignored -> new LongOpenHashSet()).addAll(acknowledgedBatch);
        }
    }

    public static synchronized void dropChunk(ServerPlayer player, ChunkPos chunkPos) {
        long packedChunkPos = chunkPos.toLong();
        LongSet acknowledged = ACKNOWLEDGED_CHUNKS.get(player);
        if (acknowledged != null) {
            acknowledged.remove(packedChunkPos);
            if (acknowledged.isEmpty()) {
                ACKNOWLEDGED_CHUNKS.remove(player);
            }
        }
        ArrayDeque<LongSet> batches = PENDING_BATCHES.get(player);
        if (batches != null) {
            batches.forEach(batch -> batch.remove(packedChunkPos));
            batches.removeIf(LongSet::isEmpty);
            if (batches.isEmpty()) {
                PENDING_BATCHES.remove(player);
            }
        }
    }

    public static synchronized boolean canSendSnowyStatus(LevelChunk chunk, ServerPlayer player) {
        if (!enabled()) {
            return true;
        }
        if (player.level() != chunk.getLevel()) {
            return false;
        }
        long packedChunkPos = chunk.getPos().toLong();
        ChunkSendContext chunkSendContext = CURRENT_CHUNK_SEND.get();
        if (chunkSendContext != null && chunkSendContext.player == player && chunkSendContext.chunkPos == packedChunkPos) {
            return true;
        }
        if (!player.getChunkTrackingView().contains(chunk.getPos())) {
            return false;
        }
        if (player.connection == null || player.connection.chunkSender.isPending(packedChunkPos)) {
            return false;
        }
        LongSet acknowledged = ACKNOWLEDGED_CHUNKS.get(player);
        return acknowledged != null && acknowledged.contains(packedChunkPos);
    }

    private static boolean enabled() {
        return Config.ENABLE_ECLIPTIC_SEASONS_CHUNK_ATTACHMENT_SYNC_PATCH.get();
    }

    private static final class BatchContext {
        private final ServerPlayer player;
        private final LongSet chunks = new LongOpenHashSet();

        private BatchContext(ServerPlayer player) {
            this.player = player;
        }
    }

    private record ChunkSendContext(ServerPlayer player, long chunkPos) {
    }
}
