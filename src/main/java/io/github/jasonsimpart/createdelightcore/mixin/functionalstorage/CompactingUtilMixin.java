package io.github.jasonsimpart.createdelightcore.mixin.functionalstorage;

import com.buuz135.functionalstorage.util.CompactingUtil;
import com.llamalad7.mixinextras.sugar.Local;
import de.cadentem.quality_food.util.QualityUtils;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(CompactingUtil.class)
public class CompactingUtilMixin {
    @Inject(method = "findAllMatchingRecipes", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z"), remap = false)
    public void findAllMatchingRecipesMixin(CraftingContainer crafting, CallbackInfoReturnable<List<ItemStack>> cir, @Local ItemStack result) {
        QualityUtils.applyQuality(result, QualityUtils.getQuality(crafting.getItem(0)));
    }

    @ModifyArg(method = "findUpperTier",at = @At(value = "INVOKE", target = "Lcom/buuz135/functionalstorage/util/CompactingUtil$Result;<init>(Lnet/minecraft/world/item/ItemStack;I)V"), index = 0, remap = false)
    public ItemStack findUpperMixin(ItemStack result, @Local(argsOnly = true) ItemStack stack) {
        QualityUtils.applyQuality(result, QualityUtils.getQuality(stack));
        return result;
    }
    @ModifyArg(method = "findLowerTier",at = @At(value = "INVOKE", target = "Lcom/buuz135/functionalstorage/util/CompactingUtil$Result;<init>(Lnet/minecraft/world/item/ItemStack;I)V"), index = 0, remap = false)
    public ItemStack findLowerMixin(ItemStack result, @Local(argsOnly = true) ItemStack stack) {
        QualityUtils.applyQuality(result, QualityUtils.getQuality(stack));
        return result;
    }
}
