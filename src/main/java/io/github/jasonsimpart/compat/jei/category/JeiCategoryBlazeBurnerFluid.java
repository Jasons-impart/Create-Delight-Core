package io.github.jasonsimpart.compat.jei.category;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.compat.jei.EmptyBackground;
import com.simibubi.create.content.processing.recipe.HeatCondition;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import io.github.jasonsimpart.CreateDelightCore;
import io.github.jasonsimpart.compat.jei.utils.AnimatedBlazeBurner;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;

public class JeiCategoryBlazeBurnerFluid implements IRecipeCategory<JeiCategoryBlazeBurnerFluid.BlazeBurnerFluidRecipe> {
    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(CreateDelightCore.MODID, "blaze_burner");
    public static final RecipeType<BlazeBurnerFluidRecipe> RECIPE_TYPE = new RecipeType<>(UID, BlazeBurnerFluidRecipe.class);

    private final IJeiHelpers helpers;
    private final AnimatedBlazeBurner heater = new AnimatedBlazeBurner();
    private final IDrawable background = new EmptyBackground(177, 53);

    public JeiCategoryBlazeBurnerFluid(IJeiHelpers helpers) {
        this.helpers = helpers;
    }

    @Override
    public RecipeType<BlazeBurnerFluidRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei." + CreateDelightCore.MODID + ".BlazeBurnerFluid");
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public @Nullable IDrawable getIcon() {
        return helpers.getGuiHelper().createDrawableItemStack(AllBlocks.BLAZE_BURNER.asStack());
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, BlazeBurnerFluidRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, background.getWidth() / 2 - 16, 3)
                .addFluidStack(recipe.fluid(), recipe.amountConsume());
    }

    @Override
    public void draw(BlazeBurnerFluidRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
        HeatCondition heat = recipe.isSuperHeated() ? HeatCondition.SUPERHEATED : HeatCondition.HEATED;
        graphics.drawString(Minecraft.getInstance().font, formatTime(recipe.burnTime()), background.getWidth() / 2 + 48, 36, 0x404040, false);
        AllGuiTextures.JEI_LIGHT.render(graphics, 81, 38);
        AllGuiTextures.JEI_HEAT_BAR.render(graphics, 4, 30);
        graphics.drawString(
                Minecraft.getInstance().font,
                Component.translatable("jei." + CreateDelightCore.MODID + ".amountConsume", recipe.amountConsume()),
                9,
                36,
                heat.getColor(),
                false
        );
        heater.withHeat(heat.visualizeAsBlazeBurner()).draw(graphics, background.getWidth() / 2 + 3, 5);
        AllGuiTextures.JEI_DOWN_ARROW.render(graphics, background.getWidth() / 2 + 3, 8);
    }

    public record BlazeBurnerFluidRecipe(Fluid fluid, boolean isSuperHeated, int burnTime, int amountConsume) {
    }

    public static String formatTime(int ticks) {
        if (ticks >= 20 * 60) {
            return ticks / (20 * 60) + " m";
        }
        if (ticks >= 20) {
            return ticks / 20 + " s";
        }
        return ticks + " t";
    }
}
