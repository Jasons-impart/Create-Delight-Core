package io.github.jasonsimpart.createdelightcore.content.humidity;

import io.github.jasonsimpart.createdelightcore.CreateDelightCore;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CreateDelightCore.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class SmartHumidityRegulatorModels {
    private SmartHumidityRegulatorModels() { }

    @SubscribeEvent
    public static void register(ModelEvent.RegisterAdditional event) {
        // Ensure the independent rotating model is baked even though no blockstate references it.
        event.register(ResourceLocation.fromNamespaceAndPath(CreateDelightCore.MODID,
                "block/smart_humidity_regulator/cog"));
    }
}
