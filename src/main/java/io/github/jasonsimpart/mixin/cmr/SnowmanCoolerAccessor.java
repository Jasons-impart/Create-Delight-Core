package io.github.jasonsimpart.mixin.cmr;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "fr.iglee42.cmr.cooler.SnowmanCoolerBlockEntity", remap = false)
public interface SnowmanCoolerAccessor {
    @Accessor("remainingBurnTime")
    int createdelightcore$getRemainingBurnTime();

    @Accessor("remainingBurnTime")
    void createdelightcore$setRemainingBurnTime(int remainingBurnTime);
}
