package io.github.jasonsimpart.compat.kubejs.disabled;

import dev.latvian.mods.kubejs.event.KubeEvent;
import dev.latvian.mods.kubejs.script.ConsoleJS;
import dev.latvian.mods.kubejs.util.ListJS;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashSet;
import java.util.Set;

public final class DisabledCreativeTabsKubeEvent implements KubeEvent {
    private final Set<ResourceLocation> tabs = new LinkedHashSet<>();

    public void tab(String value) {
        ResourceLocation id = value == null ? null : ResourceLocation.tryParse(value);
        if (id == null) {
            ConsoleJS.SERVER.warn("[CDC] disabledCreativeTabs: invalid Tab ID '" + value + "'; ignored");
            return;
        }
        if (tabs.add(id) && !BuiltInRegistries.CREATIVE_MODE_TAB.containsKey(id)) {
            // Some mods register tabs only on the physical client. Still send the ID.
            ConsoleJS.SERVER.warn("[CDC] disabledCreativeTabs: Tab '" + id
                    + "' is not registered on the server; clients will check this ID locally");
        }
    }

    public void tabs(Object values) {
        for (Object value : ListJS.orSelf(values)) {
            tab(String.valueOf(value));
        }
    }

    public Set<ResourceLocation> snapshot() {
        return Set.copyOf(tabs);
    }
}
