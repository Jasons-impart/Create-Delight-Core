package io.github.jasonsimpart.createdelightcore.mixin.create;

import com.simibubi.create.compat.jei.category.CreateRecipeCategory;
import io.github.jasonsimpart.createdelightcore.CDConfig;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@Mixin(value = CreateRecipeCategory.class, remap = false)
public abstract class CreateRecipeCategoryMixin<T extends Recipe<?>> {

    private static final ResourceLocation POLISHING_WITH_GRINDER =
            ResourceLocation.fromNamespaceAndPath("createmetallurgy", "polishing_with_grinder");

    @Shadow
    @Final
    protected RecipeType<T> type;

    @Shadow
    @Final
    private Supplier<List<T>> recipes;

    @Inject(method = "registerRecipes", at = @At("HEAD"), cancellable = true)
    private void createdelightcore$filterBlockedPolishingRecipesInJei(IRecipeRegistration registration, CallbackInfo ci) {
        if (!POLISHING_WITH_GRINDER.equals(type.getUid()))
            return;

        List<String> blocked = CDConfig.beltGrinderBlockedSandpaperRecipes;
        if (blocked == null || blocked.isEmpty())
            return;

        List<T> original = recipes.get();
        if (original == null || original.isEmpty())
            return;

        List<T> filtered = new ArrayList<>(original.size());
        for (T recipe : original) {
            if (!blocked.contains(recipe.getId().toString()))
                filtered.add(recipe);
        }

        if (filtered.size() != original.size()) {
            registration.addRecipes(type, filtered);
            ci.cancel();
        }
    }
}
