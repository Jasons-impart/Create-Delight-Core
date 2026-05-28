package io.github.jasonsimpart.createdelightcore.mixin.refurbished_furniture;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import de.cadentem.quality_food.util.QualityUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = { "com.mrcrayfish.furniture.refurbished.blockentity.StoveBlockEntity$CookingSpace" })
public class StoveBlockEntityMixin {
    @Inject(method = "onCompleteProcess", at = @At(value = "INVOKE", target = "Lcom/mrcrayfish/furniture/refurbished/blockentity/StoveBlockEntity$CookingSpace;getRecipe()Ljava/util/Optional;"), remap = false)
    public void storeInput(CallbackInfo ci, @Local(ordinal = 0) ItemStack itemStack, @Share("ovenInput") final LocalRef<CompoundTag> input) {
        input.set(create_Delight_Core$copyQualityTag(itemStack));
    }

    @Redirect(method = "onCompleteProcess", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;copy()Lnet/minecraft/world/item/ItemStack;", remap = true), remap = false)
    public ItemStack applyQualityToCopy(ItemStack instance, @Share("ovenInput") final LocalRef<CompoundTag> input) {
        ItemStack result = instance.copy();
        create_Delight_Core$applyQualityTag(result, input.get());
        return result;
    }

    @Inject(method = "canProcess", at = @At(value = "INVOKE", target = "Lcom/mrcrayfish/furniture/refurbished/blockentity/StoveBlockEntity$CookingSpace;getRecipe()Ljava/util/Optional;"), remap = false)
    public void storeInputForOutputCheck(CallbackInfoReturnable<Boolean> cir, @Local(ordinal = 0) ItemStack itemStack, @Share("ovenOutputCheckInput") final LocalRef<CompoundTag> input) {
        input.set(create_Delight_Core$copyQualityTag(itemStack));
    }

    @ModifyArg(method = "canProcess", at = @At(value = "INVOKE", target = "Lcom/mrcrayfish/furniture/refurbished/blockentity/StoveBlockEntity$CookingSpace;canOutput(Lnet/minecraft/world/item/ItemStack;)Z"), index = 0, remap = false)
    public ItemStack applyQualityToOutputCheck(ItemStack stack, @Share("ovenOutputCheckInput") final LocalRef<CompoundTag> input) {
        CompoundTag qualityTag = input.get();
        if (qualityTag == null) {
            return stack;
        }

        ItemStack result = stack.copy();
        create_Delight_Core$applyQualityTag(result, qualityTag);
        return result;
    }

    @Unique
    private static CompoundTag create_Delight_Core$copyQualityTag(ItemStack stack) {
        CompoundTag tag = stack.getTagElement(QualityUtils.QUALITY_TAG);
        return tag != null ? tag.copy() : null;
    }

    @Unique
    private static void create_Delight_Core$applyQualityTag(ItemStack stack, CompoundTag qualityTag) {
        if (qualityTag != null) {
            stack.getOrCreateTag().put(QualityUtils.QUALITY_TAG, qualityTag.copy());
        }
    }

}
