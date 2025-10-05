package io.github.jasonsimpart.createdelightcore.data;

import io.github.jasonsimpart.createdelightcore.CreateDelightCore;
import io.github.jasonsimpart.createdelightcore.data.recipe.CDProcessingRecipeGen;
import io.github.jasonsimpart.createdelightcore.data.recipe.FreezingRecipeGenerator;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import vectorwing.farmersdelight.data.loot.FDBlockLoot;

import java.util.Collections;
import java.util.List;

@Mod.EventBusSubscriber(modid = CreateDelightCore.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CDCoreDatagenEvent {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        var generator = event.getGenerator();
        var output = generator.getPackOutput();

        CDRegistrateTags.addGenerators();

        generator.addProvider(event.includeServer(), new FreezingRecipeGenerator(output));
        generator.addProvider(event.includeServer(), new CDCGenEntitiesProvider(output, event.getLookupProvider()));

        if (event.includeServer()) {
            CDProcessingRecipeGen.registerAll(generator, output);
        }
    }
}