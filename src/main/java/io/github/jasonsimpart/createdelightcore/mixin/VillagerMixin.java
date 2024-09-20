package io.github.jasonsimpart.createdelightcore.mixin;

import io.github.jasonsimpart.createdelightcore.registry.CDItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Villager.class)
public abstract class VillagerMixin {
    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    public void readAdditionalSaveData(CompoundTag pCompound, CallbackInfo ci) {
        Villager self = (Villager) (Object) this;
        var offers = self.offers;
        if (offers == null)
            return;
        MerchantOffers offers_copy = (MerchantOffers) offers.clone();
        offers.clear();
        offers.addAll(offers_copy.stream().map(offer -> {
            ItemStack itemA = offer.getBaseCostA();
            ItemStack itemB = offer.getCostB();
            ItemStack itemC = offer.getResult();
            if (itemA.is(Items.EMERALD))
                itemA = new ItemStack(CDItems.COPPER_COIN, itemA.getCount());
            if (itemB.is(Items.EMERALD))
                itemB = new ItemStack(CDItems.COPPER_COIN, itemB.getCount());
            if (itemC.is(Items.EMERALD))
                itemC = new ItemStack(CDItems.COPPER_COIN, itemC.getCount());
            return new MerchantOffer(itemA, itemB, itemC, offer.getUses(), offer.getMaxUses(), offer.getXp(), offer.getPriceMultiplier(), offer.getDemand());
        }).toList());
    }
}
