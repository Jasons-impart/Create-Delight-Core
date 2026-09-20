package io.github.jasonsimpart.client;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.jasonsimpart.network.ChainCasingModifierPayload;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

public final class ChainCasingInput {
    private static net.minecraft.client.player.LocalPlayer lastPlayer;
    private static net.minecraft.client.multiplayer.ClientPacketListener lastConnection;
    private ChainCasingInput() {}

    public static void register() {
        NeoForge.EVENT_BUS.addListener(ChainCasingInput::tick);
        // Send before vanilla's use-item packet, including a click between ticks.
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, ChainCasingInput::interact);
    }

    private static void tick(ClientTickEvent.Pre event) { sync(); }
    private static void interact(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide()) sync();
    }

    private static void sync() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.getConnection() == null) {
            lastPlayer = null;
            lastConnection = null;
            return;
        }
        long window = mc.getWindow().getWindow();
        boolean down = mc.screen == null && mc.isWindowActive()
                && (InputConstants.isKeyDown(window, GLFW.GLFW_KEY_LEFT_ALT)
                || InputConstants.isKeyDown(window, GLFW.GLFW_KEY_RIGHT_ALT));
        if (mc.player != lastPlayer || mc.getConnection() != lastConnection
                || mc.player.getPersistentData().getBoolean(ChainCasingModifierPayload.KEY) != down) {
            lastPlayer = mc.player;
            lastConnection = mc.getConnection();
            mc.player.getPersistentData().putBoolean(ChainCasingModifierPayload.KEY, down);
            PacketDistributor.sendToServer(new ChainCasingModifierPayload(down));
        }
    }
}
