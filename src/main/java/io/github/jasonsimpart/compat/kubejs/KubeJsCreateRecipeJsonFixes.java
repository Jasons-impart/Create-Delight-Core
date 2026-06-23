package io.github.jasonsimpart.compat.kubejs;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.latvian.mods.kubejs.recipe.RecipeScriptContext;
import dev.latvian.mods.kubejs.util.JsonUtils;
import io.github.jasonsimpart.Config;
import io.github.jasonsimpart.CreateDelightCore;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import org.jetbrains.annotations.Nullable;

public final class KubeJsCreateRecipeJsonFixes {
    public static final String CREATE_SIZED_FLUID_INGREDIENT = "create:sized_fluid_ingredient";
    private static final String CREATE_SEQUENCED_ASSEMBLY = "create:sequenced_assembly";
    private static final String CREATE_FILLING = "create:filling";
    private static final String NEOFORGE_SINGLE = "neoforge:single";
    private static final String NEOFORGE_TAG = "neoforge:tag";
    private static final String CREATE_FLUID_STACK = "fluid_stack";
    private static final String CREATE_FLUID_TAG = "fluid_tag";

    public static void normalize(ResourceLocation recipeId, JsonObject recipeJson) {
        if (!Config.ENABLE_KUBEJS_CREATE_SEQUENCED_FLUID_FIX.get()) {
            return;
        }
        if (!hasType(recipeJson, CREATE_SEQUENCED_ASSEMBLY)) {
            return;
        }

        JsonArray sequence = getArray(recipeJson, "sequence");
        if (sequence == null) {
            return;
        }

        int changed = 0;
        for (JsonElement stepElement : sequence) {
            if (!(stepElement instanceof JsonObject step) || !hasType(step, CREATE_FILLING)) {
                continue;
            }

            JsonArray ingredients = getArray(step, "ingredients");
            if (ingredients == null) {
                continue;
            }

            for (JsonElement ingredientElement : ingredients) {
                if (ingredientElement instanceof JsonObject ingredient && normalizeFluidIngredient(ingredient)) {
                    changed++;
                }
            }
        }

        if (changed > 0 && Config.DEBUG_KUBEJS_CREATE_SEQUENCED_FLUID_FIX.get()) {
            CreateDelightCore.LOGGER.info(
                    "[CDCore] Normalized {} KubeJS Create sequenced filling fluid ingredient(s) in {}",
                    changed,
                    recipeId
            );
        }
    }

    public static JsonObject normalizeCreateSizedFluidIngredientInput(JsonObject ingredient) {
        String type = getString(ingredient, "type");
        if (CREATE_FLUID_TAG.equals(type) && ingredient.has("fluid_tag") && ingredient.has("amount")) {
            JsonObject normalized = ingredient.deepCopy();
            normalized.addProperty("type", NEOFORGE_TAG);
            normalized.add("tag", normalized.get("fluid_tag").deepCopy());
            normalized.remove("fluid_tag");
            return normalized;
        }

        if (NEOFORGE_TAG.equals(type) && ingredient.has("fluid_tag") && !ingredient.has("tag")) {
            JsonObject normalized = ingredient.deepCopy();
            normalized.add("tag", normalized.get("fluid_tag").deepCopy());
            normalized.remove("fluid_tag");
            return normalized;
        }

        return ingredient;
    }

    @Nullable
    public static SizedFluidIngredient tryCreateSizedFluidTagInput(RecipeScriptContext cx, Object from) {
        JsonObject json = objectOf(cx, from);
        if (json == null) {
            return null;
        }

        String type = getString(json, "type");
        JsonElement tagElement = null;
        if (CREATE_FLUID_TAG.equals(type)) {
            tagElement = json.get("fluid_tag");
        } else if (NEOFORGE_TAG.equals(type)) {
            tagElement = json.get("tag");
            if (tagElement == null) {
                tagElement = json.get("fluid_tag");
            }
        }

        if (tagElement == null || !json.has("amount")) {
            return null;
        }

        String tagId = tagElement.getAsString();
        if (tagId.startsWith("#")) {
            tagId = tagId.substring(1);
        }

        TagKey<Fluid> tag = TagKey.create(Registries.FLUID, ResourceLocation.parse(tagId));
        return SizedFluidIngredient.of(tag, json.get("amount").getAsInt());
    }

    public static JsonObject normalizeCreateSizedFluidIngredientInput(RecipeScriptContext cx, Object from) {
        JsonObject json = objectOf(cx, from);
        return json == null ? null : normalizeCreateSizedFluidIngredientInput(json);
    }

    public static JsonObject objectOf(RecipeScriptContext cx, Object from) {
        if (from instanceof JsonObject json) {
            return json;
        }
        if (!cx.cx().isMapLike(from)) {
            return null;
        }
        return JsonUtils.objectOf(cx.cx(), from);
    }

    private static boolean normalizeFluidIngredient(JsonObject ingredient) {
        String type = getString(ingredient, "type");
        if (NEOFORGE_SINGLE.equals(type) && ingredient.has("fluid") && ingredient.has("amount")) {
            ingredient.addProperty("type", CREATE_FLUID_STACK);
            return true;
        }

        if (NEOFORGE_TAG.equals(type) && ingredient.has("amount")) {
            JsonElement tag = ingredient.get("tag");
            if (tag == null) {
                tag = ingredient.get("fluid_tag");
            }
            if (tag == null) {
                return false;
            }

            ingredient.addProperty("type", CREATE_FLUID_TAG);
            ingredient.add("fluid_tag", tag.deepCopy());
            ingredient.remove("tag");
            return true;
        }

        return false;
    }

    private static boolean hasType(JsonObject object, String expectedType) {
        return expectedType.equals(getString(object, "type"));
    }

    private static String getString(JsonObject object, String key) {
        JsonElement element = object.get(key);
        return element != null && element.isJsonPrimitive() ? element.getAsString() : "";
    }

    private static JsonArray getArray(JsonObject object, String key) {
        JsonElement element = object.get(key);
        return element instanceof JsonArray array ? array : null;
    }

    private KubeJsCreateRecipeJsonFixes() {
    }
}
