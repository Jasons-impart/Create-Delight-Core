package io.github.jasonsimpart.client;

import io.github.jasonsimpart.registry.ModBlocks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

public final class ClientModEvents {
    private static final int LUSH_CONFITURE_COLOR = 0xF0612E;

    private ClientModEvents() {
    }

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(ClientModEvents::registerBlockColors);
        modEventBus.addListener(ClientModEvents::registerItemColors);
    }

    private static void registerBlockColors(RegisterColorHandlersEvent.Block event) {
        event.register((state, level, pos, tintIndex) -> LUSH_CONFITURE_COLOR, ModBlocks.LUSH_CONFITURE_JELLY.get(), ModBlocks.LUSH_CONFITURE_JELLO.get());
    }

    private static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        event.register((stack, tintIndex) -> tintIndex == 0 ? -1 : LUSH_CONFITURE_COLOR, ModBlocks.LUSH_CONFITURE_JELLY_BOTTLE.asItem());
        event.register((stack, tintIndex) -> LUSH_CONFITURE_COLOR, ModBlocks.LUSH_CONFITURE_JELLY.asItem(), ModBlocks.LUSH_CONFITURE_JELLO.asItem());
    }
}
