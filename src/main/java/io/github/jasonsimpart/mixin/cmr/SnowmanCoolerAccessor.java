package io.github.jasonsimpart.mixin.cmr;

import fr.iglee42.cmr.cooler.SnowmanCoolerBlockEntity.FuelType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "fr.iglee42.cmr.cooler.SnowmanCoolerBlockEntity", remap = false)
public interface SnowmanCoolerAccessor {
    @Accessor("remainingBurnTime")
    int createdelightcore$getRemainingBurnTime();

    @Accessor("remainingBurnTime")
    void createdelightcore$setRemainingBurnTime(int remainingBurnTime);

    @Accessor("activeFuel")
    FuelType createdelightcore$getActiveFuel();

    @Accessor("activeFuel")
    void createdelightcore$setActiveFuel(FuelType activeFuel);
}
