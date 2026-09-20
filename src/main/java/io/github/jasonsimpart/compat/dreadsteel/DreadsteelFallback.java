package io.github.jasonsimpart.compat.dreadsteel;

import io.github.jasonsimpart.CreateDelightCore;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.List;

/** Temporary item identities; the actual Dreadsteel mod always owns its namespace when installed. */
public final class DreadsteelFallback {
    public static final ResourceLocation INGOT = ResourceLocation.parse("dreadsteel:dreadsteel_ingot");
    private static final List<String> PLACEHOLDERS = List.of("dreadsteel_ingot", "dreadsteel_helmet",
            "dreadsteel_chestplate", "dreadsteel_leggings", "dreadsteel_boots", "dreadsteel_scythe",
            "dreadsteel_shield", "kit_default", "kit_white", "kit_black", "kit_bronze");
    private static boolean active;

    private DreadsteelFallback() {}

    public static void register(IEventBus bus) {
        if (ModList.get().isLoaded("dreadsteel")) return;
        bus.addListener(EventPriority.LOWEST, DreadsteelFallback::registerItem);
        bus.addListener(DreadsteelFallback::addResources);
    }

    private static void registerItem(RegisterEvent event) {
        event.register(Registries.ITEM, registry -> {
            for (String path : PLACEHOLDERS) {
                var id = ResourceLocation.fromNamespaceAndPath("dreadsteel", path);
                if (BuiltInRegistries.ITEM.containsKey(id)) continue;
                registry.register(id, new PlaceholderItem(path));
                active = true;
                CreateDelightCore.LOGGER.info("Registered temporary Dreadsteel item {}", id);
            }
        });
    }

    public static boolean isActive() {
        return active;
    }

    public static void addResources(AddPackFindersEvent event) {
        if (!active) return;
        event.addPackFinders(ResourceLocation.parse("createdelightcore:resourcepacks/dreadsteel_fallback"),
                PackType.CLIENT_RESOURCES, Component.translatable("pack.createdelightcore.dreadsteel_fallback"),
                PackSource.BUILT_IN, true, Pack.Position.BOTTOM);
    }

    private static final class PlaceholderItem extends Item {
        private final String path;

        private PlaceholderItem(String path) {
            super(new Item.Properties().stacksTo(path.equals("dreadsteel_ingot") || path.startsWith("kit_") ? 64 : 1));
            this.path = path;
        }

        @Override
        public String getDescriptionId() {
            return "item.createdelightcore.dreadsteel_placeholder_" + path.replace("dreadsteel_", "");
        }

        @Override
        public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
            tooltip.add(Component.translatable("tooltip.createdelightcore.dreadsteel_placeholder").withStyle(ChatFormatting.GRAY));
            if (!path.equals("dreadsteel_ingot"))
                tooltip.add(Component.translatable("tooltip.createdelightcore.dreadsteel_nonfunctional").withStyle(ChatFormatting.YELLOW));
            tooltip.add(Component.translatable("tooltip.createdelightcore.dreadsteel_replacement").withStyle(ChatFormatting.GRAY));
        }
    }
}
