package io.github.jasonsimpart.createdelightcore.compat.createcybergoggles;

import io.github.jasonsimpart.createdelightcore.CreateDelightCore;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.ModList;

import java.lang.reflect.Method;
import java.util.List;

/** Optional client integration with CreateCyberGoggles 7.2.2's native fluid bar. */
public final class CyberGogglesFluidTooltip {
    private static boolean initialized;
    private static Method fluid;
    private static Method forGoggles;

    private CyberGogglesFluidTooltip() {
    }

    public static boolean add(List<Component> tooltip, FluidStack stack, int capacity, int extraIndent) {
        if (!initialized) {
            initialized = true;
            if (!ModList.get().isLoaded("create_cyber_goggles")) return false;
            try {
                // Only optional-mod API names are reflected; no mapped Minecraft method names.
                Class<?> lang = Class.forName("io.github.forgestove.create_cyber_goggles.core.util.CCGLang");
                Method fluidMethod = lang.getMethod("fluid", FluidStack.class, int.class);
                forGoggles = fluidMethod.getReturnType().getMethod("forGoggles", List.class, int.class);
                fluid = fluidMethod;
            } catch (ReflectiveOperationException | LinkageError exception) {
                CreateDelightCore.LOGGER.warn("Cyber Goggles fluid bar API unavailable; using text", exception);
            }
        }
        if (fluid == null) return false;
        try {
            Object builder = fluid.invoke(null, stack, capacity);
            // Preserve CCG's identity marker so its overlay measures and renders a fluid component.
            forGoggles.invoke(builder, tooltip, extraIndent);
            return true;
        } catch (ReflectiveOperationException | LinkageError exception) {
            fluid = null;
            CreateDelightCore.LOGGER.warn("Cyber Goggles fluid bar failed; using text", exception);
            return false;
        }
    }
}
