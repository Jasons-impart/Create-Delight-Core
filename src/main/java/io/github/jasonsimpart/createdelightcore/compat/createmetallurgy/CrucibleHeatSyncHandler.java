package io.github.jasonsimpart.createdelightcore.compat.createmetallurgy;

import fr.lucreeper74.createmetallurgy.content.blocks.industrial_crucible.CrucibleBlockEntity;
import io.github.jasonsimpart.createdelightcore.CreateDelightCore;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.event.level.ChunkWatchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CreateDelightCore.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CrucibleHeatSyncHandler {
    @SubscribeEvent
    public static void onChunkWatch(ChunkWatchEvent.Watch event) {
        for (BlockEntity blockEntity : event.getChunk().getBlockEntities().values()) {
            if (!(blockEntity instanceof CrucibleBlockEntity crucible) || !crucible.isController()) {
                continue;
            }

            crucible.foundryData.updateTemperature();
            crucible.sendData();
        }
    }
}
