package io.github.jasonsimpart.createdelightcore.mixin.createmetallurgy;

import com.mojang.logging.LogUtils;
import fr.lucreeper74.createmetallurgy.content.blocks.belt_grinder.BeltGrinderBlockEntity;
import io.github.jasonsimpart.createdelightcore.CDConfig;
import net.minecraft.world.item.crafting.Recipe;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = BeltGrinderBlockEntity.class, remap = false)
public class BeltGrinderBlockEntityMixin {

    private static final Logger LOGGER = LogUtils.getLogger();

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
                LOGGER.info("[CDCore] BeltGrinder: blocking recipe {}", recipeId);
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
