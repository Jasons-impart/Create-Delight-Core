// Adapted from Create More Recipes by iglee42 (MIT). See META-INF/licenses/create-more-recipes.txt.
package io.github.jasonsimpart.createdelightcore.compat.jei.category;

import net.minecraft.client.Minecraft;
import com.simibubi.create.compat.jei.EmptyBackground;
import com.simibubi.create.foundation.gui.AllGuiTextures;

import io.github.jasonsimpart.createdelightcore.content.cooler.SnowmanCoolerBlock.HeatLevel;
import io.github.jasonsimpart.createdelightcore.registry.CDBlocks;
import io.github.jasonsimpart.createdelightcore.CreateDelightCore;
import io.github.jasonsimpart.createdelightcore.compat.jei.utils.AnimatedSnowmanCooler;
import io.github.jasonsimpart.createdelightcore.compat.jei.utils.FuelTimeFormatter;
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

public class JeiCategorySnowmanCoolerFluid
        implements IRecipeCategory<JeiCategorySnowmanCoolerFluid.SnowmanCoolerFluidRecipe> {
    public static final ResourceLocation UID = CreateDelightCore.id("snowman_cooler");
    private final IJeiHelpers helpers;
    private final AnimatedSnowmanCooler cooler = new AnimatedSnowmanCooler();

    public static final RecipeType<SnowmanCoolerFluidRecipe> RECIPE_TYPE = new RecipeType<>(UID,
            SnowmanCoolerFluidRecipe.class);

    public JeiCategorySnowmanCoolerFluid(IJeiHelpers helpers) {
        this.helpers = helpers;
    }

    @Override
    public RecipeType<SnowmanCoolerFluidRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei." + CreateDelightCore.MODID + ".SnowmanCoolerFluid");
    }

    @Override
    public IDrawable getBackground() {
        return new EmptyBackground(177, 53);
    }

    @Override
    public @Nullable IDrawable getIcon() {
        return helpers.getGuiHelper().createDrawableItemStack(CDBlocks.SNOWMAN_COOLER.asStack());
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, SnowmanCoolerFluidRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, getBackground().getWidth() / 2 - 16, 3)
                .addFluidStack(recipe.fluid(), recipe.amountConsume());
    }

    @Override
    public void draw(SnowmanCoolerFluidRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics,
            double mouseX, double mouseY) {
        var font = Minecraft.getInstance().font;
        String duration = formatTime(recipe.coolTime());
        guiGraphics.drawString(font, duration,
                getBackground().getWidth() - 8 - font.width(duration), 86 - 50, 4210752);

        HeatLevel requiredHeat = recipe.isFreezing() ? HeatLevel.FREEZING : HeatLevel.COOLING;

        AllGuiTextures.JEI_LIGHT.render(guiGraphics, 81, 58 + 30 - 50);
        AllGuiTextures.JEI_HEAT_BAR.render(guiGraphics, 4, 80 - 50);

        int color = recipe.isFreezing() ? 0x5555FF : 0x55FFFF;
        guiGraphics.drawString(Minecraft.getInstance().font,
                Component.translatable("jei." + CreateDelightCore.MODID + ".amountConsumeCool", recipe.amountConsume()),
                9, 86 - 50, color);

        cooler.withHeat(requiredHeat).draw(guiGraphics, getBackground().getWidth() / 2 + 3, 55 - 50);
        AllGuiTextures.JEI_DOWN_ARROW.render(guiGraphics, getBackground().getWidth() / 2 + 3, 8);
    }

    public record SnowmanCoolerFluidRecipe(Fluid fluid, boolean isFreezing, int coolTime, int amountConsume) {
    }

    public static String formatTime(int ticks) {
        return FuelTimeFormatter.format(ticks);
    }
}
