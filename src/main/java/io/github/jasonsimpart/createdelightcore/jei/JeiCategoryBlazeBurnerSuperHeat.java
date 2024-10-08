package io.github.jasonsimpart.createdelightcore.jei;

import com.forsteri.createliquidfuel.core.BurnerStomachHandler;
import com.forsteri.createliquidfuel.util.Triplet;
import com.mojang.datafixers.util.Pair;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.compat.jei.EmptyBackground;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlockEntity;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlockItem;
import io.github.jasonsimpart.createdelightcore.CreateDelightCore;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;

import java.util.Map;


public class JeiCategoryBlazeBurnerSuperHeat implements IRecipeCategory<Fluid> {
    public static final ResourceLocation UID = new ResourceLocation(CreateDelightCore.MODID, "blaze_burner_super_heat");
    private final IJeiHelpers helpers;

    public static final RecipeType<Fluid> RECIPE_TYPE = new RecipeType<>(UID, Fluid.class);
    public JeiCategoryBlazeBurnerSuperHeat(IJeiHelpers helpers){
        this.helpers = helpers;
    }

    @Override
    public RecipeType<Fluid> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.BlazeBurnerSuperHeat");
    }

    @Override
    public IDrawable getBackground() {
            return new EmptyBackground(120, 25);
    }

    @Override
    public @Nullable IDrawable getIcon() {
        return helpers.getGuiHelper().createDrawableItemStack(AllBlocks.BLAZE_BURNER.asStack());
    }


    @Override
    public void setRecipe(IRecipeLayoutBuilder iRecipeLayoutBuilder, Fluid fluid, IFocusGroup iFocusGroup) {
        iRecipeLayoutBuilder.addSlot(RecipeIngredientRole.INPUT, 70, 3).addItemStack(AllBlocks.BLAZE_BURNER.asStack());
        iRecipeLayoutBuilder.addSlot(RecipeIngredientRole.INPUT, 90, 3).addFluidStack(fluid, 1000);
    }


    @Override
    public void draw(Fluid recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        guiGraphics.drawString(Minecraft.getInstance().font, Component.translatable("jei.SuperHeat"), 10, 7, 0xFFFFFF);
    }
}
