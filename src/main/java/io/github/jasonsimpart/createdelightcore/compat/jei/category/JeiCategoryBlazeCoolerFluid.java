package io.github.jasonsimpart.createdelightcore.compat.jei.category;

import com.mrh0.createaddition.util.ClientMinecraftWrapper;
import com.simibubi.create.compat.jei.EmptyBackground;
import com.simibubi.create.foundation.gui.AllGuiTextures;

import com.yision.fluidlogistics.registry.AllBlocks;
import io.github.jasonsimpart.createdelightcore.CreateDelightCore;
import com.yision.fluidlogistics.compat.jei.animation.AnimatedBlazeCooler;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;

import org.jetbrains.annotations.Nullable;

public class JeiCategoryBlazeCoolerFluid
        implements IRecipeCategory<JeiCategoryBlazeCoolerFluid.BlazeCoolerFluidRecipe> {
    public static final ResourceLocation UID = CreateDelightCore.id("blaze_cooler");
    private final IJeiHelpers helpers;
    private final AnimatedBlazeCooler cooler = new AnimatedBlazeCooler();

    public static final RecipeType<BlazeCoolerFluidRecipe> RECIPE_TYPE = new RecipeType<>(UID,
            BlazeCoolerFluidRecipe.class);

    public JeiCategoryBlazeCoolerFluid(IJeiHelpers helpers) {
        this.helpers = helpers;
    }

    @Override
    public RecipeType<BlazeCoolerFluidRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei." + CreateDelightCore.MODID + ".BlazeCoolerFluid");
    }

    @Override
    public IDrawable getBackground() {
        return new EmptyBackground(177, 53);
    }

    @Override
    public @Nullable IDrawable getIcon() {
        return helpers.getGuiHelper().createDrawableItemStack(AllBlocks.BLAZE_COOLER.asStack());
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, BlazeCoolerFluidRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, getBackground().getWidth() / 2 - 16, 3)
                .addFluidStack(recipe.fluid(), recipe.amountConsume());
    }

    @Override
    public void draw(BlazeCoolerFluidRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics,
            double mouseX, double mouseY) {
        guiGraphics.drawString(ClientMinecraftWrapper.getFont(), formatTime(recipe.coolTime()),
                getBackground().getWidth() / 2 + 48, 86 - 50, 4210752);

        AllGuiTextures.JEI_LIGHT.render(guiGraphics, 81, 58 + 30 - 50);
        AllGuiTextures.JEI_HEAT_BAR.render(guiGraphics, 4, 80 - 50);

        int color = recipe.isSupercooled() ? 0x5555FF : 0x55FFFF;
        guiGraphics.drawString(ClientMinecraftWrapper.getFont(),
                Component.translatable("jei." + CreateDelightCore.MODID + ".amountConsumeCool", recipe.amountConsume()),
                9, 86 - 50, color);

        cooler.draw(guiGraphics, getBackground().getWidth() / 2 + 3, 55 - 50, recipe.isSupercooled());
        AllGuiTextures.JEI_DOWN_ARROW.render(guiGraphics, getBackground().getWidth() / 2 + 3, 8);
    }

    public record BlazeCoolerFluidRecipe(Fluid fluid, boolean isSupercooled, int coolTime, int amountConsume) {
    }

    public static String formatTime(int ticks) {
        if (ticks >= 20 * 60)
            return (ticks / (20 * 60)) + " m";
        if (ticks >= 20)
            return (ticks / 20) + " s";
        return ticks + " t";
    }
}
