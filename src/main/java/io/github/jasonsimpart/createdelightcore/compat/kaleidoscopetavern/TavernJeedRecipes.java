package io.github.jasonsimpart.createdelightcore.compat.kaleidoscopetavern;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** KubeJS registers drink sources at startup, including on clients of servers without JEED. */
public final class TavernJeedRecipes {
    private static final ResourceLocation SIGNATURE_COCKTAIL = ResourceLocation.parse("kaleidoscope_tavern:signature_cocktail");
    private static final Map<ResourceLocation, Set<ResourceLocation>> DRINKS_BY_EFFECT = new LinkedHashMap<>();
    private static final Set<ResourceLocation> BASE_WINE_EFFECTS = new LinkedHashSet<>();

    private TavernJeedRecipes() {}

    public static void registerDrinkEffect(String itemId, String effectId, boolean signatureBase) {
        var item = ResourceLocation.parse(itemId);
        var effect = ResourceLocation.parse(effectId);
        if (!ForgeRegistries.ITEMS.containsKey(item) || !ForgeRegistries.MOB_EFFECTS.containsKey(effect)) return;
        DRINKS_BY_EFFECT.computeIfAbsent(effect, key -> new LinkedHashSet<>()).add(item);
        if (signatureBase) BASE_WINE_EFFECTS.add(effect);
    }

    public static void addRecipes(RecipeSerializer<?> serializer, RecipeManager manager, List<Recipe<?>> recipes) {
        if (DRINKS_BY_EFFECT.isEmpty()) return;

        Map<ResourceLocation, Set<ResourceLocation>> providers = new LinkedHashMap<>();
        DRINKS_BY_EFFECT.forEach((effect, items) -> providers.put(effect, new LinkedHashSet<>(items)));
        Set<ResourceLocation> signatureEffects = new LinkedHashSet<>(BASE_WINE_EFFECTS);
        for (var potion : ForgeRegistries.POTIONS.getValues()) {
            for (var instance : potion.getEffects()) {
                var effect = ForgeRegistries.MOB_EFFECTS.getKey(instance.getEffect());
                if (effect != null) signatureEffects.add(effect);
            }
        }
        for (var effect : signatureEffects) {
            providers.computeIfAbsent(effect, key -> new LinkedHashSet<>()).add(SIGNATURE_COCKTAIL);
        }
        providers.forEach((effect, items) -> addRecipe(serializer, manager, recipes,
                effect.getNamespace() + "/" + effect.getPath(), effect, items, List.of()));

        addRecipe(serializer, manager, recipes, "secondary/glowing", ResourceLocation.parse("minecraft:glowing"),
                List.of(), List.of("kaleidoscope_tavern:vision"));
        addRecipe(serializer, manager, recipes, "secondary/fire_resistance", ResourceLocation.parse("minecraft:fire_resistance"),
                List.of(), List.of("kaleidoscope_tavern:ardent_heat", "kaleidoscope_tavern:zenith"));
    }

    private static void addRecipe(RecipeSerializer<?> serializer, RecipeManager manager, List<Recipe<?>> recipes,
                                  String path, ResourceLocation effect, Collection<ResourceLocation> items,
                                  List<String> sourceEffects) {
        var id = ResourceLocation.fromNamespaceAndPath("createdelight", "jeed/kaleidoscope_tavern/" + path);
        if (manager.byKey(id).isPresent()) return;

        JsonObject json = new JsonObject();
        json.addProperty("type", "jeed:effect_provider");
        json.addProperty("effect", effect.toString());
        JsonArray providers = new JsonArray();
        for (var item : items) {
            JsonObject source = new JsonObject();
            source.addProperty("item", item.toString());
            providers.add(source);
        }
        json.add("providers", providers);
        if (!sourceEffects.isEmpty()) {
            JsonArray effects = new JsonArray();
            sourceEffects.forEach(effects::add);
            json.add("effect_providers", effects);
        }
        recipes.add(serializer.fromJson(id, json));
    }
}
