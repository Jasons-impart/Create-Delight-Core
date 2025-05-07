package io.github.jasonsimpart.createdelightcore.mixin.ratatouille;

import com.llamalad7.mixinextras.sugar.Local;
import de.cadentem.quality_food.util.QualityUtils;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import org.forsteri.ratatouille.content.thresher.ThresherBlockEntity;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.List;

@Mixin(ThresherBlockEntity.class)
public class ThresherBlockEntityMixin {
    @Shadow
    public ItemStackHandler inputInv;
    @ModifyArg(method = "lambda$process$1", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/items/ItemHandlerHelper;insertItemStacked(Lnet/minecraftforge/items/IItemHandler;Lnet/minecraft/world/item/ItemStack;Z)Lnet/minecraft/world/item/ItemStack;"), index = 1)
    public @NotNull ItemStack applyQuality(@NotNull ItemStack stack) {
        QualityUtils.applyQuality(stack, List.of(inputInv.getStackInSlot(0)), null);
        return stack;
    }
}








