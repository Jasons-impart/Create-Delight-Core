package io.github.jasonsimpart.createdelightcore.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

/**
 * goggle tooltip 在客户端渲染，但 remainingBurnTime 只在服务端 tick，
 * 液体燃料还会把它顶在 10000 tick 上限。这里按客户端游戏时间本地递减，
 * 避免超过 500s 时数字冻住不动。
 */
public final class GoggleBurnTimeTicker {
    private static final Map<BlockPos, Snapshot> SNAPSHOTS = new HashMap<>();

    private record Snapshot(long gameTime, long totalTicks) {
    }

    private GoggleBurnTimeTicker() {
    }

    public static long displayTicks(Level level, BlockPos pos, long rawTotalTicks) {
        if (level == null || pos == null) {
            return Math.max(rawTotalTicks, 0);
        }
        long now = level.getGameTime();
        Snapshot prev = SNAPSHOTS.get(pos);
        if (prev == null || prev.totalTicks() != rawTotalTicks) {
            SNAPSHOTS.put(pos, new Snapshot(now, rawTotalTicks));
            return Math.max(rawTotalTicks, 0);
        }
        long elapsed = now - prev.gameTime();
        return Math.max(prev.totalTicks() - elapsed, 0);
    }

    public static void clear() {
        SNAPSHOTS.clear();
    }
}
