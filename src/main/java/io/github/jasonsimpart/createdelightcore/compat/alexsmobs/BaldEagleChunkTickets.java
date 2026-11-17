package io.github.jasonsimpart.createdelightcore.compat.alexsmobs;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Temporary, non-persistent chunk tickets for Alex's Mobs falconry.
 *
 * <p>Upstream {@code EntityBaldEagle.loadChunkOnServer} calls
 * {@link ServerLevel#setChunkForced(int, int, boolean)} on a 3×3 around the
 * eagle and never releases those chunks. That is the same persisted set as
 * {@code /forceload}, so the flight path stays loaded across restarts.
 *
 * <p>Create 6.0.8 trains do not use {@code setChunkForced} or region tickets
 * either: they keep an abstract railway graph and only materialize carriages
 * in already entity-ticking chunks ({@code carriageWaitingForChunks},
 * {@code CarriageEntityHandler.isActiveChunk}). Falconry still needs the
 * destination chunks actually loaded for the camera, so this helper uses the
 * vanilla non-persistent mechanism those tickets would have used:
 * {@link ServerChunkCache#addRegionTicket} / {@link ServerChunkCache#removeRegionTicket}
 * with a short-lived {@link TicketType}. Tickets are refreshed while the
 * player is flying the eagle (~every 10 ticks) and expire or are removed
 * when flight stops. Existing world forceloads are never cleared.
 */
public final class BaldEagleChunkTickets {
    public static final String TICKET_NAME = "createdelight:bald_eagle";
    /** Longer than the client refresh interval (dayTime % 10 == 0) plus lag margin. */
    public static final int TICKET_LIFETIME_TICKS = 40;
    /**
     * {@code addRegionTicket} radius 2 produces ticket level 31 (entity ticking)
     * on that one chunk, matching a vanilla forced-chunk ticket.
     */
    public static final int REGION_TICKET_RADIUS = 2;
    public static final TicketType<UUID> TICKET_TYPE = TicketType.create(
            TICKET_NAME,
            Comparator.comparingLong(UUID::getMostSignificantBits)
                    .thenComparingLong(UUID::getLeastSignificantBits),
            TICKET_LIFETIME_TICKS);

    private static final ConcurrentHashMap<UUID, Tracked> TRACKED = new ConcurrentHashMap<>();

    private BaldEagleChunkTickets() {
    }

    public static long[] chunksAroundBlock(int blockX, int blockZ) {
        return BaldEagleChunkGeometry.chunksAroundBlock(blockX, blockZ);
    }

    public static void refresh(ServerLevel level, UUID eagleId, int blockX, int blockZ) {
        Tracked tracked = TRACKED.computeIfAbsent(eagleId, unused -> new Tracked());
        ResourceKey<Level> dimension = level.dimension();
        if (tracked.dimension != null && !tracked.dimension.equals(dimension) && !tracked.chunks.isEmpty()) {
            ServerLevel previous = level.getServer().getLevel(tracked.dimension);
            if (previous != null) {
                removeAll(previous, eagleId, tracked.chunks);
            }
            tracked.chunks.clear();
        }
        tracked.dimension = dimension;

        long[] next = chunksAroundBlock(blockX, blockZ);
        Set<Long> nextSet = new HashSet<>(next.length);
        ServerChunkCache source = level.getChunkSource();
        for (long packed : next) {
            nextSet.add(packed);
            source.addRegionTicket(TICKET_TYPE, new ChunkPos(packed), REGION_TICKET_RADIUS, eagleId);
        }
        for (long old : tracked.chunks) {
            if (!nextSet.contains(old)) {
                source.removeRegionTicket(TICKET_TYPE, new ChunkPos(old), REGION_TICKET_RADIUS, eagleId);
            }
        }
        tracked.chunks.clear();
        tracked.chunks.addAll(nextSet);
    }

    public static void release(ServerLevel level, UUID eagleId) {
        Tracked tracked = TRACKED.remove(eagleId);
        if (tracked == null || tracked.chunks.isEmpty()) {
            return;
        }
        if (tracked.dimension == null || tracked.dimension.equals(level.dimension())) {
            removeAll(level, eagleId, tracked.chunks);
            return;
        }
        ServerLevel previous = level.getServer().getLevel(tracked.dimension);
        if (previous != null) {
            removeAll(previous, eagleId, tracked.chunks);
        }
    }

    private static void removeAll(ServerLevel level, UUID eagleId, Set<Long> chunks) {
        ServerChunkCache source = level.getChunkSource();
        for (long packed : chunks) {
            source.removeRegionTicket(TICKET_TYPE, new ChunkPos(packed), REGION_TICKET_RADIUS, eagleId);
        }
    }

    private static final class Tracked {
        private ResourceKey<Level> dimension;
        private final Set<Long> chunks = new HashSet<>();
    }
}
