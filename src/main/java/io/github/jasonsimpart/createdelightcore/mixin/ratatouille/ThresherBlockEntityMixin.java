package io.github.jasonsimpart.createdelightcore.mixin.ratatouille;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import de.cadentem.quality_food.util.QualityUtils;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import org.forsteri.ratatouille.content.thresher.ThresherBlockEntity;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ThresherBlockEntity.class)
public class ThresherBlockEntityMixin {
    @Shadow(remap = false)
    public ItemStackHandler inputInv;
    @Inject(method = "process", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V"))
    public void storeInput(CallbackInfo ci, @Local ItemStack stackInSlot, @Share("input") final LocalRef<ItemStack> input) {
        input.set(inputInv.getStackInSlot(0));
    }
    @ModifyArg(method = "lambda$process$1", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/items/ItemHandlerHelper;insertItemStacked(Lnet/minecraftforge/items/IItemHandler;Lnet/minecraft/world/item/ItemStack;Z)Lnet/minecraft/world/item/ItemStack;"), index = 1, remap = false)
    public @NotNull ItemStack applyQuality(@NotNull ItemStack stack, @Share("input") final LocalRef<ItemStack> input) {
        QualityUtils.applyQuality(stack, List.of(input.get()), null);
        return stack;
    }
}








