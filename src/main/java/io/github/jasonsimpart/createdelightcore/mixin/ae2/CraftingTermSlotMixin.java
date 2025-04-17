package io.github.jasonsimpart.createdelightcore.mixin.ae2;

import appeng.api.inventories.InternalInventory;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;
import appeng.menu.slot.CraftingTermSlot;
import de.cadentem.quality_food.util.QualityUtils;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(CraftingTermSlot.class)
public abstract class CraftingTermSlotMixin {

    @Shadow(remap = false)
    abstract InternalInventory getPattern();
    @Inject(method = "craftItem", at = @At(value = "RETURN"), cancellable = true, remap = false)
    public void craftItemMixin(Player p, MEStorage inv, KeyCounter all, CallbackInfoReturnable<ItemStack> cir) {
        List<ItemStack> itemStacks = new ArrayList<>();
        getPattern().forEach(itemStacks::add);
        ItemStack outputItem = cir.getReturnValue();
        QualityUtils.applyQuality(outputItem, itemStacks, p);
        cir.setReturnValue(outputItem);
    }
}
