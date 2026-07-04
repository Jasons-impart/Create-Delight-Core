package io.github.jasonsimpart.createdelightcore.mixin.fruitsdelight;

import dev.xkmc.fruitsdelight.content.item.FDFoodItem;
import dev.xkmc.fruitsdelight.init.food.FruitType;
import io.github.jasonsimpart.createdelightcore.compat.fruitsdelight.CustomFDFruits;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = FDFoodItem.class, remap = false)
public class FDFoodItemMixin {
    @Inject(method = "getFruits(Lnet/minecraft/world/item/ItemStack;)Ljava/util/List;", at = @At("RETURN"), require = 0)
    private static void createdelightcore$readCustomFruits(ItemStack stack, CallbackInfoReturnable<List<FruitType>> cir) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(FDFoodItem.ROOT)) {
            return;
        }

        ListTag fruits = tag.getList(FDFoodItem.ROOT, Tag.TAG_STRING);
        for (int i = 0; i < fruits.size(); i++) {
            CustomFDFruits.getFruit(fruits.getString(i)).ifPresent(cir.getReturnValue()::add);
        }
    }
}
