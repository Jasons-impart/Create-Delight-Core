package io.github.jasonsimpart.createdelightcore.mixin.createmetallurgy;

import fr.lucreeper74.createmetallurgy.content.blocks.belt_grinder.BeltGrinderBlockEntity;
import io.github.jasonsimpart.createdelightcore.CDConfig;
import net.minecraft.world.item.crafting.Recipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = BeltGrinderBlockEntity.class, remap = false)
public class BeltGrinderBlockEntityMixin {

    @Inject(method = "getRecipes", at = @At("RETURN"), cancellable = true)
    private void createdelightcore$filterBlockedSandpaperRecipes(CallbackInfoReturnable<List<? extends Recipe<?>>> cir) {
        List<String> blocked = CDConfig.beltGrinderBlockedSandpaperRecipes;
        if (blocked == null || blocked.isEmpty())
            return;

        List<? extends Recipe<?>> recipes = cir.getReturnValue();
        if (recipes == null || recipes.isEmpty())
            return;

        List<Recipe<?>> filtered = new ArrayList<>(recipes.size());
        boolean changed = false;

        for (Recipe<?> recipe : recipes) {
            String recipeId = recipe.getId().toString();
            if (blocked.contains(recipeId)) {
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
