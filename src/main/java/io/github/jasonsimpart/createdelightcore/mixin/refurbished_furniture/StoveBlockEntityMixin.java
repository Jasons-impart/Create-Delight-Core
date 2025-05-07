package io.github.jasonsimpart.createdelightcore.mixin.refurbished_furniture;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import de.cadentem.quality_food.util.QualityUtils;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(targets = { "com.mrcrayfish.furniture.refurbished.blockentity.StoveBlockEntity$CookingSpace" })
public class StoveBlockEntityMixin {
    @Inject(method = "onCompleteProcess", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V"), remap = false)
    public void storeInput(CallbackInfo ci, @Local(ordinal = 0) ItemStack itemStack, @Share("input") final LocalRef<ItemStack> input) {
        input.set(itemStack.copy());
    }
    @ModifyArg(method = "onCompleteProcess", at = @At(value = "INVOKE", target = "Lcom/mrcrayfish/furniture/refurbished/blockentity/StoveBlockEntity;setItem(ILnet/minecraft/world/item/ItemStack;)V", ordinal = 0), index = 1)
    public ItemStack applyQualityCopy(ItemStack par2, @Share("input") final LocalRef<ItemStack> input) {
        QualityUtils.applyQuality(par2, List.of(input.get()), null);
        return par2;
    }
    @ModifyArg(method = "onCompleteProcess", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isSameItemSameTags(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Z"), index = 0)
    public ItemStack applyQualityAnoCopy(ItemStack par2, @Share("input") final LocalRef<ItemStack> input) {
        QualityUtils.applyQuality(par2, List.of(input.get()), null);
        return par2;
    }

}
