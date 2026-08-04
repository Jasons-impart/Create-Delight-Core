package io.github.jasonsimpart.createdelightcore.content.configuration;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.jasonsimpart.createdelightcore.registry.CDRecipeTypes;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public class ConfigurationModuleRefillRecipe implements CraftingRecipe {
    private final ResourceLocation id;
    private final CraftingBookCategory category;
    private final Ingredient moduleIngredient;
    private final List<ConfigurationRequirement> refillRequirements;
    private final int refillCharge;

    public ConfigurationModuleRefillRecipe(ResourceLocation id, CraftingBookCategory category,
                                           Ingredient moduleIngredient,
                                           List<ConfigurationRequirement> refillRequirements,
                                           int refillCharge) {
        this.id = id;
        this.category = category;
        this.moduleIngredient = moduleIngredient;
        this.refillRequirements = List.copyOf(refillRequirements);
        this.refillCharge = refillCharge;
    }

    @Override
    public boolean matches(CraftingContainer container, Level level) {
        ItemStack module = ItemStack.EMPTY;
        List<ItemStack> refillStacks = new ArrayList<>();
        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            ItemStack stack = container.getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }
            if (module.isEmpty() && moduleIngredient.test(stack)
                    && stack.getItem() instanceof ConfigurationModuleItem) {
                module = stack;
            } else {
                refillStacks.add(stack);
            }
        }

        List<Ingredient> ingredients = expandedRefillIngredients();
        return !module.isEmpty() && refillStacks.size() == ingredients.size()
                && matchesIngredients(refillStacks, ingredients, 0, new boolean[ingredients.size()])
                && ConfigurationModuleManager.getCharge(module) + refillCharge
                <= ConfigurationModuleManager.getMaxCharge(module);
    }

    @Override
    public ItemStack assemble(CraftingContainer container, RegistryAccess registryAccess) {
        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            ItemStack stack = container.getItem(slot);
            if (!moduleIngredient.test(stack) || !(stack.getItem() instanceof ConfigurationModuleItem)) {
                continue;
            }
            ItemStack result = stack.copyWithCount(1);
            ConfigurationModuleManager.getDefinition(result)
                    .ifPresent(definition -> ConfigurationModuleManager.applyDefinitionSnapshot(result, definition));
            ConfigurationModuleManager.setCharge(result,
                    ConfigurationModuleManager.getCharge(result) + refillCharge);
            ConfigurationModuleManager.ensureSelectedMode(result);
            return result;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 1 + expandedRefillIngredients().size();
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        ItemStack[] modules = moduleIngredient.getItems();
        return modules.length == 0 ? ItemStack.EMPTY : modules[0].getItem().getDefaultInstance();
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        ingredients.add(moduleIngredient);
        ingredients.addAll(expandedRefillIngredients());
        return ingredients;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return CDRecipeTypes.CONFIGURATION_MODULE_REFILL.getSerializer();
    }

    @Override
    public CraftingBookCategory category() {
        return category;
    }

    public boolean supportsModule(ItemStack stack) {
        return stack.getItem() instanceof ConfigurationModuleItem && moduleIngredient.test(stack);
    }

    public List<ConfigurationRequirement> refillRequirements() {
        return refillRequirements;
    }

    public int refillCharge() {
        return refillCharge;
    }

    private List<Ingredient> expandedRefillIngredients() {
        List<Ingredient> result = new ArrayList<>();
        for (ConfigurationRequirement requirement : refillRequirements) {
            for (int count = 0; count < requirement.count(); count++) {
                result.add(requirement.ingredient());
            }
        }
        return result;
    }

    private static boolean matchesIngredients(List<ItemStack> stacks, List<Ingredient> ingredients,
                                              int stackIndex, boolean[] usedIngredients) {
        if (stackIndex >= stacks.size()) {
            return true;
        }
        ItemStack stack = stacks.get(stackIndex);
        for (int ingredientIndex = 0; ingredientIndex < ingredients.size(); ingredientIndex++) {
            if (usedIngredients[ingredientIndex] || !ingredients.get(ingredientIndex).test(stack)) {
                continue;
            }
            usedIngredients[ingredientIndex] = true;
            if (matchesIngredients(stacks, ingredients, stackIndex + 1, usedIngredients)) {
                return true;
            }
            usedIngredients[ingredientIndex] = false;
        }
        return false;
    }

    private static ConfigurationRequirement parseRequirement(JsonElement element) {
        if (!element.isJsonObject()) {
            return new ConfigurationRequirement(Ingredient.fromJson(element), 1);
        }
        JsonObject object = element.getAsJsonObject();
        int count = GsonHelper.getAsInt(object, "count", 1);
        JsonElement ingredient = object.has("ingredient") ? object.get("ingredient") : object;
        return new ConfigurationRequirement(Ingredient.fromJson(ingredient), count);
    }

    public static class Serializer implements RecipeSerializer<ConfigurationModuleRefillRecipe> {
        @Override
        public ConfigurationModuleRefillRecipe fromJson(ResourceLocation recipeId, JsonObject object) {
            CraftingBookCategory category = CraftingBookCategory.CODEC.byName(
                    GsonHelper.getAsString(object, "category", null), CraftingBookCategory.MISC);
            Ingredient module = Ingredient.fromJson(GsonHelper.getNonNull(object, "module"));
            List<ConfigurationRequirement> ingredients = new ArrayList<>();
            if (object.has("ingredients")) {
                JsonArray array = GsonHelper.getAsJsonArray(object, "ingredients");
                for (JsonElement element : array) {
                    ingredients.add(parseRequirement(element));
                }
            } else {
                ingredients.add(new ConfigurationRequirement(
                        Ingredient.fromJson(GsonHelper.getNonNull(object, "ingredient")), 1));
            }
            if (ingredients.isEmpty()) {
                throw new IllegalArgumentException("Configuration module refill must have at least one ingredient");
            }
            int charge = GsonHelper.getAsInt(object, "charge");
            if (charge < 1) {
                throw new IllegalArgumentException("Configuration module refill charge must be positive");
            }
            return new ConfigurationModuleRefillRecipe(recipeId, category, module, ingredients, charge);
        }

        @Override
        public ConfigurationModuleRefillRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            CraftingBookCategory category = buffer.readEnum(CraftingBookCategory.class);
            Ingredient module = Ingredient.fromNetwork(buffer);
            int ingredientCount = buffer.readVarInt();
            List<ConfigurationRequirement> ingredients = new ArrayList<>(ingredientCount);
            for (int index = 0; index < ingredientCount; index++) {
                ingredients.add(new ConfigurationRequirement(Ingredient.fromNetwork(buffer), buffer.readVarInt()));
            }
            int charge = buffer.readVarInt();
            return new ConfigurationModuleRefillRecipe(recipeId, category, module, ingredients, charge);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, ConfigurationModuleRefillRecipe recipe) {
            buffer.writeEnum(recipe.category);
            recipe.moduleIngredient.toNetwork(buffer);
            buffer.writeVarInt(recipe.refillRequirements.size());
            for (ConfigurationRequirement requirement : recipe.refillRequirements) {
                requirement.ingredient().toNetwork(buffer);
                buffer.writeVarInt(requirement.count());
            }
            buffer.writeVarInt(recipe.refillCharge);
        }
    }
}
