package io.github.jasonsimpart.createdelightcore.content.humidity;

import com.teamtea.eclipticseasons.config.CommonConfig;
import io.github.jasonsimpart.createdelightcore.network.CDNetwork;
import io.github.jasonsimpart.createdelightcore.network.SyncHumidityRoomsPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

/** Fixed-range providers; no room geometry or per-position humidity cache. */
public final class SmartHumidityRegulatorSeasonCompat {
    public static final int RADIUS = 4;
    // Immutable active device entries are safe for detector/network readers.
    private static final Map<ServerLevel, Map<Long, Float>> PROVIDERS = new ConcurrentHashMap<>();
    private static final Map<ServerPlayer, ReadingSnapshot> SENT = new WeakHashMap<>();
    private record ReadingSnapshot(ServerLevel level, Map<Long, Float> values) { }
    private SmartHumidityRegulatorSeasonCompat() { }

    public static void update(ServerLevel level, SmartHumidityRegulatorBlockEntity owner, boolean active) {
        long key = owner.getBlockPos().asLong();
        Float value = active && CommonConfig.Crop.enableCropHumidityControl.get()
                ? owner.getTargetHumidity() + .5F : null;
        Map<Long, Float> old = PROVIDERS.getOrDefault(level, Map.of());
        if (java.util.Objects.equals(old.get(key), value)) return;
        Map<Long, Float> next = new HashMap<>(old);
        if (value == null) next.remove(key); else next.put(key, value);
        PROVIDERS.put(level, Map.copyOf(next));
    }

    public static void remove(ServerLevel level, SmartHumidityRegulatorBlockEntity owner) {
        update(level, owner, false);
    }

    @Nullable
    public static Float target(ServerLevel level, BlockPos pos) {
        return target(PROVIDERS.getOrDefault(level, Map.of()), pos);
    }

    /** Exact 9x9x9 box; overlapping active providers select the highest level. */
    @Nullable
    public static Float target(Map<Long, Float> providers, BlockPos pos) {
        Float highest = null;
        for (var entry : providers.entrySet()) {
            BlockPos center = BlockPos.of(entry.getKey());
            if (Math.abs((long) pos.getX() - center.getX()) <= RADIUS
                    && Math.abs((long) pos.getY() - center.getY()) <= RADIUS
                    && Math.abs((long) pos.getZ() - center.getZ()) <= RADIUS
                    && (highest == null || entry.getValue() > highest)) highest = entry.getValue();
        }
        return highest;
    }

    public static void syncReadings(ServerLevel level) {
        double distance = (level.getServer().getPlayerList().getViewDistance() + 2) * 16D;
        for (var player : level.players()) {
            Map<Long, Float> nearby = new HashMap<>();
            PROVIDERS.getOrDefault(level, Map.of()).forEach((key, value) -> {
                if (BlockPos.of(key).closerToCenterThan(player.position(), distance)) nearby.put(key, value);
            });
            Map<Long, Float> values = Map.copyOf(nearby);
            ReadingSnapshot previous = SENT.get(player);
            if (previous == null || previous.level != level || !previous.values.equals(values)) {
                CDNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                        new SyncHumidityRoomsPacket(level.dimension().location(), values));
                SENT.put(player, new ReadingSnapshot(level, values));
            }
        }
    }

    public static void chunkUnloaded(ServerLevel level, int chunkX, int chunkZ) {
        Map<Long, Float> next = new HashMap<>(PROVIDERS.getOrDefault(level, Map.of()));
        if (next.keySet().removeIf(key -> {
            BlockPos pos = BlockPos.of(key);
            return pos.getX() >> 4 == chunkX && pos.getZ() >> 4 == chunkZ;
        })) PROVIDERS.put(level, Map.copyOf(next));
    }

    public static void levelUnloaded(ServerLevel level) {
        PROVIDERS.remove(level);
        SENT.entrySet().removeIf(entry -> entry.getValue().level == level);
    }
}
