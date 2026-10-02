package io.github.jasonsimpart.createdelightcore.content.humidity;

import com.teamtea.eclipticseasons.common.core.SolarHolders;
import com.teamtea.eclipticseasons.common.core.crop.HumidityControlProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

final class HumiditySeasonCompat {
    private HumiditySeasonCompat() {
    }

    static Object refresh(ServerLevel level, BlockPos center, int adjustment,
                                           Object ownedProvider) {
        var manager = SolarHolders.getSaveData(level);
        if (manager == null) {
            return null;
        }
        var existing = manager.queryHumidityControlProvider(center);
        if (existing == null) {
            var provider = new HumidityControlProvider(adjustment, 4, 40, false);
            manager.addHumidityControlProvider(center, provider);
            return provider;
        }
        if (existing == ownedProvider) {
            existing.setRemainTime(40);
            return existing;
        }
        return null;
    }

    static void remove(ServerLevel level, BlockPos center, Object ownedProvider) {
        var manager = SolarHolders.getSaveData(level);
        if (manager != null && ownedProvider != null
                && manager.queryHumidityControlProvider(center) == ownedProvider) {
            manager.removeHumidityControlProvider(center);
        }
    }
}
