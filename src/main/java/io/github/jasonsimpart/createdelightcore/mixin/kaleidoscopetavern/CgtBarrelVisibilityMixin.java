package io.github.jasonsimpart.createdelightcore.mixin.kaleidoscopetavern;

import com.github.ysbbbbbb.kaleidoscopetavern.blockentity.brew.BarrelBlockEntity;
import io.github.jasonsimpart.createdelightcore.compat.kaleidoscopetavern.TavernBarrelAccess;
import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "com.yision.creategearsandtavern.compat.kaleidoscope.CGTKaleidoscopeBarrelFluids", remap = false)
public abstract class CgtBarrelVisibilityMixin {
    @Inject(method = "getVisibleFluid", at = @At("HEAD"), cancellable = true)
    private static void createdelightcore$showProductTank(BarrelBlockEntity barrel, CallbackInfoReturnable<FluidStack> cir) {
        if (!(barrel instanceof TavernBarrelAccess access)) return;
        var output = access.createdelightcore$getFluidOutput();
        if (output != null) cir.setReturnValue(output.visibleFluid());
        else if (barrel.isBrewing()) cir.setReturnValue(FluidStack.EMPTY);
    }
}
