package io.github.jasonsimpart.compat.northstar;

import io.github.jasonsimpart.CreateDelightCore;
import java.lang.reflect.Method;
import java.util.function.Consumer;

public final class NorthstarPlanetIntegration {
    private static final String NORTHSTAR_TEXTURE_PREFIX = "northstar:textures/environment/";
    private static final double PLAYABLE_LOW_GRAVITY = 0.16;

    private NorthstarPlanetIntegration() {
    }

    public static void register() throws ReflectiveOperationException {
        Class<?> registryClass = Class.forName("com.lightning.northstar.api.planet.PlanetRegistry");
        Method registerCallback = registryClass.getMethod("registerCallback", Consumer.class);
        registerCallback.invoke(null, (Consumer<Object>) ignored -> {
            try {
                registerPlanets(registryClass);
            } catch (ReflectiveOperationException exception) {
                throw new IllegalStateException("Failed to build Create Delight Core Northstar planet definitions", exception);
            }
        });
    }

    private static void registerPlanets(Class<?> registryClass) throws ReflectiveOperationException {
        Method registerOrReplace = registryClass.getMethod(
                "registerOrReplace",
                Class.forName("com.lightning.northstar.api.planet.PlanetDefinition"));

        registerOrReplace.invoke(null, dwarfPlanet("ceres", "createdelightcore:ceres_dimension", -173, PLAYABLE_LOW_GRAVITY,
                NORTHSTAR_TEXTURE_PREFIX + "ceres_far.png", 6, 6));
        registerOrReplace.invoke(null, enceladus());
        registerOrReplace.invoke(null, dwarfPlanet("pluto", "createdelightcore:pluto_dimension", -225, PLAYABLE_LOW_GRAVITY,
                NORTHSTAR_TEXTURE_PREFIX + "pluto_far.png", 6, 6));
    }

    private static Object dwarfPlanet(
            String id,
            String dimension,
            int temperature,
            double gravityMultiplier,
            String texture,
            int hitRadius,
            int hitOffset) throws ReflectiveOperationException {
        Object builder = builder(id);
        call(builder, "dimension", String.class, dimension);
        call(builder, "orbit", orbitAroundOrigin(id));
        call(builder, "gravityMultiplier", double.class, gravityMultiplier);
        call(builder, "temperature", int.class, temperature);
        call(builder, "oxygen", boolean.class, false);
        call(builder, "atmosphere", boolean.class, false);
        call(builder, "atmosphereCost", int.class, 0);
        call(builder, "computingCost", int.class, 650);
        call(builder, "engineConstant", double.class, 4.0);
        call(builder, "sunMultiplier", float.class, 1.0f);
        call(builder, "wind", windNone());
        call(builder, "skyProfile", skyProfile("SPACE"));
        call(builder, "telescopeTexture", String.class, texture);
        call(builder, "telescopeHitbox", int.class, hitRadius, int.class, hitOffset);
        call(builder, "reachableByRocket", boolean.class, true);
        call(builder, "hasSky", boolean.class, true);
        call(builder, "canSeeSkyAtDay", boolean.class, true);
        call(builder, "hasWeather", boolean.class, false);
        call(builder, "customDimension", boolean.class, true);
        call(builder, "heat", double.class, 0.0, double.class, 0.0);
        return build(builder);
    }

    private static Object enceladus() throws ReflectiveOperationException {
        Object builder = builder("enceladus");
        call(builder, "dimension", String.class, "createdelightcore:enceladus_dimension");
        call(builder, "displayNameLangKey", String.class, "planets.enceladus.name");
        call(builder, "orbit", orbitAround("saturn", 20.0, 20.0, Math.PI / 10000.0));
        call(builder, "gravityMultiplier", double.class, PLAYABLE_LOW_GRAVITY);
        call(builder, "temperature", int.class, -201);
        call(builder, "oxygen", boolean.class, false);
        call(builder, "atmosphere", boolean.class, false);
        call(builder, "atmosphereCost", int.class, 0);
        call(builder, "computingCost", int.class, 900);
        call(builder, "engineConstant", double.class, 6.0);
        call(builder, "sunMultiplier", float.class, 0.35f);
        call(builder, "wind", windNone());
        call(builder, "skyProfile", skyProfile("SPACE"));
        call(builder, "telescopeTexture", String.class, NORTHSTAR_TEXTURE_PREFIX + "moon_far.png");
        call(builder, "telescopeHitbox", int.class, 5, int.class, 5);
        call(builder, "reachableByRocket", boolean.class, true);
        call(builder, "hasSky", boolean.class, true);
        call(builder, "canSeeSkyAtDay", boolean.class, true);
        call(builder, "hasWeather", boolean.class, false);
        call(builder, "customDimension", boolean.class, true);
        call(builder, "heat", double.class, 0.0, double.class, 0.0);
        return build(builder);
    }

    private static Object builder(String id) throws ReflectiveOperationException {
        Class<?> definitionClass = Class.forName("com.lightning.northstar.api.planet.PlanetDefinition");
        return definitionClass.getMethod("builder", String.class).invoke(null, id);
    }

    private static Object build(Object builder) throws ReflectiveOperationException {
        return builder.getClass().getMethod("build").invoke(builder);
    }

    private static Object orbitAroundOrigin(String id) throws ReflectiveOperationException {
        return switch (id) {
            case "ceres" -> orbitAroundOrigin(0.0, 0.0, 220.0, 220.0, Math.PI / 100000.0);
            case "pluto" -> orbitAroundOrigin(0.0, 0.0, 520.0, 220.0, Math.PI / 190000.0);
            default -> orbitAroundOrigin(0.0, 0.0, 120.0, 120.0, Math.PI / 90000.0);
        };
    }

    private static Object orbitAroundOrigin(
            double originX,
            double originY,
            double radiusX,
            double radiusY,
            double speed) throws ReflectiveOperationException {
        Class<?> orbitClass = Class.forName("com.lightning.northstar.api.planet.OrbitDefinition");
        return orbitClass.getMethod("aroundOrigin", double.class, double.class, double.class, double.class, double.class)
                .invoke(null, originX, originY, radiusX, radiusY, speed);
    }

    private static Object orbitAround(String parentId, double radiusX, double radiusY, double speed)
            throws ReflectiveOperationException {
        Class<?> orbitClass = Class.forName("com.lightning.northstar.api.planet.OrbitDefinition");
        return orbitClass.getMethod("around", String.class, double.class, double.class, double.class)
                .invoke(null, parentId, radiusX, radiusY, speed);
    }

    private static Object windNone() throws ReflectiveOperationException {
        return Class.forName("com.lightning.northstar.api.planet.WindDefinition").getField("NONE").get(null);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Object skyProfile(String name) throws ReflectiveOperationException {
        Class<? extends Enum> skyProfileClass =
                (Class<? extends Enum>) Class.forName("com.lightning.northstar.api.planet.SkyProfile");
        return Enum.valueOf(skyProfileClass, name);
    }

    private static void call(Object target, String methodName, Object value) throws ReflectiveOperationException {
        target.getClass().getMethod(methodName, value.getClass()).invoke(target, value);
    }

    private static void call(Object target, String methodName, Class<?> parameterType, Object value)
            throws ReflectiveOperationException {
        target.getClass().getMethod(methodName, parameterType).invoke(target, value);
    }

    private static void call(
            Object target,
            String methodName,
            Class<?> firstParameterType,
            Object firstValue,
            Class<?> secondParameterType,
            Object secondValue) throws ReflectiveOperationException {
        target.getClass().getMethod(methodName, firstParameterType, secondParameterType)
                .invoke(target, firstValue, secondValue);
    }
}
