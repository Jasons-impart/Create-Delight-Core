package io.github.jasonsimpart.createdelightcore.mixin.lunarian;

import io.github.jasonsimpart.createdelightcore.registry.CDItems;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "earth.terrarium.adastra.common.entities.mob.lunarians.LunarianMerchantOffer$ProcessItemFactory", remap = false)
public abstract class ProcessItemFactoryMixin {
    @Shadow @Final private int price;

    @Shadow @Final private ItemStack secondBuy;

    @Shadow @Final private int secondCount;

    @Shadow @Final private ItemStack sell;

    @Shadow @Final private int sellCount;

    @Shadow @Final private int maxUses;

    @Shadow @Final private int experience;

    @Shadow @Final private float multiplier;

    @Inject(method = "getOffer", at = @At("RETURN"), cancellable = true)
    public void changeOffer(Entity entity, RandomSource random, CallbackInfoReturnable<MerchantOffer> cir) {
        cir.setReturnValue(new MerchantOffer(new ItemStack(CDItems.COPPER_COIN, price), new ItemStack(secondBuy.getItem(), secondCount), new ItemStack(sell.getItem(), sellCount), maxUses, experience, multiplier));
    }
}
