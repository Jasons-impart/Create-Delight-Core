package io.github.jasonsimpart.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.event.InputEvent;
import org.lwjgl.glfw.GLFW;

public final class QuickReloadKeyHandler {
    private QuickReloadKeyHandler() {
    }

    public static void onKeyInput(InputEvent.Key event) {
        if (event.getAction() != InputConstants.PRESS || event.getKey() != GLFW.GLFW_KEY_4) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen != null || minecraft.getWindow() == null || minecraft.getConnection() == null) {
            return;
        }

        long window = minecraft.getWindow().getWindow();
        if (!InputConstants.isKeyDown(window, GLFW.GLFW_KEY_F3)) {
            return;
        }

        minecraft.getConnection().sendCommand("reload");
    }
}
