package io.github.jasonsimpart.createdelightcore.content.contraption.components.actor;


import io.github.jasonsimpart.createdelightcore.CreateDelightCore;
import io.github.jasonsimpart.createdelightcore.registry.CDBlocks;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import plus.dragons.createcentralkitchen.content.contraptions.components.actor.FDHarvesterMovementBehaviorExtensions;
import plus.dragons.createcentralkitchen.content.contraptions.components.actor.HarvesterMovementBehaviourExtension;

@Mod.EventBusSubscriber(modid = CreateDelightCore.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CreateDelightCoreHarvesterMovementBehaviorExtensions {
    @SubscribeEvent
    public static void register(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {

            HarvesterMovementBehaviourExtension.REGISTRY.put(CDBlocks.FIRE_LILY_CLUSTER.get(), FDHarvesterMovementBehaviorExtensions::harvestMushroomColony);
            HarvesterMovementBehaviourExtension.REGISTRY.put(CDBlocks.FROST_LILY_CLUSTER.get(), FDHarvesterMovementBehaviorExtensions::harvestMushroomColony);
            HarvesterMovementBehaviourExtension.REGISTRY.put(CDBlocks.LIGHTNING_LILY_CLUSTER.get(), FDHarvesterMovementBehaviorExtensions::harvestMushroomColony);

        });
    }
}
