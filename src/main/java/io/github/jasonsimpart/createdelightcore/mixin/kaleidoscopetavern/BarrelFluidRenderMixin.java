package io.github.jasonsimpart.createdelightcore.mixin.kaleidoscopetavern;

import com.github.ysbbbbbb.kaleidoscopetavern.blockentity.brew.BarrelBlockEntity;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.jasonsimpart.createdelightcore.compat.kaleidoscopetavern.TavernBarrelAccess;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@Mixin(targets = "com.github.ysbbbbbb.kaleidoscopetavern.client.render.block.BarrelBlockEntityRender", remap = false)
public abstract class BarrelFluidRenderMixin {
    @WrapOperation(method = "renderFluid", at = @At(value = "INVOKE",
            target = "Lnet/minecraftforge/fluids/capability/templates/FluidTank;getFluidAmount()I"))
    private int createdelightcore$renderFillLevel(FluidTank tank, Operation<Integer> original,
                                                   @Local(argsOnly = true) BarrelBlockEntity barrel) {
        var output = ((TavernBarrelAccess) barrel).createdelightcore$getFluidOutput();
        return output == null ? original.call(tank) : output.amount();
    }

    @WrapOperation(method = "renderFluid", at = @At(value = "INVOKE",
            target = "Lnet/minecraftforge/fluids/capability/templates/FluidTank;getFluid()Lnet/minecraftforge/fluids/FluidStack;"))
    private FluidStack createdelightcore$renderProductFluid(FluidTank tank, Operation<FluidStack> original,
                                                            @Local(argsOnly = true) BarrelBlockEntity barrel) {
        var output = ((TavernBarrelAccess) barrel).createdelightcore$getFluidOutput();
        return output == null ? original.call(tank) : output.visibleFluid();
    }
}
