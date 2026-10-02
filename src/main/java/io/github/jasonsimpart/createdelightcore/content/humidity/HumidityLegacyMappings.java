package io.github.jasonsimpart.createdelightcore.content.humidity;

import io.github.jasonsimpart.createdelightcore.CreateDelightCore;
import io.github.jasonsimpart.createdelightcore.registry.CDBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.MissingMappingsEvent;

@Mod.EventBusSubscriber(modid = CreateDelightCore.MODID)
public final class HumidityLegacyMappings {
    private HumidityLegacyMappings() {
    }

    @SubscribeEvent
    public static void remap(MissingMappingsEvent event) {
        for (var mapping : event.getAllMappings(Registries.BLOCK)) {
            if (!mapping.getKey().getNamespace().equals("createdelight")) {
                continue;
            }
            if (mapping.getKey().getPath().equals("sprinkler")) {
                mapping.remap(CDBlocks.SPRINKLER.get());
            } else if (mapping.getKey().getPath().equals("dryer")) {
                mapping.remap(CDBlocks.DRYER.get());
            }
        }
        for (var mapping : event.getAllMappings(Registries.ITEM)) {
            if (!mapping.getKey().getNamespace().equals("createdelight")) {
                continue;
            }
            if (mapping.getKey().getPath().equals("sprinkler")) {
                mapping.remap(CDBlocks.SPRINKLER.asItem());
            } else if (mapping.getKey().getPath().equals("dryer")) {
                mapping.remap(CDBlocks.DRYER.asItem());
            }
        }
    }
}
