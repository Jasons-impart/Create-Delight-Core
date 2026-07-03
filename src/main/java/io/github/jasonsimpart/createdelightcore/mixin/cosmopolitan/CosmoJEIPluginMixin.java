package io.github.jasonsimpart.createdelightcore.mixin.cosmopolitan;

import com.gumillea.cosmopolitan.core.util.jei.CosmoJEIPlugin;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeRegistration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

@Mixin(value = CosmoJEIPlugin.class, remap = false)
public class CosmoJEIPluginMixin {
    @Redirect(
            method = "registerRecipes",
            at = @At(
                    value = "INVOKE",
                    target = "Lmezz/jei/api/registration/IRecipeRegistration;addRecipes(Lmezz/jei/api/recipe/RecipeType;Ljava/util/List;)V",
                    ordinal = 0
            )
    )
    private void createdelightcore$hideHerbalCookieRecipes(IRecipeRegistration registration, RecipeType<?> recipeType, List<?> recipes) {
    }
}
