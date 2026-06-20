package io.github.jasonsimpart.createdelightcore.mixin.create;

import com.simibubi.create.content.fluids.transfer.GenericItemFilling;
import io.github.jasonsimpart.createdelightcore.util.NorthstarBucketHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = GenericItemFilling.class, remap = false)
public class GenericItemFillingMixin {
    @Inject(method = "getRequiredAmountForItem", at = @At("HEAD"), cancellable = true)
    private static void createdelightcore$getRequiredAmountForNorthstarBucket(Level level, ItemStack stack,
                                                                              FluidStack availableFluid,
                                                                              CallbackInfoReturnable<Integer> cir) {
        if (stack.is(Items.BUCKET) && NorthstarBucketHelper.getBucket(availableFluid.getFluid()) != null) {
            cir.setReturnValue(FluidType.BUCKET_VOLUME);
        }
    }

    @Inject(method = "fillItem", at = @At("HEAD"), cancellable = true)
    private static void createdelightcore$fillNorthstarBucket(Level level, int requiredAmount, ItemStack stack,
                                                              FluidStack availableFluid,
                                                              CallbackInfoReturnable<ItemStack> cir) {
        if (!stack.is(Items.BUCKET) || requiredAmount != FluidType.BUCKET_VOLUME) {
            return;
        }

        ItemStack filledBucket = NorthstarBucketHelper.getBucket(availableFluid.getFluid());
        if (filledBucket == null) {
            return;
        }

        availableFluid.shrink(requiredAmount);
        stack.shrink(1);
        cir.setReturnValue(filledBucket);
    }
}
