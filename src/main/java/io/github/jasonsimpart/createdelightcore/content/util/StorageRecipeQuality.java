package io.github.jasonsimpart.createdelightcore.content.util;

import de.cadentem.quality_food.config.ServerConfig;
import de.cadentem.quality_food.util.QualityUtils;
import de.cadentem.quality_food.util.StorageRecipeCache;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;

/** Shared storage-conversion policy; never mutate a recipe's result template. */
public final class StorageRecipeQuality {
    private StorageRecipeQuality() {}

    public static boolean isConversion(Recipe<?> recipe, Level level) {
        return ServerConfig.isRetainQualityRecipe(recipe, level.registryAccess())
                || StorageRecipeCache.isStorageRecipe(recipe, level);
    }

    public static boolean isExplicitlyExcluded(Recipe<?> recipe) {
        // isNoQualityRecipe also excludes storage recipes from random quality generation.
        // That exclusion must not prevent their deterministic quality inheritance.
        return recipe != null && ServerConfig.NO_QUALITY_RECIPES.get().contains(recipe.getId().toString());
    }

    public static ItemStack convert(ItemStack result, Container inputs, Recipe<?> recipe, Level level) {
        ItemStack output = result.copy();
        if (!output.isEmpty() && isConversion(recipe, level)) {
            // NONE must also remain NONE, even when an upstream hook already assigned a quality.
            output.removeTagKey(QualityUtils.QUALITY_TAG);
            QualityUtils.handleConversion(output, inputs, recipe, level);
        }
        return output;
    }
}
