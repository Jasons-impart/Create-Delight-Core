package io.github.jasonsimpart.createdelightcore.mixin.createmetallurgy;

import fr.lucreeper74.createmetallurgy.registries.CMFluids;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = CMFluids.class, remap = false)
public class CMFluidsMixin {

    @Inject(method = "registerFluidInteractions", at = @At("HEAD"), cancellable = true)
    private static void createdelightcore$disableMoltenWaterInteraction(CallbackInfo ci) {
        ci.cancel();
    }
}
