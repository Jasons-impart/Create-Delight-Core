package io.github.jasonsimpart.mixin.xaero;

import io.github.jasonsimpart.Config;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "xaero.lib.patreon.Patreon", remap = false)
public abstract class PatreonMixin {
    @Inject(method = "checkPatreon()V", at = @At("HEAD"), cancellable = true, remap = false)
    private static void cdc$blockPatreonCheck(CallbackInfo ci) {
        if (Config.ENABLE_XAERO_UPDATE_CHECK_BLOCK.get()) {
            ci.cancel();
        }
    }
}
