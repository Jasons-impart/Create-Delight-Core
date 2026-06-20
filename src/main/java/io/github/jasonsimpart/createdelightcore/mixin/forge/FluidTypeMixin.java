package io.github.jasonsimpart.createdelightcore.mixin.forge;

import io.github.jasonsimpart.createdelightcore.util.NorthstarBucketHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = FluidType.class, remap = false)
public class FluidTypeMixin {
    @Inject(method = "getBucket", at = @At("HEAD"), cancellable = true)
    private void createdelightcore$getNorthstarBucket(FluidStack stack, CallbackInfoReturnable<ItemStack> cir) {
        ItemStack bucket = NorthstarBucketHelper.getBucket(stack.getFluid());
        if (bucket != null) {
            cir.setReturnValue(bucket);
        }
    }
}
