package io.github.jasonsimpart.createdelightcore.mixin.createbicbit;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.pyzpre.createbitterballen.block.mechanicalfryer.DeepFryingRecipe;
import com.pyzpre.createbitterballen.block.mechanicalfryer.MechanicalFryerEntity;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import de.cadentem.quality_food.util.QualityUtils;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(DeepFryingRecipe.class)
public class DeepFryingRecipeMixin {
    @Unique
    private static final Object create_Delight_Core$lock = new Object();
    @Inject(method = "apply(Lcom/simibubi/create/content/processing/basin/BasinBlockEntity;Lcom/pyzpre/createbitterballen/block/mechanicalfryer/MechanicalFryerEntity;Lnet/minecraft/world/item/crafting/Recipe;Z)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V"), remap = false)
    private static void storeInput(BasinBlockEntity basin, MechanicalFryerEntity fryer, Recipe<?> recipe, boolean test, CallbackInfoReturnable<Boolean> cir, @Share("input") final LocalRef<ItemStack> input, @Local IItemHandler availableItems) {
        input.set(availableItems.getStackInSlot(0).copy());
    }
    @ModifyArg(method = "apply(Lcom/simibubi/create/content/processing/basin/BasinBlockEntity;Lcom/pyzpre/createbitterballen/block/mechanicalfryer/MechanicalFryerEntity;Lnet/minecraft/world/item/crafting/Recipe;Z)Z", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/items/ItemHandlerHelper;insertItemStacked(Lnet/minecraftforge/items/IItemHandler;Lnet/minecraft/world/item/ItemStack;Z)Lnet/minecraft/world/item/ItemStack;"), index = 1, remap = false)
    private static @NotNull ItemStack applyQuality(@NotNull ItemStack stack, @Share("input") final LocalRef<ItemStack> input) {
        synchronized (create_Delight_Core$lock) {
            QualityUtils.applyQuality(stack, List.of(input.get()), null);
            return stack;
        }
    }

}
