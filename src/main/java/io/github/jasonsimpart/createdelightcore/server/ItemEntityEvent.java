package io.github.jasonsimpart.createdelightcore.server;

import io.github.jasonsimpart.createdelightcore.CreateDelightCore;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.StreamSupport;

@Mod.EventBusSubscriber(modid = CreateDelightCore.MODID, value = Dist.DEDICATED_SERVER)
public class ItemEntityEvent {

    public static Chunk worldToChunk(String dimension, int x, int z) {
        return new Chunk(dimension, x >> 4, z >> 4);
    }

    @SubscribeEvent
    public static void serverTickEvent(TickEvent.ServerTickEvent event) {
        long preTime = 20 * 60 * 5;
        if(event.getServer().getTickCount() % preTime == 0) {
            Map<Chunk, Integer> worldEntityCount = new HashMap<Chunk, Integer>();
            var loadedWorlds = event.getServer().getAllLevels();
            loadedWorlds.forEach(world -> {
                List<ItemEntity> collect = StreamSupport.stream(world.getEntities().getAll().spliterator(), false)
                        .filter(entity -> entity instanceof ItemEntity)
                        .map(entity -> (ItemEntity) entity)
                        .toList();


                collect.forEach(itemEntity -> {
                    Chunk chunk = worldToChunk(world.dimension().location().toString() , (int) itemEntity.getX(), (int) itemEntity.getZ());
                    if(worldEntityCount.containsKey(chunk)) {
                        worldEntityCount.put(chunk, worldEntityCount.get(chunk) + itemEntity.getItem().getCount());
                    } else {
                        worldEntityCount.put(chunk, itemEntity.getItem().getCount());
                    }
                    //System.out.println("item: " + itemEntity.getName().getString() + " x: " + itemEntity.getX() + " y: " + itemEntity.getY() + " z: " + itemEntity.getZ() + " stack: " + itemEntity.getItem().getCount());
                });
            });
            final boolean[] isStart = {false};
            worldEntityCount.forEach((chunk, count) -> {
                System.out.println("chunk: " + chunk.x + " " + chunk.z + " count: " + count);
                if(count > 100 ){
                    if(!isStart[0])
                    {
                        isStart[0] = true;
                        event.getServer().getPlayerList().getPlayers().forEach(player -> {
                            player.sendSystemMessage(Component.literal("掉落物报告: "));
                        });
                    }
                    event.getServer().getPlayerList().getPlayers().forEach(player -> {
                        player.sendSystemMessage(Component.literal( "World: " + chunk.dimension() + " Chunk: [" + chunk.x + ", " + chunk.z + "] count: " + count));
                    });
                }
            });
        }

    }

    private record Chunk(String dimension, int x, int z){};




}