package io.github.jasonsimpart.createdelightcore.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import java.util.Map;

public final class ClientHumidityRoomCache {
    private static ClientLevel cachedLevel;
    private static Map<Long, Float> values = Map.of();
    private ClientHumidityRoomCache() { }
    public static void accept(ResourceLocation dimension, Map<Long, Float> snapshot) {
        var level = Minecraft.getInstance().level;
        if (level != null && level.dimension().location().equals(dimension)) {
            cachedLevel = level;
            values = Map.copyOf(snapshot);
            if (net.minecraftforge.fml.ModList.get().isLoaded("eclipticseasons"))
                com.teamtea.eclipticseasons.client.render.worldui.GrowthInfoClientCache.clear();
        }
    }
    public static Float target(Level level, BlockPos pos) {
        return level == cachedLevel ? io.github.jasonsimpart.createdelightcore.content.humidity.SmartHumidityRegulatorSeasonCompat.target(values, pos) : null;
    }
}
