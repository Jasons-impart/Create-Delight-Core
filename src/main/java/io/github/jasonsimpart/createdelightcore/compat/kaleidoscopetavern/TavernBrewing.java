package io.github.jasonsimpart.createdelightcore.compat.kaleidoscopetavern;

import com.github.ysbbbbbb.kaleidoscopetavern.blockentity.brew.BarrelBlockEntity;
import com.github.ysbbbbbb.kaleidoscopetavern.init.ModRecipes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.RecipeMatcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Starter is an ordinary ingredient: one item per bottle, sixteen per full barrel. */
public final class TavernBrewing {
    public static final int YEAST_PER_BARREL = 16;
    public static final TagKey<Item> YEAST = TagKey.create(Registries.ITEM,
            new ResourceLocation("createdelight", "tavern_yeast"));

    private TavernBrewing() {}

    public static boolean isYeast(ItemStack stack) { return stack.is(YEAST); }

    public static boolean isYeastIngredient(Ingredient ingredient) {
        return Arrays.stream(ingredient.getItems()).anyMatch(TavernBrewing::isYeast);
    }

    public static boolean awaitingYeast(BarrelBlockEntity barrel, Level level) {
        if (barrel.getFluid().getFluidAmount() < 4000) return false;
        List<ItemStack> inputs = new ArrayList<>();
        int yeast = 0;
        for (int slot = 0; slot < barrel.getIngredient().getSlots(); slot++) {
            ItemStack stack = barrel.getIngredient().getStackInSlot(slot);
            if (isYeast(stack)) yeast += stack.getCount();
            else if (!stack.isEmpty()) inputs.add(stack);
        }
        if (yeast >= YEAST_PER_BARREL) return false;
        for (var recipe : level.getRecipeManager().getAllRecipesFor(ModRecipes.BARREL_RECIPE)) {
            if (!recipe.fluid().isSame(barrel.getFluid().getFluid().getFluid())) continue;
            boolean requiresYeast = false;
            List<Ingredient> ingredients = new ArrayList<>();
            for (Ingredient ingredient : recipe.ingredients()) {
                if (isYeastIngredient(ingredient)) requiresYeast = true;
                else if (!ingredient.isEmpty()) ingredients.add(ingredient);
            }
            if (requiresYeast && RecipeMatcher.findMatches(inputs, ingredients) != null) return true;
        }
        return false;
    }
}
