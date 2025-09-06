package io.github.jasonsimpart.createdelightcore.mixin.refurbished_furniture;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import de.cadentem.quality_food.core.Quality;
import de.cadentem.quality_food.util.QualityUtils;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = { "com.mrcrayfish.furniture.refurbished.blockentity.StoveBlockEntity$CookingSpace" })
public class StoveBlockEntityMixin {
    @Inject(method = "onCompleteProcess", at = @At(value = "INVOKE", target = "Lcom/mrcrayfish/furniture/refurbished/blockentity/StoveBlockEntity$CookingSpace;getRecipe()Ljava/util/Optional;"), remap = false)
    public void storeInput(CallbackInfo ci, @Local(ordinal = 0) ItemStack itemStack, @Share("ovenInput") final LocalRef<Quality> input) {
        input.set(QualityUtils.getQuality(itemStack));
    }

    @Redirect(method = "onCompleteProcess", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;copy()Lnet/minecraft/world/item/ItemStack;"), remap = false)
    public ItemStack applyQualityToCopy(ItemStack instance, @Share("ovenInput") final LocalRef<Quality> input) {
        QualityUtils.applyQuality(instance, input.get());
        return instance.copy();
    }


}
