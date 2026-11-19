package io.github.jasonsimpart.createdelightcore.mixin.dungeonsdelight;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.yirmiri.dungeonsdelight.common.block.entity.container.MonsterPotRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Arrays;

/** The one-litre wort recipe spends four items in each of its six ingredient slots. */
@Mixin(targets = "net.yirmiri.dungeonsdelight.integration.jei.category.MonsterPotRecipeCategory", remap = false)
public abstract class MonsterPotWortJeiMixin {
    @WrapOperation(method = "setRecipe(Lmezz/jei/api/gui/builder/IRecipeLayoutBuilder;Lnet/yirmiri/dungeonsdelight/common/block/entity/container/MonsterPotRecipe;Lmezz/jei/api/recipe/IFocusGroup;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/crafting/Ingredient;getItems()[Lnet/minecraft/world/item/ItemStack;", remap = true))
    private ItemStack[] createdelightcore$showFourServings(Ingredient ingredient, Operation<ItemStack[]> original,
                                                          @Local(argsOnly = true) MonsterPotRecipe recipe) {
        ItemStack[] stacks = original.call(ingredient);
        if (!recipe.getId().equals(new ResourceLocation("createdelight", "alcohol_preparation/ghastly_spirits"))) return stacks;
        return Arrays.stream(stacks).map(stack -> stack.copyWithCount(4)).toArray(ItemStack[]::new);
    }
}
