package io.github.jasonsimpart.network;

import io.github.jasonsimpart.CreateDelightCore;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;

import java.util.List;
import java.util.Set;

/** No client-only class references: safe to load when registering payloads on a dedicated server. */
public final class ClientCreativeTabCache {
    private static Set<ResourceLocation> hiddenTabs = Set.of();
    public static Runnable onUpdate;

    private ClientCreativeTabCache() {
    }

    public static boolean isHidden(CreativeModeTab tab) {
        ResourceLocation id = BuiltInRegistries.CREATIVE_MODE_TAB.getKey(tab);
        return id != null && hiddenTabs.contains(id);
    }

    public static List<CreativeModeTab> filter(List<CreativeModeTab> tabs) {
        return hiddenTabs.isEmpty() ? tabs : tabs.stream().filter(tab -> !isHidden(tab)).toList();
    }

    public static void replace(Set<ResourceLocation> tabs) {
        Set<ResourceLocation> next = Set.copyOf(tabs);
        for (ResourceLocation id : next) {
            if (!hiddenTabs.contains(id) && !BuiltInRegistries.CREATIVE_MODE_TAB.containsKey(id)) {
                CreateDelightCore.LOGGER.warn("[CDC] disabledCreativeTabs: Tab '{}' is not registered on this client; ignored", id);
            }
        }
        boolean changed = !hiddenTabs.equals(next);
        hiddenTabs = next;
        if (changed && onUpdate != null) {
            onUpdate.run();
        }
    }

    public static void clear() {
        hiddenTabs = Set.of();
    }
}
