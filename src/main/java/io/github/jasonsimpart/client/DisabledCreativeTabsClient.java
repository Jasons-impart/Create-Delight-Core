package io.github.jasonsimpart.client;

import io.github.jasonsimpart.network.ClientCreativeTabCache;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.common.NeoForge;

public final class DisabledCreativeTabsClient {
    private DisabledCreativeTabsClient() {
    }

    public static void register() {
        ClientCreativeTabCache.onUpdate = DisabledCreativeTabsClient::refreshScreen;
        NeoForge.EVENT_BUS.addListener(DisabledCreativeTabsClient::onLogout);
    }

    private static void refreshScreen() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen instanceof CreativeModeInventoryScreen screen) {
            // Rebuild NeoForge's pages and selection using Screen's normal initialization lifecycle.
            screen.init(minecraft, screen.width, screen.height);
        }
    }

    private static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientCreativeTabCache.clear();
    }
}
