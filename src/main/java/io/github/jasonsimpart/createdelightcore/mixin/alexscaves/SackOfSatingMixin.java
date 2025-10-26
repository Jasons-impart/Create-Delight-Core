package io.github.jasonsimpart.createdelightcore.mixin.alexscaves;

import com.github.alexmodguy.alexscaves.server.item.SackOfSatingItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(value = SackOfSatingItem.class, remap = false)
public class SackOfSatingMixin extends Item {
    public SackOfSatingMixin(Properties pProperties) {
        super(pProperties);
    }

    private ItemStack itemStackMixin;
    // 修改 player.getFoodData().eat(...) 的第一个参数
    @ModifyArgs(
            method = "inventoryTick", // 包含调用 eat() 的方法
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/github/alexmodguy/alexscaves/server/item/SackOfSatingItem;getHunger(Lnet/minecraft/world/item/ItemStack;)I"
            )
    )
    private void getStacks(Args args) {
        itemStackMixin = args.get(0);
    }

   // 修改 player.getFoodData().eat(...) 的第一个参数
    @ModifyArgs(
            method = "inventoryTick", // 包含调用 eat() 的方法
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/food/FoodData;eat(IF)V"
            )
    )
    private void modifyEatAmount(Args args) {
        if(itemStackMixin == null){
            return;
        }
        var hungerValue = SackOfSatingItem.getHunger(itemStackMixin);
        if(hungerValue >= 5){
            args.set(0, 5);
            args.set(1, 0.25F);
        }
        else {
            args.set(0,hungerValue);
            args.set(1, 0.05F * hungerValue);

        }
    }


    // 修改 player.getFoodData().eat(...) 的第一个参数
    @ModifyArgs(
            method = "inventoryTick", // 包含调用 eat() 的方法
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/github/alexmodguy/alexscaves/server/item/SackOfSatingItem;setHunger(Lnet/minecraft/world/item/ItemStack;I)V"
            )
    )
    private void modifySetAmount(Args args) {
        if(itemStackMixin == null){
            return;
        }
        var hungerValue = SackOfSatingItem.getHunger(itemStackMixin);
        if(hungerValue >= 5){
            args.set(1, hungerValue -5);
        }
        else {
            args.set(1, 0);

        }
    }

}
