package io.github.jasonsimpart.mixin.createmetallurgy;

import fr.lucreeper74.createmetallurgy.content.blocks.belt_grinder.BeltGrinderBlockEntity;
import io.github.jasonsimpart.Config;
import io.github.jasonsimpart.CreateDelightCore;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = BeltGrinderBlockEntity.class, remap = false)
public class BeltGrinderBlockEntityMixin {
    @Inject(method = "getRecipes", at = @At("RETURN"), cancellable = true)
    private void createdelightcore$filterBlockedSandpaperRecipes(CallbackInfoReturnable<List<RecipeHolder<? extends Recipe<?>>>> cir) {
        List<? extends String> blocked = Config.BELT_GRINDER_BLOCKED_SANDPAPER_RECIPES.get();
        if (blocked == null || blocked.isEmpty()) {
            return;
        }

        List<RecipeHolder<? extends Recipe<?>>> recipes = cir.getReturnValue();
        if (recipes == null || recipes.isEmpty()) {
            return;
        }

        List<RecipeHolder<? extends Recipe<?>>> filtered = new ArrayList<>(recipes.size());
        boolean changed = false;

        for (RecipeHolder<? extends Recipe<?>> recipe : recipes) {
            String recipeId = recipe.id().toString();
            if (blocked.contains(recipeId)) {
                CreateDelightCore.LOGGER.info("[CDCore] BeltGrinder: blocking recipe {}", recipeId);
                changed = true;
                continue;
            }
            filtered.add(recipe);
        }

        if (changed) {
            cir.setReturnValue(filtered);
            cir.cancel();
        }
    }
}
