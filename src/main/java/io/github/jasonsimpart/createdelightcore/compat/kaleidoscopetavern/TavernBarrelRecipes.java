package io.github.jasonsimpart.createdelightcore.compat.kaleidoscopetavern;

import com.github.ysbbbbbb.kaleidoscopetavern.crafting.container.BarrelRecipeContainer;
import com.github.ysbbbbbb.kaleidoscopetavern.crafting.recipe.BarrelRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** KubeJS declares exact amounts beside the recipe and opts in to fixed-batch brewing. */
public final class TavernBarrelRecipes {
    private static final Map<ResourceLocation, FixedBatch> FIXED_BATCHES = new ConcurrentHashMap<>();

    private TavernBarrelRecipes() {}

    public static void registerFixedBatch(String recipeId, int[] ingredientCounts) {
        if (ingredientCounts.length < 1 || ingredientCounts.length > 4
                || Arrays.stream(ingredientCounts).anyMatch(count -> count <= 0)) {
            throw new IllegalArgumentException("Invalid fixed barrel batch for " + recipeId);
        }
        var id = new ResourceLocation(recipeId);
        var batch = new FixedBatch(Arrays.stream(ingredientCounts).boxed().toList());
        var previous = FIXED_BATCHES.putIfAbsent(id, batch);
        if (previous != null && !previous.equals(batch)) {
            throw new IllegalStateException("Conflicting fixed barrel batch for " + recipeId + "; restart to change it");
        }
    }

    @Nullable
    public static FixedBatch fixedBatch(BarrelRecipe recipe) {
        return FIXED_BATCHES.get(recipe.id());
    }

    public record FixedBatch(List<Integer> ingredientCounts) {
        public boolean matches(BarrelRecipe recipe, BarrelRecipeContainer input) {
            var ingredients = recipe.ingredients().stream().filter(ingredient -> !ingredient.isEmpty()).toList();
            if (input.getFluid() != recipe.fluid() || ingredients.size() != ingredientCounts.size()) return false;
            var stacks = input.getItems().stream().filter(stack -> !stack.isEmpty()).toList();
            int[] remaining = ingredientCounts.stream().mapToInt(Integer::intValue).toArray();
            return matches(ingredients, stacks, remaining, 0);
        }

        // Allow an ingredient group to be split across physical slots without relying on their order.
        private static boolean matches(List<Ingredient> ingredients, List<ItemStack> stacks, int[] remaining, int slot) {
            if (slot == stacks.size()) return Arrays.stream(remaining).allMatch(count -> count == 0);

            ItemStack stack = stacks.get(slot);
            int count = stack.getCount();
            for (int index = 0; index < remaining.length; index++) {
                if (remaining[index] < count || !ingredients.get(index).test(stack)) continue;
                remaining[index] -= count;
                if (matches(ingredients, stacks, remaining, slot + 1)) return true;
                remaining[index] += count;
            }
            return false;
        }
    }
}
