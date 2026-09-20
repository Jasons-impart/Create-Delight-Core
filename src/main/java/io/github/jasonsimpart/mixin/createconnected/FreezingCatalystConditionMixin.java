package io.github.jasonsimpart.mixin.createconnected;

import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

/** Core supplies freezing; retain the normal user/category toggles but not the other-mod requirement. */
@Mixin(targets = "com.hlysine.create_connected.config.FeatureToggle", remap = false)
public abstract class FreezingCatalystConditionMixin {
    @Inject(method = "addCondition(Lnet/minecraft/resources/ResourceLocation;Ljava/util/function/Supplier;)V",
            at = @At("HEAD"), cancellable = true)
    private static void keepCoreFreezing(ResourceLocation id, Supplier<Boolean> condition, CallbackInfo ci) {
        if (id.toString().equals("create_connected:fan_freezing_catalyst")) ci.cancel();
    }
}
