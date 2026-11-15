package io.github.jasonsimpart.createdelightcore.compat.alcohol;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.github.jasonsimpart.createdelightcore.CreateDelightCore;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RecipesUpdatedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

/** Native JEED recipes also work when only the client has JEED installed. */
@Mod.EventBusSubscriber(modid = CreateDelightCore.MODID, value = Dist.CLIENT)
public final class AlcoholJeedClientRecipes {
    private AlcoholJeedClientRecipes() {}

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void recipesUpdated(RecipesUpdatedEvent event) {
        var serializer = ForgeRegistries.RECIPE_SERIALIZERS.getValue(id("jeed:effect_provider"));
        if (serializer == null || !ForgeRegistries.MOB_EFFECTS.containsKey(id("youkaishomecoming:drunk"))) return;
        var manager = event.getRecipeManager();
        List<Recipe<?>> recipes = new ArrayList<>(manager.getRecipes());
        boolean changed = false;
        var drunkRecipeId = id("createdelight:jeed/alcohol/drunk");
        if (manager.byKey(drunkRecipeId).isEmpty()) {
            recipes.add(provider(serializer, drunkRecipeId));
            changed = true;
        }
        for (var entry : AlcoholDrinks.tooltipEffectSources().entrySet()) {
            var recipeId = id("createdelight:jeed/alcohol/native/" + entry.getKey().replace(':', '/'));
            if (manager.byKey(recipeId).isPresent()) continue;
            JsonObject json = new JsonObject();
            json.addProperty("type", "jeed:effect_provider");
            json.addProperty("effect", entry.getKey());
            JsonArray providers = new JsonArray();
            entry.getValue().forEach(item -> providers.add(source(item)));
            json.add("providers", providers);
            recipes.add(serializer.fromJson(recipeId, json));
            changed = true;
        }
        if (changed) manager.replaceRecipes(recipes);
    }

    private static Recipe<?> provider(RecipeSerializer<?> serializer, ResourceLocation id) {
        JsonObject json = new JsonObject();
        json.addProperty("type", "jeed:effect_provider");
        json.addProperty("effect", "youkaishomecoming:drunk");
        JsonArray providers = new JsonArray();
        providers.add(source("#createdelight:order/wine"));
        // Startup profiles also cover standalone Core tests without the pack's order tags.
        for (String source : AlcoholDrinks.sources()) providers.add(source(source));
        JsonArray fluids = new JsonArray();
        for (String fluid : AlcoholDrinks.fluids()) {
            if (ForgeRegistries.FLUIDS.containsKey(id(fluid))) fluids.add(fluid);
        }
        json.add("fluid_providers", fluids);
        json.add("providers", providers);
        return serializer.fromJson(id, json);
    }

    private static JsonObject source(String value) {
        JsonObject json = new JsonObject();
        json.addProperty(value.startsWith("#") ? "tag" : "item", value.startsWith("#") ? value.substring(1) : value);
        return json;
    }

    private static ResourceLocation id(String id) { return ResourceLocation.parse(id); }
}
