package io.github.jasonsimpart.server;

import io.github.jasonsimpart.Config;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class DropReportEvents {
    private static int lastReportTick = -1;

    private DropReportEvents() {
    }

    public static void onServerTick(ServerTickEvent.Post event) {
        if (Config.DISABLE_DROP_REPORT.get()) {
            return;
        }

        MinecraftServer server = event.getServer();
        List<ServerPlayer> players = server.getPlayerList().getPlayers();
        if (players.isEmpty()) {
            return;
        }

        int tick = server.getTickCount();
        int interval = Config.DROP_REPORT_INTERVAL_TICKS.get();
        if (tick == lastReportTick || tick % interval != 0) {
            return;
        }

        lastReportTick = tick;
        reportDrops(server, players);
    }

    private static void reportDrops(MinecraftServer server, List<ServerPlayer> players) {
        Map<ChunkKey, Integer> counts = new HashMap<>();

        server.getAllLevels().forEach(level -> level.getEntities().getAll().forEach(entity -> {
            if (entity instanceof ItemEntity itemEntity) {
                ChunkKey chunk = new ChunkKey(
                        level.dimension().location().toString(),
                        itemEntity.chunkPosition().x,
                        itemEntity.chunkPosition().z
                );
                int count = Config.DROP_REPORT_IGNORE_STACK_COUNT.get() ? 1 : itemEntity.getItem().getCount();
                counts.merge(chunk, count, Integer::sum);
            }
        }));

        int threshold = Config.DROP_REPORT_ITEM_THRESHOLD.get();
        List<ChunkDropCount> reports = counts.entrySet().stream()
                .filter(entry -> entry.getValue() > threshold)
                .map(entry -> new ChunkDropCount(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparingInt(ChunkDropCount::count).reversed())
                .limit(Config.DROP_REPORT_MAX_CHUNKS.get())
                .toList();

        if (reports.isEmpty()) {
            return;
        }

        players.forEach(player -> {
            player.sendSystemMessage(Component.literal("掉落物报告:"));
            reports.forEach(report -> player.sendSystemMessage(Component.literal(
                    "World: " + report.chunk().dimension()
                            + " Chunk: [" + report.chunk().x() + ", " + report.chunk().z() + "]"
                            + " count: " + report.count()
            )));
        });
    }

    private record ChunkKey(String dimension, int x, int z) {
    }

    private record ChunkDropCount(ChunkKey chunk, int count) {
    }
}
