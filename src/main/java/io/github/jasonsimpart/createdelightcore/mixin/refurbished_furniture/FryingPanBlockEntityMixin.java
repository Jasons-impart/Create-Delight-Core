package io.github.jasonsimpart.createdelightcore.mixin.refurbished_furniture;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mrcrayfish.furniture.refurbished.blockentity.FryingPanBlockEntity;
import com.simibubi.create.foundation.item.SmartInventory;
import de.cadentem.quality_food.util.QualityUtils;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(FryingPanBlockEntity.class)
public class FryingPanBlockEntityMixin {
    @Inject(method = "onCompleteCooking", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V"), remap = false)
    public void storeInput(CallbackInfo ci, @Local(ordinal = 0) ItemStack itemStack, @Share("input") final LocalRef<ItemStack> input) {
        input.set(itemStack.copy());
    }
     @ModifyArg(method = "onCompleteCooking", at = @At(value = "INVOKE", target = "Lcom/mrcrayfish/furniture/refurbished/blockentity/FryingPanBlockEntity;setItem(ILnet/minecraft/world/item/ItemStack;)V"), index = 1)
    public ItemStack applyQuality(ItemStack stack, @Share("input") final LocalRef<ItemStack> input) {
         QualityUtils.applyQuality(stack, List.of(input.get()), null);
         return stack;
    }
}
