package io.github.jasonsimpart.mixin.kubejs;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import dev.latvian.mods.kubejs.recipe.RecipeScriptContext;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponentType;
import dev.latvian.mods.kubejs.recipe.component.SizedFluidIngredientComponent;
import dev.latvian.mods.rhino.type.TypeInfo;
import io.github.jasonsimpart.compat.kubejs.KubeJsCreateRecipeJsonFixes;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = SizedFluidIngredientComponent.class, remap = false)
public abstract class SizedFluidIngredientComponentMixin {
    @Shadow
    public abstract RecipeComponentType<?> type();

    @Shadow
    public abstract Codec<SizedFluidIngredient> codec();

    @Shadow
    public abstract TypeInfo typeInfo();

    public SizedFluidIngredient wrap(RecipeScriptContext cx, Object from) {
        SizedFluidIngredient fluidTag = KubeJsCreateRecipeJsonFixes.tryCreateSizedFluidTagInput(cx, from);
        if (fluidTag != null) {
            return fluidTag;
        }

        if (KubeJsCreateRecipeJsonFixes.CREATE_SIZED_FLUID_INGREDIENT.equals(type().toString())) {
            JsonObject json = KubeJsCreateRecipeJsonFixes.objectOf(cx, from);
            if (json != null) {
                JsonObject normalized = KubeJsCreateRecipeJsonFixes.normalizeCreateSizedFluidIngredientInput(json);
                if (normalized != json) {
                    return codec().parse(cx.recipe().type.event.ops.json(), normalized).getOrThrow();
                }
            }
        }

        return (SizedFluidIngredient) cx.cx().jsToJava(from, typeInfo());
    }
}
