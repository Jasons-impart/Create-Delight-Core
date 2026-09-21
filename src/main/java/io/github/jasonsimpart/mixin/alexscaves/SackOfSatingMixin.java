package io.github.jasonsimpart.mixin.alexscaves;

import com.github.alexmodguy.alexscaves.server.item.SackOfSatingItem;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(value = SackOfSatingItem.class, remap = false)
public abstract class SackOfSatingMixin {
    @ModifyArgs(method = "inventoryTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/food/FoodData;eat(IF)V"))
    private void createdelightcore$modifyEatAmount(Args args, @Local(argsOnly = true) ItemStack stack) {
        int hungerValue = SackOfSatingItem.getHunger(stack);
        if (hungerValue >= 5) {
            args.set(0, 5);
            args.set(1, 0.25F);
        } else {
            args.set(0, hungerValue);
            args.set(1, 0.05F * hungerValue);
        }
    }

    @ModifyArgs(method = "inventoryTick", at = @At(value = "INVOKE", target = "Lcom/github/alexmodguy/alexscaves/server/item/SackOfSatingItem;setHunger(Lnet/minecraft/world/item/ItemStack;I)V"))
    private void createdelightcore$modifySetAmount(Args args, @Local(argsOnly = true) ItemStack stack) {
        int hungerValue = SackOfSatingItem.getHunger(stack);
        args.set(1, hungerValue >= 5 ? hungerValue - 5 : 0);
    }
}
