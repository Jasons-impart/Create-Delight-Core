package io.github.jasonsimpart.compat.kubejs.disabled;

import com.google.gson.JsonElement;
import dev.latvian.mods.kubejs.core.RecipeManagerKJS;
import dev.latvian.mods.kubejs.event.EventGroupRegistry;
import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.recipe.RecipesKubeEvent;
import dev.latvian.mods.kubejs.script.ScriptType;
import io.github.jasonsimpart.disabled.DisabledContentManager;
import net.minecraft.resources.ResourceLocation;

import java.util.Iterator;
import java.util.Map;

public final class CreateDelightCoreKubePlugin implements KubeJSPlugin {
    @Override
    public void registerEvents(EventGroupRegistry registry) {
        registry.register(CreateDelightCoreKubeEvents.GROUP);
    }

    @Override
    public void beforeRecipeLoading(RecipesKubeEvent event, RecipeManagerKJS manager, Map<ResourceLocation, JsonElement> recipeJsons) {
        DisabledContentManager.clear();
        CreateDelightCoreKubeEvents.DISABLED_ITEMS.post(ScriptType.SERVER, new DisabledItemsKubeEvent());
        CreateDelightCoreKubeEvents.DISABLED_BLOCKS.post(ScriptType.SERVER, new DisabledBlocksKubeEvent());

        Iterator<Map.Entry<ResourceLocation, JsonElement>> iterator = recipeJsons.entrySet().iterator();
        while (iterator.hasNext()) {
            if (DisabledContentManager.shouldRemoveRecipe(iterator.next().getValue())) {
                iterator.remove();
            }
        }
    }
}
