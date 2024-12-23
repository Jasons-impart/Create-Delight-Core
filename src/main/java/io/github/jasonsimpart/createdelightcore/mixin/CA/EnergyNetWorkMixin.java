package io.github.jasonsimpart.createdelightcore.mixin.CA;

import com.mrh0.createaddition.energy.network.EnergyNetwork;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(EnergyNetwork.class)
public abstract class EnergyNetWorkMixin {
//    @ModifyArg(method = "getMaxBuff", at = @At(value = "INVOKE", target = "Ljava/lang/Math;min(II)I"), index = 1)
//    private int getMaxBuff(int maxBuff) {
//        return (int) (maxBuff * 2.5F);
//    }
}
