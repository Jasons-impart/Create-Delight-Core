package io.github.jasonsimpart.createdelightcore.jei.category;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.compat.jei.EmptyBackground;
import io.github.jasonsimpart.createdelightcore.CreateDelightCore;
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


public class JeiCategoryBlazeBurnerSuperHeat implements IRecipeCategory<JeiCategoryBlazeBurnerSuperHeat.BlazeBurnerRecipe> {
    public static final ResourceLocation UID = new ResourceLocation(CreateDelightCore.MODID, "blaze_burner_super_heat");
    private final IJeiHelpers helpers;

    public static final RecipeType<BlazeBurnerRecipe> RECIPE_TYPE = new RecipeType<>(UID, BlazeBurnerRecipe.class);
    public JeiCategoryBlazeBurnerSuperHeat(IJeiHelpers helpers){
        this.helpers = helpers;
    }

    @Override
    public RecipeType<BlazeBurnerRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.BlazeBurnerSuperHeat");
    }

    @Override
    public IDrawable getBackground() {
            return new EmptyBackground(130, 45);
    }

    @Override
    public @Nullable IDrawable getIcon() {
        return helpers.getGuiHelper().createDrawableItemStack(AllBlocks.BLAZE_BURNER.asStack());
    }


    @Override
    public void setRecipe(IRecipeLayoutBuilder iRecipeLayoutBuilder, BlazeBurnerRecipe fluid, IFocusGroup iFocusGroup) {
        iRecipeLayoutBuilder.addSlot(RecipeIngredientRole.INPUT, 110, 3).addFluidStack(fluid.fluid, 1000);
        iRecipeLayoutBuilder.addSlot(RecipeIngredientRole.INPUT, 110, 23).addItemStack(AllBlocks.BLAZE_BURNER.asStack());

    }


    @Override
    public void draw(BlazeBurnerRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        guiGraphics.drawString(Minecraft.getInstance().font, Component.translatable("jei.burnTime", recipe.burnTime), 10, 7, 0xFFFFFF);
        guiGraphics.drawString(Minecraft.getInstance().font, Component.translatable("jei.amountConsumedPerTick", recipe.perTick), 10, 27, 0xFFFFFF);
    }

    public record BlazeBurnerRecipe(Fluid fluid, int burnTime, int perTick) {
    }
}
