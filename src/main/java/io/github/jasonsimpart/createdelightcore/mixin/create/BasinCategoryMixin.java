package io.github.jasonsimpart.createdelightcore.mixin.create;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.compat.jei.category.BasinCategory;
import com.simibubi.create.compat.jei.category.CreateRecipeCategory;
import com.simibubi.create.content.processing.basin.BasinRecipe;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.simibubi.create.content.processing.recipe.HeatCondition;
import com.simibubi.create.foundation.fluid.FluidIngredient;
import com.simibubi.create.foundation.item.ItemHelper;
import io.github.jasonsimpart.createdelightcore.content.recipe.BerrySyrupFluidMixingRecipe;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.createmod.catnip.data.Pair;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.apache.commons.lang3.mutable.MutableInt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = BasinCategory.class, remap = false)
public abstract class BasinCategoryMixin {
    @org.spongepowered.asm.mixin.injection.Redirect(method = "setRecipe", at = @At(value = "INVOKE",
            target = "Lcom/simibubi/create/content/processing/recipe/HeatCondition;testBlazeBurner(Lcom/simibubi/create/content/processing/burner/BlazeBurnerBlock$HeatLevel;)Z"))
    private boolean createdelightcore$hideHeatingCatalysts(HeatCondition heat, HeatLevel level) {
        return io.github.jasonsimpart.createdelightcore.content.cooler.CoolerHeat.isCooling(heat) || heat.testBlazeBurner(level);
    }

    @Inject(method = "setRecipe", at = @At("TAIL"))
    private void createdelightcore$coolingCatalysts(IRecipeLayoutBuilder builder, BasinRecipe recipe,
                                                    IFocusGroup focuses, CallbackInfo ci) {
        if (!io.github.jasonsimpart.createdelightcore.content.cooler.CoolerHeat.isCooling(recipe.getRequiredHeat())) return;
        builder.addSlot(RecipeIngredientRole.RENDER_ONLY, 134, 81)
                .addItemStack(io.github.jasonsimpart.createdelightcore.registry.CDBlocks.SNOWMAN_COOLER.asStack());
        if (io.github.jasonsimpart.createdelightcore.content.cooler.CoolerHeat.isFreezing(recipe.getRequiredHeat()))
            builder.addSlot(RecipeIngredientRole.CATALYST, 153, 81)
                    .addItemStack(io.github.jasonsimpart.createdelightcore.registry.CDItems.FROZEN_CAKE.asStack());
    }

    @Inject(method = "setRecipe", at = @At("HEAD"), cancellable = true)
    private void createdelightcore$setDynamicBerrySyrupRecipe(IRecipeLayoutBuilder builder, BasinRecipe recipe,
                                                              IFocusGroup focuses, CallbackInfo ci) {
        if (!(recipe instanceof BerrySyrupFluidMixingRecipe berrySyrupRecipe)) {
            return;
        }

        addInputs(builder, berrySyrupRecipe);
        CreateRecipeCategory.addFluidSlot(builder, 142, 51, RecipeIngredientRole.OUTPUT)
                .addIngredients(ForgeTypes.FLUID_STACK, berrySyrupRecipe.getJeiFluidResults());
        addHeatCatalysts(builder, berrySyrupRecipe);
        ci.cancel();
    }

    private static void addInputs(IRecipeLayoutBuilder builder, BasinRecipe recipe) {
        List<Pair<Ingredient, MutableInt>> condensedIngredients = ItemHelper.condenseIngredients(recipe.getIngredients());
        int size = condensedIngredients.size() + recipe.getFluidIngredients().size();
        int xOffset = size < 3 ? (3 - size) * 19 / 2 : 0;
        int i = 0;

        for (Pair<Ingredient, MutableInt> pair : condensedIngredients) {
            List<ItemStack> stacks = new ArrayList<>();
            for (ItemStack itemStack : pair.getFirst().getItems()) {
                ItemStack copy = itemStack.copy();
                copy.setCount(pair.getSecond().getValue());
                stacks.add(copy);
            }

            builder.addSlot(RecipeIngredientRole.INPUT, 17 + xOffset + (i % 3) * 19, 51 - (i / 3) * 19)
                    .setBackground(CreateRecipeCategory.getRenderedSlot(), -1, -1)
                    .addItemStacks(stacks);
            i++;
        }

        for (FluidIngredient fluidIngredient : recipe.getFluidIngredients()) {
            int x = 17 + xOffset + (i % 3) * 19;
            int y = 51 - (i / 3) * 19;
            CreateRecipeCategory.addFluidSlot(builder, x, y, fluidIngredient);
            i++;
        }
    }

    private static void addHeatCatalysts(IRecipeLayoutBuilder builder, BasinRecipe recipe) {
        HeatCondition requiredHeat = recipe.getRequiredHeat();
        if (!requiredHeat.testBlazeBurner(HeatLevel.NONE)) {
            builder.addSlot(RecipeIngredientRole.RENDER_ONLY, 134, 81)
                    .addItemStack(AllBlocks.BLAZE_BURNER.asStack());
        }
        if (!requiredHeat.testBlazeBurner(HeatLevel.KINDLED)) {
            builder.addSlot(RecipeIngredientRole.CATALYST, 153, 81)
                    .addItemStack(AllItems.BLAZE_CAKE.asStack());
        }
    }
}
