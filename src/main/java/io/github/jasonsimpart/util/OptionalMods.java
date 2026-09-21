package io.github.jasonsimpart.util;

import io.github.jasonsimpart.CreateDelightCore;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.LoadingModList;

public final class OptionalMods {
    private OptionalMods() {
    }

    public static boolean isLoaded(String modId) {
        ModList modList = ModList.get();
        if (modList != null) {
            return modList.isLoaded(modId);
        }
        LoadingModList loadingModList = LoadingModList.get();
        return loadingModList != null && loadingModList.getModFileById(modId) != null;
    }

    public static boolean allLoaded(String... modIds) {
        for (String modId : modIds) {
            if (!isLoaded(modId)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Invokes {@code public static void register()} on {@code className} reflectively so the
     * class is never touched while its optional dependencies are absent. Fails fast on error.
     */
    public static void invokeRegister(String className) {
        invoke(className, "register");
    }

    /**
     * Like {@link #invokeRegister(String)} but logs instead of throwing when the target class
     * cannot be linked, so a partially broken optional dependency cannot crash startup.
     */
    public static void invokeRegisterSoft(String className) {
        invokeSoft(className, "register");
    }

    /**
     * Invokes {@code public static void <methodName>(args)} on {@code className} reflectively.
     * Fails fast on error.
     */
    public static void invoke(String className, String methodName, Class<?>[] parameterTypes, Object... args) {
        try {
            Class.forName(className).getMethod(methodName, parameterTypes).invoke(null, args);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to invoke createdelightcore compat " + className + "." + methodName, exception);
        }
    }

    /**
     * Like {@link #invoke(String, String, Class[], Object...)} but logs instead of throwing.
     */
    public static void invokeSoft(String className, String methodName, Class<?>[] parameterTypes, Object... args) {
        try {
            Class.forName(className).getMethod(methodName, parameterTypes).invoke(null, args);
        } catch (ReflectiveOperationException | LinkageError exception) {
            CreateDelightCore.LOGGER.warn("Failed to invoke createdelightcore compat {}.{}", className, methodName, exception);
        }
    }

    private static void invoke(String className, String methodName) {
        invoke(className, methodName, new Class<?>[0]);
    }

    private static void invokeSoft(String className, String methodName) {
        invokeSoft(className, methodName, new Class<?>[0]);
    }
}
