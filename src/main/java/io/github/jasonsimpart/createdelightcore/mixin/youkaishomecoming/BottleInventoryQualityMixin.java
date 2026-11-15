package io.github.jasonsimpart.createdelightcore.mixin.youkaishomecoming;

import io.github.jasonsimpart.createdelightcore.compat.alcohol.YoukaiBottleQuality;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Inventory.class)
public abstract class BottleInventoryQualityMixin {
    @ModifyVariable(method = "placeItemBackInInventory(Lnet/minecraft/world/item/ItemStack;)V",
            at = @At("HEAD"), argsOnly = true)
    private ItemStack createdelightcore$qualifyBeforeMerging(ItemStack bottle) {
        return YoukaiBottleQuality.qualify(bottle);
    }
}
