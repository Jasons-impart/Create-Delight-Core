package io.github.jasonsimpart.mixin.kubejs;

import dev.latvian.mods.kubejs.recipe.KubeRecipe;
import dev.latvian.mods.kubejs.recipe.RecipesKubeEvent;
import dev.latvian.mods.kubejs.recipe.filter.IDFilter;
import dev.latvian.mods.kubejs.recipe.filter.RecipeFilter;
import dev.latvian.mods.kubejs.script.SourceLine;
import dev.latvian.mods.rhino.Context;
import io.github.jasonsimpart.Config;
import io.github.jasonsimpart.CreateDelightCore;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(value = RecipesKubeEvent.class, remap = false)
public abstract class RecipesKubeEventMixin {
    @Shadow
    public Map<ResourceLocation, KubeRecipe> originalRecipes;

    @Inject(method = "remove", at = @At("HEAD"))
    private void createdelightcore$checkMissingExactRecipeId(Context cx, RecipeFilter filter, CallbackInfo ci) {
        if (!(filter instanceof IDFilter idFilter)) {
            return;
        }

        Config.RecipeRemoveMissingIdMode mode = Config.RECIPE_REMOVE_MISSING_ID_MODE.get();
        if (mode == Config.RecipeRemoveMissingIdMode.OFF) {
            return;
        }

        ResourceLocation id = idFilter.id;
        KubeRecipe recipe = originalRecipes.get(id);
        if (recipe != null && !recipe.removed) {
            return;
        }

        String message = createdelightcore$missingRecipeMessage(id, SourceLine.of(cx));
        if (mode == Config.RecipeRemoveMissingIdMode.WARN) {
            CreateDelightCore.LOGGER.warn(message);
            return;
        }

        throw new IllegalStateException(message);
    }

    private static String createdelightcore$missingRecipeMessage(ResourceLocation id, SourceLine sourceLine) {
        String source = sourceLine.isUnknown() ? "<unknown KubeJS source>" : sourceLine.toString();
        return "KubeJS tried to remove missing recipe id '" + id + "' at " + source + ". "
                + "Only exact id removals are checked by createdelightcore. "
                + "If this is an optional recipe removal, check whether the mod/recipe exists first, "
                + "or set recipeRemoveMissingIdMode to WARN/OFF, or use a non-strict optional removal tool.";
    }
}
