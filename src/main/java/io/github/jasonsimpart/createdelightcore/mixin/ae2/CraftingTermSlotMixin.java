package io.github.jasonsimpart.createdelightcore.mixin.ae2;

import appeng.api.inventories.InternalInventory;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;
import appeng.menu.slot.CraftingTermSlot;
import com.llamalad7.mixinextras.sugar.Local;
import de.cadentem.quality_food.util.QualityUtils;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(CraftingTermSlot.class)
public abstract class CraftingTermSlotMixin {

    @Shadow(remap = false)
    abstract InternalInventory getPattern();
    @Shadow(remap = false)
    abstract protected Recipe<CraftingContainer> findRecipe(CraftingContainer ic, Level level);
    @Final
    @Shadow(remap = false)
    private  InternalInventory craftInv;

    @Inject(method = "craftItem", at = @At(value = "RETURN", ordinal = 3), remap = false, require = 1, cancellable = true)
    public void craftItemMixin(Player p, MEStorage inv, KeyCounter all, CallbackInfoReturnable<ItemStack> cir, @Local(ordinal = 0) ItemStack is) {
        ItemStack itemStack = is.copy();
        TransientCraftingContainer ic = new TransientCraftingContainer(p.containerMenu, 3, 3);

        for(int x = 0; x < 9; ++x) {
            ic.setItem(x, this.getPattern().getStackInSlot(x));
        }
        Recipe<CraftingContainer> r = this.findRecipe(ic, p.level());
        QualityUtils.handleConversion(itemStack, ic, r, p.level().registryAccess());
        cir.setReturnValue(itemStack);
    }
}
