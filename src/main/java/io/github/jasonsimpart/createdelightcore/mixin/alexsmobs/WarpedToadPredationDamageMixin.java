package io.github.jasonsimpart.createdelightcore.mixin.alexsmobs;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/** Alex's Mobs 1.22.9: only the crimson-mosquito branch uses this sentinel. */
@Pseudo
@Mixin(targets = "com.github.alexthe666.alexsmobs.entity.EntityWarpedToad", remap = false)
public abstract class WarpedToadPredationDamageMixin {
    // Explicit named/SRG selectors: the optional target is not on the compile classpath.
    @ModifyConstant(method = {"tick()V", "m_8119_()V"},
            constant = @Constant(floatValue = Float.MAX_VALUE),
            remap = false, require = 1, expect = 1, allow = 1)
    private float createdelightcore$finitePredationDamage(float original) {
        return 10_000.0F;
    }
}
