package io.github.jasonsimpart.createdelightcore.mixin.CA;

import com.mrh0.createaddition.energy.network.EnergyNetwork;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(EnergyNetwork.class)
public abstract class EnergyNetWorkMixin {
    @ModifyArg(method = "getMaxBuff", at = @At(value = "INVOKE", target = "Ljava/lang/Math;min(II)I"), index = 1, remap = false)
    private int getMaxBuff(int a){
        return 1_000_000;
    }
}
