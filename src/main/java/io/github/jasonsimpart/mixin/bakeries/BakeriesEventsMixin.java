package io.github.jasonsimpart.mixin.bakeries;

import com.renyigesai.bakeries.api.event.SnifferDropSeedEvent;
import com.renyigesai.bakeries.common.event.BakeriesEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = BakeriesEvents.class, remap = false)
public abstract class BakeriesEventsMixin {
    @Inject(method = "onDropSeed", at = @At("HEAD"), cancellable = true)
    private static void createdelightcore$cancelDropSeed(SnifferDropSeedEvent event, CallbackInfo ci) {
        ci.cancel();
    }
}
