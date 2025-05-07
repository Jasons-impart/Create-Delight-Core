package io.github.jasonsimpart.createdelightcore.mixin.ratatouille;

import com.llamalad7.mixinextras.sugar.Local;
import de.cadentem.quality_food.util.QualityUtils;
import net.minecraft.world.item.ItemStack;
import org.forsteri.ratatouille.content.oven.BakeData;
import org.forsteri.ratatouille.content.oven.OvenBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.List;

@Mixin(BakeData.class)
public class BakeDataMixin {
    @ModifyArg(method = "processFood",index = 1, at = @At(value = "INVOKE", target = "Lorg/forsteri/ratatouille/content/oven/OvenBlockEntity$Inventory;setStackInSlot(ILnet/minecraft/world/item/ItemStack;)V"), remap = false)
    public ItemStack applyQuality(ItemStack par2, @Local OvenBlockEntity.Inventory inventory) {
        QualityUtils.applyQuality(par2, List.of(inventory.getStackInSlot(0)), null);
        return par2;
    }
}
