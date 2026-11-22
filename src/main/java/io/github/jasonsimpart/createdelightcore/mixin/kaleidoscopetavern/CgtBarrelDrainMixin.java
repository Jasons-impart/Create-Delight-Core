package io.github.jasonsimpart.createdelightcore.mixin.kaleidoscopetavern;

import com.github.ysbbbbbb.kaleidoscopetavern.blockentity.brew.BarrelBlockEntity;
import io.github.jasonsimpart.createdelightcore.compat.kaleidoscopetavern.TavernBarrelAccess;
import io.github.jasonsimpart.createdelightcore.compat.kaleidoscopetavern.TavernBarrelFluidOutput;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "com.yision.creategearsandtavern.compat.kaleidoscope.CGTKaleidoscopeBarrelFluids$BarrelFluidHandler", remap = false)
public abstract class CgtBarrelDrainMixin {
    @Shadow @Final private BarrelBlockEntity barrel;

    @Inject(method = "getTankCapacity", at = @At("HEAD"), cancellable = true)
    private void createdelightcore$capacity(int tank, CallbackInfoReturnable<Integer> cir) {
        if (barrel instanceof TavernBarrelAccess access && access.createdelightcore$getFluidOutput() != null) {
            cir.setReturnValue(tank == 0 ? TavernBarrelFluidOutput.CAPACITY : 0);
        }
    }

    @Inject(method = "drain(Lnet/minecraftforge/fluids/FluidStack;Lnet/minecraftforge/fluids/capability/IFluidHandler$FluidAction;)Lnet/minecraftforge/fluids/FluidStack;",
            at = @At("HEAD"), cancellable = true)
    private void createdelightcore$drainMatching(FluidStack request, FluidAction action, CallbackInfoReturnable<FluidStack> cir) {
        if (barrel instanceof TavernBarrelAccess access && access.createdelightcore$getFluidOutput() != null) {
            var drained = access.createdelightcore$getFluidOutput().drain(request, action);
            if (!drained.isEmpty() && action.execute()) access.createdelightcore$fluidDrained();
            cir.setReturnValue(drained);
        }
    }

    @Inject(method = "drain(ILnet/minecraftforge/fluids/capability/IFluidHandler$FluidAction;)Lnet/minecraftforge/fluids/FluidStack;",
            at = @At("HEAD"), cancellable = true)
    private void createdelightcore$drainAmount(int amount, FluidAction action, CallbackInfoReturnable<FluidStack> cir) {
        if (barrel instanceof TavernBarrelAccess access && access.createdelightcore$getFluidOutput() != null) {
            var drained = access.createdelightcore$getFluidOutput().drain(amount, action);
            if (!drained.isEmpty() && action.execute()) access.createdelightcore$fluidDrained();
            cir.setReturnValue(drained);
        }
    }
}
