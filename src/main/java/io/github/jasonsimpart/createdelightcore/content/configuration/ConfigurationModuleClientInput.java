package io.github.jasonsimpart.createdelightcore.content.configuration;

import com.mojang.blaze3d.platform.InputConstants;
import com.simibubi.create.AllKeys;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

public final class ConfigurationModuleClientInput {
    private static boolean suppressUntilToolbeltRelease;

    private ConfigurationModuleClientInput() {
    }

    public static void tick(Minecraft minecraft) {
        if (!suppressUntilToolbeltRelease) {
            return;
        }
        drainToolbeltClicks();
        if (!isToolbeltKeyPhysicallyDown(minecraft)) {
            suppressUntilToolbeltRelease = false;
            drainToolbeltClicks();
        }
    }

    public static boolean consumeOpenClick(Minecraft minecraft) {
        tick(minecraft);
        if (suppressUntilToolbeltRelease || !AllKeys.TOOLBELT.getKeybind().consumeClick()) {
            return false;
        }
        suppressUntilToolbeltRelease = true;
        drainToolbeltClicks();
        return true;
    }

    public static boolean isToolbeltKeyPhysicallyDown(Minecraft minecraft) {
        InputConstants.Key key = AllKeys.TOOLBELT.getKeybind().getKey();
        if (key.getType() == InputConstants.Type.MOUSE) {
            return AllKeys.isMouseButtonDown(key.getValue());
        }
        return InputConstants.isKeyDown(minecraft.getWindow().getWindow(), key.getValue());
    }

    private static void drainToolbeltClicks() {
        KeyMapping keyMapping = AllKeys.TOOLBELT.getKeybind();
        while (keyMapping.consumeClick()) {
            // Discard keyboard-repeat clicks until the physical key is released.
        }
    }
}
