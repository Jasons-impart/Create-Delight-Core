package io.github.jasonsimpart.compat.jei.category;

import com.simibubi.create.foundation.gui.AllGuiTextures;
import fr.iglee42.cmr.cooler.SnowmanCoolerBlock.HeatLevel;
import fr.iglee42.cmr.init.CMRRegistries;
import io.github.jasonsimpart.CreateDelightCore;
import io.github.jasonsimpart.compat.jei.utils.AnimatedSnowmanCooler;
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

public class JeiCategorySnowmanCoolerFluid implements IRecipeCategory<JeiCategorySnowmanCoolerFluid.SnowmanCoolerFluidRecipe> {
    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(CreateDelightCore.MODID, "snowman_cooler");
    public static final RecipeType<SnowmanCoolerFluidRecipe> RECIPE_TYPE = new RecipeType<>(UID, SnowmanCoolerFluidRecipe.class);

    private static final int WIDTH = 177;
    private static final int HEIGHT = 53;

    private final IJeiHelpers helpers;
    private final AnimatedSnowmanCooler cooler = new AnimatedSnowmanCooler();

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
    public int getWidth() {
        return WIDTH;
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }

    @Override
    public @Nullable IDrawable getIcon() {
        return helpers.getGuiHelper().createDrawableItemStack(CMRRegistries.SNOWMAN_COOLER.asStack());
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, SnowmanCoolerFluidRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, WIDTH / 2 - 16, 3)
                .addFluidStack(recipe.fluid(), recipe.amountConsume());
    }

    @Override
    public void draw(SnowmanCoolerFluidRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
        HeatLevel heat = recipe.isFreezing() ? HeatLevel.FREEZING : HeatLevel.COOLING;
        int color = recipe.isFreezing() ? 0x5555FF : 0x55FFFF;
        graphics.drawString(Minecraft.getInstance().font, formatTime(recipe.coolTime()), WIDTH / 2 + 48, 36, 0x404040, false);
        AllGuiTextures.JEI_LIGHT.render(graphics, 81, 38);
        AllGuiTextures.JEI_HEAT_BAR.render(graphics, 4, 30);
        graphics.drawString(
                Minecraft.getInstance().font,
                Component.translatable("jei." + CreateDelightCore.MODID + ".amountConsumeCool", recipe.amountConsume()),
                9,
                36,
                color,
                false
        );
        cooler.withHeat(heat).draw(graphics, WIDTH / 2 + 3, 5);
        AllGuiTextures.JEI_DOWN_ARROW.render(graphics, WIDTH / 2 + 3, 8);
    }

    public record SnowmanCoolerFluidRecipe(Fluid fluid, boolean isFreezing, int coolTime, int amountConsume) {
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
