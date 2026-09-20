package io.github.jasonsimpart.mixin.create;

import com.simibubi.create.compat.jei.category.ItemDrainCategory;
import com.simibubi.create.content.fluids.transfer.EmptyingRecipe;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.simibubi.create.compat.jei.category.CreateRecipeCategory.getRenderedSlot;

@Mixin(value = ItemDrainCategory.class, remap = false)
public abstract class ItemDrainCategoryMixin {
    @Inject(method = "setRecipe(Lmezz/jei/api/gui/builder/IRecipeLayoutBuilder;Lcom/simibubi/create/content/fluids/transfer/EmptyingRecipe;Lmezz/jei/api/recipe/IFocusGroup;)V", at = @At("TAIL"))
    private void createdelightcore$showExtraOutputs(IRecipeLayoutBuilder builder, EmptyingRecipe recipe,
                                                   IFocusGroup focuses, CallbackInfo ci) {
        for (int i = 1; i < recipe.getRollableResults().size(); i++) {
            var output = recipe.getRollableResults().get(i);
            builder.addSlot(RecipeIngredientRole.OUTPUT, 132 + (i % 2) * 19, 27 + (i / 2) * 19)
                    .setBackground(getRenderedSlot(output.getChance()), -1, -1).addItemStack(output.getStack())
                    .addRichTooltipCallback((view, tooltip) -> {
                        if (output.getChance() != 1) tooltip.add(net.minecraft.network.chat.Component.literal(
                                Float.toString(output.getChance() * 100) + "%").withStyle(net.minecraft.ChatFormatting.GOLD));
                    });
        }
    }
}
