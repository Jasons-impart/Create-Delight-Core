package io.github.jasonsimpart.mixin.kubejs;

import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Either;
import dev.latvian.mods.kubejs.recipe.RecipeScriptContext;
import dev.latvian.mods.kubejs.recipe.component.EitherRecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.SizedFluidIngredientComponent;
import io.github.jasonsimpart.compat.kubejs.KubeJsCreateRecipeJsonFixes;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = EitherRecipeComponent.class, remap = false)
public abstract class EitherRecipeComponentMixin {
    @Shadow
    public abstract RecipeComponent<?> left();

    @Inject(method = "wrap", at = @At("HEAD"), cancellable = true)
    private void createdelightcore$wrapCreateFluidTagAlias(RecipeScriptContext cx, Object from, CallbackInfoReturnable<Either<?, ?>> cir) {
        RecipeComponent<?> left = left();
        if (!(left instanceof SizedFluidIngredientComponent)) {
            return;
        }

        SizedFluidIngredient fluidTag = KubeJsCreateRecipeJsonFixes.tryCreateSizedFluidTagInput(cx, from);
        if (fluidTag != null) {
            cir.setReturnValue(Either.left(fluidTag));
            return;
        }

        JsonObject original = KubeJsCreateRecipeJsonFixes.objectOf(cx, from);
        if (original == null) {
            return;
        }

        JsonObject normalized = KubeJsCreateRecipeJsonFixes.normalizeCreateSizedFluidIngredientInput(original);
        if (normalized == original) {
            return;
        }

        cir.setReturnValue(Either.left(left.wrap(cx, normalized)));
    }
}
