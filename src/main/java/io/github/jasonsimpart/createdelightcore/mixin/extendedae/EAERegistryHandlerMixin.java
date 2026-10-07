package io.github.jasonsimpart.createdelightcore.mixin.extendedae;

import io.github.jasonsimpart.createdelightcore.util.ExtendedAePackageInitialization;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(targets = "com.glodblock.github.extendedae.common.EAERegistryHandler", remap = false)
public abstract class EAERegistryHandlerMixin {
    @Shadow(remap = false)
    private void initPackageList() {
        throw new AssertionError("Mixin shadow");
    }

    @Redirect(method = "onInit()V", at = @At(value = "INVOKE",
            target = "Lcom/glodblock/github/extendedae/common/EAERegistryHandler;initPackageList()V"),
            require = 1, remap = false)
    private void createdelightcore$initializePackagesAfterConfig(@Coerce Object registry) {
        ExtendedAePackageInitialization.STARTUP.requestInitialization(this::initPackageList);
    }
}
