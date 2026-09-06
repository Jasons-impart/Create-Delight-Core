package io.github.jasonsimpart.createdelightcore.eventhandlers;

import io.github.jasonsimpart.createdelightcore.CreateDelightCore;
import io.github.jasonsimpart.createdelightcore.compat.cmr.DrainableFuelLoader;
import io.github.jasonsimpart.createdelightcore.content.configuration.ConfigurationModulePlacementHelper;
import io.github.jasonsimpart.createdelightcore.registry.CDBlockSpoutingBehaviours;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod.EventBusSubscriber(modid = CreateDelightCore.MODID)
public class ModEventHandler {
    @SubscribeEvent
    public static void commonSetup(FMLCommonSetupEvent event){
        if (ModList.get().isLoaded("cmr")) {
            event.enqueueWork(DrainableFuelLoader::load);
        }
        event.enqueueWork(CDBlockSpoutingBehaviours::register);
        event.enqueueWork(ConfigurationModulePlacementHelper::register);
    }
}
