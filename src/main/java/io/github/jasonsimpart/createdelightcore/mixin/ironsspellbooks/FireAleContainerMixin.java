package io.github.jasonsimpart.createdelightcore.mixin.ironsspellbooks;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.redspace.ironsspellbooks.item.consumables.DrinkableItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Keep fire ale's consumed container consistent with its B&C pouring recipe. */
@Mixin(value = DrinkableItem.class, remap = false)
public abstract class FireAleContainerMixin {
    @ModifyExpressionValue(method = "finishUsingItem", remap = true, at = @At(value = "FIELD",
            target = "Lio/redspace/ironsspellbooks/item/consumables/DrinkableItem;returnItem:Lnet/minecraft/world/item/Item;",
            remap = false))
    private Item createdelightcore$returnTankard(Item original) {
        if (!new ResourceLocation("irons_spellbooks", "fire_ale").equals(ForgeRegistries.ITEMS.getKey((Item) (Object) this))) return original;
        Item tankard = ForgeRegistries.ITEMS.getValue(new ResourceLocation("brewinandchewin", "tankard"));
        return tankard == null || tankard == Items.AIR ? original : tankard;
    }
}
