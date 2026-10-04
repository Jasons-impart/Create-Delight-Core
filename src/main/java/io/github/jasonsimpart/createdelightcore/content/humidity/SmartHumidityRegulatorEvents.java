package io.github.jasonsimpart.createdelightcore.content.humidity;

import io.github.jasonsimpart.createdelightcore.CreateDelightCore;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.TickEvent;

@Mod.EventBusSubscriber(modid = CreateDelightCore.MODID)
public final class SmartHumidityRegulatorEvents {
    private SmartHumidityRegulatorEvents() { }

    @SubscribeEvent
    public static void syncHumidity(TickEvent.LevelTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.level instanceof ServerLevel level
                && ModList.get().isLoaded("eclipticseasons")) {
            if (level.getGameTime() % 20 == 0) SmartHumidityRegulatorSeasonCompat.syncReadings(level);
        }
    }

    @SubscribeEvent
    public static void levelUnloaded(net.minecraftforge.event.level.LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel level && ModList.get().isLoaded("eclipticseasons"))
            SmartHumidityRegulatorSeasonCompat.levelUnloaded(level);
    }

    @SubscribeEvent
    public static void chunkUnloaded(ChunkEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel level && ModList.get().isLoaded("eclipticseasons")) {
            var pos = event.getChunk().getPos();
            SmartHumidityRegulatorSeasonCompat.chunkUnloaded(level, pos.x, pos.z);
        }
    }
}
