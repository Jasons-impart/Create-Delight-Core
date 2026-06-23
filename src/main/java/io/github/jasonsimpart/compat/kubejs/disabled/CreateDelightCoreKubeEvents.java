package io.github.jasonsimpart.compat.kubejs.disabled;

import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;

public interface CreateDelightCoreKubeEvents {
    EventGroup GROUP = EventGroup.of("CreateDelightCoreEvents");

    EventHandler DISABLED_ITEMS = GROUP.server("disabledItems", () -> DisabledItemsKubeEvent.class);
    EventHandler DISABLED_BLOCKS = GROUP.server("disabledBlocks", () -> DisabledBlocksKubeEvent.class);
}
