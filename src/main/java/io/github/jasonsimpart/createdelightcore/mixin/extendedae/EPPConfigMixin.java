package io.github.jasonsimpart.createdelightcore.mixin.extendedae;

import io.github.jasonsimpart.createdelightcore.util.ExtendedAePackageInitialization;
import io.github.jasonsimpart.createdelightcore.util.ExtendedAeConfigurationRecovery;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "com.glodblock.github.extendedae.config.EPPConfig", remap = false)
public abstract class EPPConfigMixin {
    @Shadow(remap = false)
    @Final
    public static ForgeConfigSpec SPEC;

    @Shadow(remap = false)
    static void onLoad(ModConfigEvent event) {
        throw new AssertionError("Mixin shadow");
    }

    @Inject(method = "<clinit>", at = @At("RETURN"), require = 1, remap = false)
    private static void createdelightcore$registerConfigRecovery(CallbackInfo ci) {
        ExtendedAeConfigurationRecovery.register(SPEC, EPPConfigMixin::onLoad);
    }

    @Inject(method = "onLoad(Lnet/minecraftforge/fml/event/config/ModConfigEvent;)V",
            at = @At("RETURN"), require = 1, remap = false)
    private static void createdelightcore$onConfigLoaded(ModConfigEvent event, CallbackInfo ci) {
        ExtendedAePackageInitialization.STARTUP.onConfigLoaded();
    }
}
