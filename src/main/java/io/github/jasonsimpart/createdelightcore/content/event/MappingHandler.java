package io.github.jasonsimpart.createdelightcore.content.event;
import com.mojang.logging.LogUtils;
import io.github.jasonsimpart.createdelightcore.CreateDelightCore;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.MissingMappingsEvent;

import java.util.logging.Logger;


@Mod.EventBusSubscriber(modid = CreateDelightCore.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class MappingHandler {

    private static final String OLD_MOD_ID = "createdelightcore";
    private static final String OLD_KUBEJS_ID = "createdelight";
    private static final String NEW_MOD_ID = CreateDelightCore.MODID;

    @SubscribeEvent
    public static void onMissingMappings(MissingMappingsEvent event) {
        handleItemMappings(event, OLD_MOD_ID, OLD_KUBEJS_ID, NEW_MOD_ID);
        handleBlockMappings(event, OLD_MOD_ID, OLD_KUBEJS_ID, NEW_MOD_ID);
        handleFluidMappings(event, OLD_MOD_ID, OLD_KUBEJS_ID, NEW_MOD_ID);
    }

    // 如果有方块需要处理，添加类似的方法
    // @SubscribeEvent
    // public static void onMissingBlockMappings(RegistryEvent.MissingMappings<Block> event) {
    //     handleMappings(event, OLD_MOD_ID, NEW_MOD_ID);
    // }

    private static void handleItemMappings(MissingMappingsEvent event, String oldModId, String oldKubeJSId, String newModId) {
        for (MissingMappingsEvent.Mapping<Item> mapping: event.getAllMappings(Registries.ITEM)) {
            ResourceLocation oldKey = mapping.getKey();
            Item remapped = ForgeRegistries.ITEMS.getValue(new ResourceLocation(newModId, oldKey.getPath()));
            if (oldKey.getNamespace().equals(oldModId) || oldKey.getNamespace().equals(oldKubeJSId)
                    && remapped != null) {
                mapping.remap(remapped);
            }
            else if (remapped == null) {
                remapped = ForgeRegistries.ITEMS.getValue(new ResourceLocation("createmetallurgy", oldKey.getPath()));
                if (remapped != null) {
                    mapping.remap(remapped);
                }
            }
        }
    }
    private static void handleBlockMappings(MissingMappingsEvent event, String oldModId, String oldKubeJSId, String newModId) {
        for (MissingMappingsEvent.Mapping<Block> mapping: event.getAllMappings(Registries.BLOCK)) {
            ResourceLocation oldKey = mapping.getKey();
            Block remapped = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(newModId, oldKey.getPath()));
            if (oldKey.getNamespace().equals(oldModId) || oldKey.getNamespace().equals(oldKubeJSId)
                    && remapped != null) {
                mapping.remap(remapped);
            }
            else if (remapped == null) {
                remapped = ForgeRegistries.BLOCKS.getValue(new ResourceLocation("createmetallurgy", oldKey.getPath()));
                if (remapped != null) {
                    mapping.remap(remapped);
                }
            }
        }
    }
    private static void handleFluidMappings(MissingMappingsEvent event, String oldModId, String oldKubeJSId, String newModId) {
        for (MissingMappingsEvent.Mapping<Fluid> mapping: event.getAllMappings(Registries.FLUID)) {
            ResourceLocation oldKey = mapping.getKey();
            Fluid remapped = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(newModId, oldKey.getPath()));
            if (oldKey.getNamespace().equals(oldModId) || oldKey.getNamespace().equals(oldKubeJSId)
                    && remapped != null) {
                mapping.remap(remapped);
            }
            else if (remapped == null) {
                remapped = ForgeRegistries.FLUIDS.getValue(new ResourceLocation("createmetallurgy", oldKey.getPath()));
                if (remapped != null) {
                    mapping.remap(remapped);
                }
            }
        }
    }
}
