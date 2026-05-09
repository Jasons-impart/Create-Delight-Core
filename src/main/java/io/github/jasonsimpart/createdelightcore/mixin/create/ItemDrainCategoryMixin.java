package io.github.jasonsimpart.createdelightcore.mixin.create;

import com.simibubi.create.compat.jei.category.CreateRecipeCategory;
import com.simibubi.create.compat.jei.category.ItemDrainCategory;
import com.simibubi.create.content.fluids.transfer.EmptyingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = ItemDrainCategory.class, remap = false)
public class ItemDrainCategoryMixin {

    @Inject(
            method = "setRecipe(Lmezz/jei/api/gui/builder/IRecipeLayoutBuilder;Lcom/simibubi/create/content/fluids/transfer/EmptyingRecipe;Lmezz/jei/api/recipe/IFocusGroup;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void createdelightcore$showStochasticItemOutput(IRecipeLayoutBuilder builder, EmptyingRecipe recipe, IFocusGroup focuses, CallbackInfo ci) {
        builder
                .addSlot(RecipeIngredientRole.INPUT, 27, 8)
                .setBackground(CreateRecipeCategory.getRenderedSlot(), -1, -1)
                .addIngredients(recipe.getIngredients().get(0));

        CreateRecipeCategory.addFluidSlot(builder, 132, 8, recipe.getResultingFluid());

        List<ProcessingOutput> outputs = recipe.getRollableResults();
        if (outputs.isEmpty()) {
            builder
                    .addSlot(RecipeIngredientRole.OUTPUT, 132, 27)
                    .setBackground(CreateRecipeCategory.getRenderedSlot(), -1, -1)
                    .addItemStack(CreateRecipeCategory.getResultItem(recipe));
        } else {
            ProcessingOutput output = outputs.get(0);
            builder
                    .addSlot(RecipeIngredientRole.OUTPUT, 132, 27)
                    .setBackground(CreateRecipeCategory.getRenderedSlot(output), -1, -1)
                    .addItemStack(output.getStack())
                    .addRichTooltipCallback(CreateRecipeCategory.addStochasticTooltip(output));
        }

        ci.cancel();
    }
}
