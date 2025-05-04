package io.github.jasonsimpart.createdelightcore.mixin.bakeries;

import com.renyigesai.bakeries.api.event.SnifferDropSeedEvent;
import com.renyigesai.bakeries.event.SnifferEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SnifferEvent.class)
public class SnifferEventMixin {
    @Inject(method = "onDropSeed", at = @At(value = "HEAD"), cancellable = true, remap = false)
    private static void onDropSeedMixin(SnifferDropSeedEvent event, CallbackInfo ci) {
        ci.cancel();
    }
}
