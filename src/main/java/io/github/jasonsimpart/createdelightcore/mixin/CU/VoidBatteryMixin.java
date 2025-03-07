package io.github.jasonsimpart.createdelightcore.mixin.CU;

import me.duquee.createutilities.blocks.voidtypes.battery.VoidBattery;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;


@Mixin(VoidBattery.class)
public abstract class VoidBatteryMixin {
    @ModifyArg(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/energy/EnergyStorage;<init>(III)V"), index = 0, remap = false)
    private static int modifyCapacity(int capacity) {
        return 320000;
    }
    @ModifyArg(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/energy/EnergyStorage;<init>(III)V"), index = 1, remap = false)
    private static int modifyMaxReceive(int maxReceive) {
        return 40960;
    }
    @ModifyArg(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/energy/EnergyStorage;<init>(III)V"), index = 2, remap = false)
    private static int modifyMaxExtract(int maxExtract) {
        return 40960;
    }
}
