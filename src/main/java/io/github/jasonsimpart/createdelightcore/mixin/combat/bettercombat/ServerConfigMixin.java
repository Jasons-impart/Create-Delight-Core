package io.github.jasonsimpart.createdelightcore.mixin.combat.bettercombat;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Pseudo
@Mixin(targets = "net.bettercombat.config.ServerConfig", remap = false)
public class ServerConfigMixin {
    @ModifyConstant(
            method = "getUpswingMultiplier",
            constant = @Constant(floatValue = 0.2F),
            require = 1
    )
    private float createdelightcore$allowZeroUpswingMultiplier(float minimum) {
        return 0.0F;
    }
}
