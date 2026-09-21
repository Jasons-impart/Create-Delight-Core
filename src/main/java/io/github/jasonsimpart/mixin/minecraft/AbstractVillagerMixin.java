package io.github.jasonsimpart.mixin.minecraft;

import io.github.jasonsimpart.registry.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(AbstractVillager.class)
public abstract class AbstractVillagerMixin {
    @Shadow
    protected MerchantOffers offers;

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void createdelightcore$convertEmeraldOffers(CompoundTag tag, CallbackInfo ci) {
        if (offers == null) {
            return;
        }
        for (int i = 0; i < offers.size(); i++) {
            offers.set(i, createdelightcore$convertOffer(offers.get(i)));
        }
    }

    private static MerchantOffer createdelightcore$convertOffer(MerchantOffer offer) {
        ItemCost costA = createdelightcore$convertCost(offer.getItemCostA());
        Optional<ItemCost> costB = offer.getItemCostB().map(AbstractVillagerMixin::createdelightcore$convertCost);
        ItemStack result = offer.getResult().is(Items.EMERALD)
                ? new ItemStack(ModItems.COPPER_COIN.get(), offer.getResult().getCount())
                : offer.getResult();
        return new MerchantOffer(costA, costB, result, offer.getUses(), offer.getMaxUses(), offer.getXp(), offer.getPriceMultiplier(), offer.getDemand());
    }

    private static ItemCost createdelightcore$convertCost(ItemCost cost) {
        return cost.item().value() == Items.EMERALD ? new ItemCost(ModItems.COPPER_COIN.get(), cost.count()) : cost;
    }
}
