package com.jasonsimpart.createdelightcore.data;

import com.jasonsimpart.createdelightcore.CreateDelightCore;
import com.jasonsimpart.createdelightcore.data.recipe.FreezingRecipeGenerator;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CreateDelightCore.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataEventHandler {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        var generator = event.getGenerator();
        generator.addProvider(event.includeServer(), new FreezingRecipeGenerator(generator));
    }
}
