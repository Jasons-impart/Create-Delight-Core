package io.github.jasonsimpart.createdelightcore.mixin.quark;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.jasonsimpart.createdelightcore.content.util.StorageRecipeQuality;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@Mixin(targets = "org.violetmoon.quark.content.automation.block.be.CrafterBlockEntity", remap = false)
public abstract class CrafterBlockEntityQualityMixin {
    @WrapOperation(method = "getResult", at = @At(value = "INVOKE", remap = false,
            target = "Lnet/minecraft/world/item/crafting/CraftingRecipe;m_5874_(Lnet/minecraft/world/Container;Lnet/minecraft/core/RegistryAccess;)Lnet/minecraft/world/item/ItemStack;"),
            require = 1)
    private static ItemStack createdelightcore$convertResult(CraftingRecipe recipe, Container inputs,
            RegistryAccess registries, Operation<ItemStack> original,
            @Local(argsOnly = true) Level level) {
        return StorageRecipeQuality.convert(original.call(recipe, inputs, registries), inputs, recipe, level);
    }
}
