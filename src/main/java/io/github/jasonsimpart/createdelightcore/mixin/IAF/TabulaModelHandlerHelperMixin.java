package io.github.jasonsimpart.createdelightcore.mixin.IAF;

import com.github.alexthe666.iceandfire.client.model.util.TabulaModelHandlerHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.io.InputStream;

@Mixin(value = TabulaModelHandlerHelper.class, remap = false)
public class TabulaModelHandlerHelperMixin {
    @Redirect(
            method = "loadTabulaModel",
            at = @At(value = "INVOKE", target = "Ljava/lang/ClassLoader;getResourceAsStream(Ljava/lang/String;)Ljava/io/InputStream;"),
            require = 0
    )
    private static InputStream createdelightcore$loadTabulaFallback(ClassLoader classLoader, String name) {
        InputStream stream = createdelightcore$getResourceAsStream(classLoader, name);
        if (stream != null) {
            return stream;
        }

        String fallback = createdelightcore$getTabulaFallback(name);
        return fallback == null ? null : createdelightcore$getResourceAsStream(classLoader, fallback);
    }

    private static InputStream createdelightcore$getResourceAsStream(ClassLoader classLoader, String name) {
        InputStream stream = classLoader.getResourceAsStream(name);
        if (stream == null && name.startsWith("/")) {
            stream = classLoader.getResourceAsStream(name.substring(1));
        }
        return stream;
    }

    private static String createdelightcore$getTabulaFallback(String name) {
        return switch (name) {
            case "/assets/iceandfire/models/tabula/firedragon/firedragon_Ground.tbl" ->
                    "/assets/iceandfire/models/tabula/firedragon/firedragon_ground.tbl";
            case "/assets/iceandfire/models/tabula/icedragon/icedragon_Ground.tbl" ->
                    "/assets/iceandfire/models/tabula/icedragon/icedragon_ground.tbl";
            case "/assets/iceandfire/models/tabula/lightningdragon/lightningdragon_Ground.tbl" ->
                    "/assets/iceandfire/models/tabula/lightningdragon/lightningdragon_ground.tbl";
            case "/assets/iceandfire/models/tabula/firedragon/firedragon_swimming.tbl" ->
                    "/assets/iceandfire/models/tabula/firedragon/firedragon_swim1.tbl";
            case "/assets/iceandfire/models/tabula/firedragon/firedragon_swim5.tbl" ->
                    "/assets/iceandfire/models/tabula/firedragon/firedragon_swim4.tbl";
            default -> null;
        };
    }
}
