package io.github.jasonsimpart.createdelightcore.compat.jei.category;

import com.mrh0.createaddition.index.CAItems;
import com.mrh0.createaddition.util.ClientMinecraftWrapper;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.compat.jei.EmptyBackground;
import com.simibubi.create.compat.jei.category.animations.AnimatedBlazeBurner;
import com.simibubi.create.content.processing.recipe.HeatCondition;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.utility.Lang;

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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;

import java.util.List;

import org.jetbrains.annotations.Nullable;


public class JeiCategoryBlazeBurnerSuperHeat implements IRecipeCategory<JeiCategoryBlazeBurnerSuperHeat.BlazeBurnerRecipe> {
    public static final ResourceLocation UID = new ResourceLocation(CreateDelightCore.MODID, "blaze_burner_super_heat");
    private final IJeiHelpers helpers;
    private final AnimatedBlazeBurner heater = new AnimatedBlazeBurner();

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
        return Component.translatable("jei." + CreateDelightCore.MODID + "BlazeBurnerSuperHeat");
    }

    @Override
    public IDrawable getBackground() {
        return new EmptyBackground(177, 53);
    }

    @Override
    public @Nullable IDrawable getIcon() {
        return helpers.getGuiHelper().createDrawableItemStack(AllBlocks.BLAZE_BURNER.asStack());
    }


    @Override
    public void setRecipe(IRecipeLayoutBuilder iRecipeLayoutBuilder, BlazeBurnerRecipe fluid, IFocusGroup iFocusGroup) {
        iRecipeLayoutBuilder.addSlot(RecipeIngredientRole.INPUT, getBackground().getWidth() / 2 -16, 3).addFluidStack(fluid.fluid, 1000);
        iRecipeLayoutBuilder.addSlot(RecipeIngredientRole.INPUT, getBackground().getWidth() / 2 -36, 3).addItemStack(AllBlocks.BLAZE_BURNER.asStack());
    }


    @Override
    public void draw(BlazeBurnerRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        // 老GUI
        // guiGraphics.drawString(Minecraft.getInstance().font, Component.translatable("jei." + CreateDelightCore.MODID + "burnTime", recipe.burnTime), 10, 7, 0xFFFFFF);
        // guiGraphics.drawString(Minecraft.getInstance().font, Component.translatable("jei." + CreateDelightCore.MODID + "amountConsumedPerTick", recipe.perTick), 10, 27, 0xFFFFFF);

        // 新GUI 参考CC&A https://github.com/mrh0/createaddition
        guiGraphics.drawString(ClientMinecraftWrapper.getFont(), formatTime(recipe.burnTime()), getBackground().getWidth() / 2 + 48, 86 - 50, 4210752);

		HeatCondition requiredHeat = recipe.isSuperheated() ? HeatCondition.SUPERHEATED : HeatCondition.HEATED;

		AllGuiTextures.JEI_LIGHT.render(guiGraphics, 81, 58 + 30 - 50);

		AllGuiTextures.JEI_HEAT_BAR.render(guiGraphics, 4, 80 - 50);
		guiGraphics.drawString(ClientMinecraftWrapper.getFont(), Lang.translateDirect(requiredHeat.getTranslationKey()), 9,
				86 - 50, requiredHeat.getColor());

		heater.withHeat(requiredHeat.visualizeAsBlazeBurner())
			.draw(guiGraphics, getBackground().getWidth() / 2 + 3, 55 - 50);

		AllGuiTextures.JEI_DOWN_ARROW.render(guiGraphics, getBackground().getWidth() / 2 + 3, 8);
    }

    public record BlazeBurnerRecipe(Fluid fluid, boolean isSuperheated, int burnTime, int perTick) {
    }

    public static String formatTime(int ticks) {
		if (ticks >= 20*60) return (ticks/(20*60)) + " min";
		if (ticks >= 20) return (ticks/20) + " sec";
		return (ticks) + " ticks";
	}
}
