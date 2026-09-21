package io.github.jasonsimpart.content.disabled;

import net.minecraft.resources.ResourceLocation;

import java.util.Set;

/** Server rules; deliberately separate from the client snapshot, including in singleplayer. */
public final class DisabledCreativeTabs {
    private static volatile Set<ResourceLocation> serverTabs = Set.of();

    private DisabledCreativeTabs() {
    }

    public static void replace(Set<ResourceLocation> tabs) {
        serverTabs = Set.copyOf(tabs);
    }

    public static Set<ResourceLocation> snapshot() {
        return serverTabs;
    }
}
