package io.github.jasonsimpart.createdelightcore.mixin.cmr;

import fr.iglee42.cmr.cooler.SnowmanCoolerBlock;
import fr.iglee42.cmr.cooler.SnowmanCoolerBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = SnowmanCoolerBlockEntity.class, remap = false)
public interface SnowmanCoolerAccessor {
    @Accessor("remainingBurnTime")
    int createdelightcore$getRemainingBurnTime();

    @Accessor("remainingBurnTime")
    void createdelightcore$setRemainingBurnTime(int remainingBurnTime);

    @Invoker("setBlockHeat")
    void createdelightcore$invokeSetBlockHeat(SnowmanCoolerBlock.HeatLevel heat);
}
