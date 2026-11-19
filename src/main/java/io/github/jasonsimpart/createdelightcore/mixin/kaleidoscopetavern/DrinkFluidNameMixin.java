package io.github.jasonsimpart.createdelightcore.mixin.kaleidoscopetavern;

import net.minecraft.network.chat.Component;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** CGT adds native aging names to these virtual fluids; QF quality is displayed separately. */
@Pseudo
@Mixin(targets = "com.yision.creategearsandtavern.content.fluids.drink.KaleidoscopeDrinkFluid$KaleidoscopeDrinkFluidType",
        remap = false)
public abstract class DrinkFluidNameMixin {
    @Inject(method = "getDescription(Lnet/minecraftforge/fluids/FluidStack;)Lnet/minecraft/network/chat/Component;",
            at = @At("HEAD"), cancellable = true)
    private void createdelightcore$plainDrinkName(FluidStack stack, CallbackInfoReturnable<Component> cir) {
        cir.setReturnValue(((FluidType) (Object) this).getDescription());
    }
}
