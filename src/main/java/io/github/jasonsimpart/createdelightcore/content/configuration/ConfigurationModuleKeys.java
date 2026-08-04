package io.github.jasonsimpart.createdelightcore.content.configuration;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

public final class ConfigurationModuleKeys {
    public static final KeyMapping MODIFIER = new KeyMapping(
            "key.createdelightcore.configuration_module_modifier",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_LEFT_CONTROL,
            "key.categories.createdelightcore"
    );

    private ConfigurationModuleKeys() {
    }
}
