package io.github.jasonsimpart.mixin.minecraft;

import com.google.gson.Gson;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(SimpleJsonResourceReloadListener.class)
public class SimpleJsonResourceReloadListenerMixin {
    private static final Gson CDC_FALLBACK_GSON = new Gson();

    @ModifyVariable(method = "scanDirectory", at = @At("HEAD"), argsOnly = true, index = 2)
    private static Gson createdelightcore$useFallbackGson(Gson gson) {
        return gson == null ? CDC_FALLBACK_GSON : gson;
    }
}
