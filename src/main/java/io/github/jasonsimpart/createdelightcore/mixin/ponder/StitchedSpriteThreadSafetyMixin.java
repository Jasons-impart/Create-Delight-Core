package io.github.jasonsimpart.createdelightcore.mixin.ponder;

import net.createmod.catnip.render.StitchedSprite;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Mixin(value = StitchedSprite.class, remap = false)
public class StitchedSpriteThreadSafetyMixin {

    @Redirect(
            method = "<init>(Lnet/minecraft/resources/ResourceLocation;Lnet/minecraft/resources/ResourceLocation;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/Map;computeIfAbsent(Ljava/lang/Object;Ljava/util/function/Function;)Ljava/lang/Object;"
            )
    )
    private Object createdelightcore$computeSpriteListSafely(Map<ResourceLocation, List<StitchedSprite>> sprites,
                                                             Object atlasLocation,
                                                             Function<ResourceLocation, List<StitchedSprite>> factory) {
        synchronized (sprites) {
            return sprites.computeIfAbsent((ResourceLocation) atlasLocation,
                    key -> Collections.synchronizedList(new ArrayList<>()));
        }
    }
}
