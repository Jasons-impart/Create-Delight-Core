package io.github.jasonsimpart.mixin.createutilities;

import io.github.jasonsimpart.createutilitiesj.blocks.voidtypes.battery.VoidBattery;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value = VoidBattery.class, remap = false)
public abstract class VoidBatteryMixin {
    @ModifyArg(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforge/energy/EnergyStorage;<init>(III)V"), index = 0)
    private static int createdelightcore$modifyCapacity(int capacity) {
        return 320000;
    }

    @ModifyArg(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforge/energy/EnergyStorage;<init>(III)V"), index = 1)
    private static int createdelightcore$modifyMaxReceive(int maxReceive) {
        return 40960;
    }

    @ModifyArg(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforge/energy/EnergyStorage;<init>(III)V"), index = 2)
    private static int createdelightcore$modifyMaxExtract(int maxExtract) {
        return 40960;
    }
}
