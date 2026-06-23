package io.github.jasonsimpart.mixin.northstar;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import io.github.jasonsimpart.Config;
import io.github.jasonsimpart.CreateDelightCore;
import io.github.jasonsimpart.compat.northstar.TelescopeRenderPoint;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import org.joml.Quaterniond;
import org.joml.Vector3d;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@Mixin(targets = "com.lightning.northstar.block.tech.telescope.TelescopeScreen", remap = false)
public class TelescopeScreenMixin {
    @Unique
    private static final double CREATEDelightCore$KM_PER_AU = 149597870.7D;
    @Unique
    private static final int CREATEDelightCore$FULL_SIZE = 900;
    @Unique
    private static final ResourceLocation CREATEDelightCore$CERES = ResourceLocation.fromNamespaceAndPath("northstar", "ceres");
    @Unique
    private static final ResourceLocation CREATEDelightCore$PLUTO = ResourceLocation.fromNamespaceAndPath("northstar", "pluto");
    @Unique
    private static final ResourceLocation CREATEDelightCore$ENCELADUS = ResourceLocation.fromNamespaceAndPath("northstar", "enceladus");
    @Unique
    private static boolean createdelightcore$loggedTelescopeFailure;

    @Shadow
    @Final
    private Level level;
    @Shadow
    @Final
    private BlockPos pos;
    @Shadow
    private float scrollX;
    @Shadow
    private float scrollY;

    @Inject(method = "renderPlanets", at = @At("HEAD"), cancellable = true)
    private void createdelightcore$renderSpacedPlanets(GuiGraphics graphics, int mouseX, int mouseY, CallbackInfoReturnable<Object> cir) {
        BufferBuilder buffer = null;
        try {
            Object currentPlanet = createdelightcore$invoke(level, "northstar$planet");
            if (currentPlanet == null) {
                ((Screen) (Object) this).onClose();
                cir.setReturnValue(null);
                return;
            }

            int guiLeft = createdelightcore$intField(this, "guiLeft");
            int guiTop = createdelightcore$intField(this, "guiTop");
            ResourceLocation planetAtlas = createdelightcore$planetAtlas();

            Object currentProperties = createdelightcore$field(currentPlanet, "properties");
            Object currentDimension = createdelightcore$invoke(level, "northstar$dimension");
            Quaterniond viewRotation = (Quaterniond) createdelightcore$planetRendererGetViewRotation(
                    pos.getX(),
                    pos.getZ(),
                    currentProperties,
                    currentDimension
            );

            Vector3d universePosition = new Vector3d((Vector3d) createdelightcore$field(currentPlanet, "position"));
            Vector3d direction = new Vector3d();
            Object currentKey = createdelightcore$field(currentPlanet, "key");
            List<TelescopeRenderPoint> renderedPoints = new ArrayList<>();

            Object hovered = null;
            double hoveredDistance = Double.POSITIVE_INFINITY;

            buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);

            for (Object planet : createdelightcore$systemPlanets(currentPlanet)) {
                Object key = createdelightcore$field(planet, "key");
                Object properties = createdelightcore$field(planet, "properties");
                Object renderer = createdelightcore$invoke(properties, "renderer");
                if (key.equals(currentKey) || createdelightcore$isNoopRenderer(renderer)) {
                    continue;
                }

                direction.set((Vector3d) createdelightcore$field(planet, "position"))
                        .sub(universePosition)
                        .rotate(viewRotation);
                if (direction.y > 0) {
                    continue;
                }

                double distance = direction.length();
                direction.mul(1.0D / distance);

                double diameter = ((Number) createdelightcore$invoke(properties, "diameter")).doubleValue();
                double rawSize = Mth.clamp(diameter / CREATEDelightCore$KM_PER_AU / distance * 20000.0D, 0.0D, 8.0D);
                int size = (int) rawSize;
                int renderSize;
                if (Config.ENABLE_NORTHSTAR_TELESCOPE_MIN_SIZE.get()) {
                    boolean priorityBody = createdelightcore$isPriorityTelescopeBody(key);
                    int minimumSize = priorityBody ? Config.NORTHSTAR_TELESCOPE_MIN_SIZE.get() : 1;
                    size = Math.max(size, minimumSize);
                    renderSize = priorityBody || rawSize >= 1.0D ? size * 2 : 1;
                } else {
                    renderSize = size * 2;
                }
                if (size < 1) {
                    continue;
                }

                float posX = Mth.map((float) direction.x, -1.0F, 1.0F, 0.0F, CREATEDelightCore$FULL_SIZE) - scrollX + guiLeft;
                float posY = Mth.map((float) direction.z, -1.0F, 1.0F, 0.0F, CREATEDelightCore$FULL_SIZE) - scrollY + guiTop;
                TelescopeRenderPoint point = createdelightcore$spacePoint(posX, posY, renderedPoints);
                renderedPoints.add(point);

                if (mouseX >= point.x() - size &&
                        mouseX <= point.x() + size &&
                        mouseY >= point.y() - size &&
                        mouseY <= point.y() + size &&
                        distance < hoveredDistance) {
                    hovered = planet;
                    hoveredDistance = distance;
                }

                PoseStack pose = graphics.pose();
                pose.pushPose();
                pose.translate(point.x(), point.y(), 0.0F);
                createdelightcore$renderPlanet(renderer, level, pose, buffer, renderSize, planet);
                pose.popPose();
            }

            MeshData mesh = buffer.build();
            buffer = null;
            if (mesh != null) {
                RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
                RenderSystem.setShaderTexture(0, planetAtlas);
                RenderSystem.enableBlend();
                BufferUploader.drawWithShader(mesh);
                RenderSystem.disableBlend();
            }

            cir.setReturnValue(hovered);
        } catch (Throwable throwable) {
            if (buffer != null) {
                try {
                    buffer.build();
                } catch (Throwable ignored) {
                    // Discard the partial telescope mesh before returning a safe empty result.
                }
            }
            if (!createdelightcore$loggedTelescopeFailure) {
                createdelightcore$loggedTelescopeFailure = true;
                CreateDelightCore.LOGGER.error("CDC failed to render the patched Northstar telescope view", throwable);
            }
            cir.setReturnValue(null);
        }
    }

    @Unique
    private TelescopeRenderPoint createdelightcore$spacePoint(float x, float y, List<TelescopeRenderPoint> points) {
        if (!Config.ENABLE_NORTHSTAR_TELESCOPE_BODY_SPACING.get()) {
            return new TelescopeRenderPoint(x, y);
        }

        float spacing = Config.NORTHSTAR_TELESCOPE_BODY_SPACING.get();
        if (spacing <= 0.0F) {
            return new TelescopeRenderPoint(x, y);
        }

        float adjustedX = x;
        float adjustedY = y;
        float spacingSquared = spacing * spacing;
        for (int pass = 0; pass < 6; pass++) {
            boolean moved = false;
            for (TelescopeRenderPoint point : points) {
                float dx = adjustedX - point.x();
                float dy = adjustedY - point.y();
                float distanceSquared = dx * dx + dy * dy;
                if (distanceSquared >= spacingSquared) {
                    continue;
                }

                if (distanceSquared < 0.0001F) {
                    float angle = (points.size() + pass + 1) * 2.3999631F;
                    dx = Mth.cos(angle);
                    dy = Mth.sin(angle);
                    distanceSquared = 1.0F;
                }

                float distance = Mth.sqrt(distanceSquared);
                float push = spacing - distance;
                adjustedX += dx / distance * push;
                adjustedY += dy / distance * push;
                moved = true;
            }

            if (!moved) {
                break;
            }
        }

        return new TelescopeRenderPoint(adjustedX, adjustedY);
    }

    @Unique
    private static boolean createdelightcore$isPriorityTelescopeBody(Object key) {
        if (key instanceof ResourceKey<?> resourceKey) {
            ResourceLocation location = resourceKey.location();
            return CREATEDelightCore$CERES.equals(location) ||
                    CREATEDelightCore$PLUTO.equals(location) ||
                    CREATEDelightCore$ENCELADUS.equals(location);
        }
        return false;
    }

    @Unique
    private static List<?> createdelightcore$systemPlanets(Object planet) throws ReflectiveOperationException {
        Object system = createdelightcore$field(planet, "system");
        Object planets = createdelightcore$invoke(system, "planets");
        return (List<?>) planets;
    }

    @Unique
    private static Object createdelightcore$planetRendererGetViewRotation(
            double cameraX,
            double cameraZ,
            Object properties,
            Object dimension
    ) throws ReflectiveOperationException {
        Class<?> rendererClass = Class.forName("com.lightning.northstar.planet.PlanetRenderer");
        Class<?> propertiesClass = Class.forName("com.lightning.northstar.planet.data.PlanetProperties");
        Class<?> dimensionClass = Class.forName("com.lightning.northstar.planet.data.PlanetDimension");
        Method method = rendererClass.getMethod("getViewRotation", double.class, double.class, propertiesClass, dimensionClass);
        return method.invoke(null, cameraX, cameraZ, properties, dimension);
    }

    @Unique
    private static void createdelightcore$renderPlanet(
            Object renderer,
            Level level,
            PoseStack pose,
            BufferBuilder buffer,
            int size,
            Object planet
    ) throws ReflectiveOperationException {
        Class<?> planetClass = Class.forName("com.lightning.northstar.planet.Planet");
        for (Method method : renderer.getClass().getMethods()) {
            if (!method.getName().equals("render") || method.getParameterCount() != 6) {
                continue;
            }

            method.invoke(renderer, level, pose, buffer, size, new Vector4f(1.0F), planetClass.cast(planet));
            return;
        }
        throw new NoSuchMethodException("Planet renderer has no compatible render method: " + renderer.getClass().getName());
    }

    @Unique
    private static ResourceLocation createdelightcore$planetAtlas() throws ReflectiveOperationException {
        Class<?> texturesClass = Class.forName("com.lightning.northstar.content.NorthstarTextures");
        return (ResourceLocation) texturesClass.getField("PLANET_ATLAS").get(null);
    }

    @Unique
    private static boolean createdelightcore$isNoopRenderer(Object renderer) {
        return renderer != null && renderer.getClass().getName().equals("com.lightning.northstar.planet.data.render.NoopPlanetRenderer");
    }

    @Unique
    private static Object createdelightcore$invoke(Object target, String methodName) throws ReflectiveOperationException {
        Method method = createdelightcore$method(target.getClass(), methodName);
        try {
            return method.invoke(target);
        } catch (InvocationTargetException exception) {
            Throwable cause = exception.getCause();
            if (cause instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            if (cause instanceof Error error) {
                throw error;
            }
            throw exception;
        }
    }

    @Unique
    private static Method createdelightcore$method(Class<?> type, String name) throws NoSuchMethodException {
        Class<?> current = type;
        while (current != null) {
            for (Method method : current.getDeclaredMethods()) {
                if (method.getName().equals(name) && method.getParameterCount() == 0) {
                    method.setAccessible(true);
                    return method;
                }
            }
            for (Class<?> iface : current.getInterfaces()) {
                try {
                    Method method = iface.getMethod(name);
                    method.setAccessible(true);
                    return method;
                } catch (NoSuchMethodException ignored) {
                }
            }
            current = current.getSuperclass();
        }
        throw new NoSuchMethodException(type.getName() + "#" + name + "()");
    }

    @Unique
    private static Object createdelightcore$field(Object target, String fieldName) throws ReflectiveOperationException {
        Field field = createdelightcore$fieldHandle(target.getClass(), fieldName);
        return field.get(target);
    }

    @Unique
    private static int createdelightcore$intField(Object target, String fieldName) throws ReflectiveOperationException {
        Field field = createdelightcore$fieldHandle(target.getClass(), fieldName);
        return field.getInt(target);
    }

    @Unique
    private static Field createdelightcore$fieldHandle(Class<?> type, String fieldName) throws NoSuchFieldException {
        Class<?> current = type;
        while (current != null) {
            try {
                Field field = current.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException ignored) {
                current = current.getSuperclass();
            }
        }
        throw new NoSuchFieldException(type.getName() + "#" + fieldName);
    }
}
