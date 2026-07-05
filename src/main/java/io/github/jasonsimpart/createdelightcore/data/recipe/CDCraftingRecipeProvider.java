package io.github.jasonsimpart.createdelightcore.data.recipe;

import io.github.jasonsimpart.createdelightcore.CreateDelightCore;
import io.github.jasonsimpart.createdelightcore.registry.CDBlocks;
import io.github.jasonsimpart.createdelightcore.registry.CDItems;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.world.item.Items;

import java.util.function.Consumer;

public class CDCraftingRecipeProvider extends RecipeProvider {
    public CDCraftingRecipeProvider(PackOutput output) {
        super(output);
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> writer) {
        ShapelessRecipeBuilder.shapeless(RecipeCategory.FOOD, CDBlocks.LUSH_CONFITURE.asItem(), 8)
                .requires(CDBlocks.LUSH_CONFITURE_JELLY.get())
                .requires(Items.GLASS_BOTTLE, 8)
                .unlockedBy("has_lush_confiture_jelly", has(CDBlocks.LUSH_CONFITURE_JELLY.get()))
                .save(writer, CreateDelightCore.id("lush_confiture_jelly"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.FOOD, CDBlocks.LUSH_CONFITURE_JELLY.get())
                .requires(CDBlocks.LUSH_CONFITURE.asItem(), 8)
                .unlockedBy("has_lush_confiture", has(CDBlocks.LUSH_CONFITURE.asItem()))
                .save(writer, CreateDelightCore.id("lush_confiture_jelly_block"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.FOOD, CDItems.LUSH_CONFITURE_JELLO.get(), 8)
                .requires(CDBlocks.LUSH_CONFITURE_JELLO_BLOCK.get())
                .requires(Items.BOWL, 8)
                .unlockedBy("has_lush_confiture_jello_block", has(CDBlocks.LUSH_CONFITURE_JELLO_BLOCK.get()))
                .save(writer, CreateDelightCore.id("lush_confiture_jello"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.FOOD, CDBlocks.LUSH_CONFITURE_JELLO_BLOCK.get())
                .requires(CDItems.LUSH_CONFITURE_JELLO.get(), 8)
                .unlockedBy("has_lush_confiture_jello", has(CDItems.LUSH_CONFITURE_JELLO.get()))
                .save(writer, CreateDelightCore.id("lush_confiture_jello_block"));
    }
}
