package io.github.jasonsimpart.createdelightcore.mixin.neapolitan;

import com.teamabnormals.neapolitan.integration.NeapolitanPlugin;
import mezz.jei.api.runtime.IJeiRuntime;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = NeapolitanPlugin.class, remap = false)
public class NeapolitanJEIPluginMixin {
    @Inject(method = "onRuntimeAvailable", at = @At("HEAD"), cancellable = true)
    public void onRuntimeAvailableMixin(IJeiRuntime jeiRuntime, CallbackInfo ci) {
        ci.cancel();
    }
}
