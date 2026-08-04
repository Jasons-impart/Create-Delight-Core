package io.github.jasonsimpart.createdelightcore.content.configuration;

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

public class ConfigurationModuleRefillRecipe implements CraftingRecipe {
    private final ResourceLocation id;
    private final CraftingBookCategory category;
    private final Ingredient moduleIngredient;
    private final Ingredient refillIngredient;
    private final int refillCharge;

    public ConfigurationModuleRefillRecipe(ResourceLocation id, CraftingBookCategory category,
                                           Ingredient moduleIngredient, Ingredient refillIngredient,
                                           int refillCharge) {
        this.id = id;
        this.category = category;
        this.moduleIngredient = moduleIngredient;
        this.refillIngredient = refillIngredient;
        this.refillCharge = refillCharge;
    }

    @Override
    public boolean matches(CraftingContainer container, Level level) {
        ItemStack module = ItemStack.EMPTY;
        ItemStack refill = ItemStack.EMPTY;
        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            ItemStack stack = container.getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }
            if (module.isEmpty() && moduleIngredient.test(stack)
                    && stack.getItem() instanceof ConfigurationModuleItem) {
                module = stack;
            } else if (refill.isEmpty() && refillIngredient.test(stack)) {
                refill = stack;
            } else {
                return false;
            }
        }
        return !module.isEmpty() && !refill.isEmpty()
                && ConfigurationModuleManager.getCharge(module) < ConfigurationModuleManager.getMaxCharge(module);
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
        return width * height >= 2;
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
        ingredients.add(refillIngredient);
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

    public static class Serializer implements RecipeSerializer<ConfigurationModuleRefillRecipe> {
        @Override
        public ConfigurationModuleRefillRecipe fromJson(ResourceLocation recipeId, JsonObject object) {
            CraftingBookCategory category = CraftingBookCategory.CODEC.byName(
                    GsonHelper.getAsString(object, "category", null), CraftingBookCategory.MISC);
            Ingredient module = Ingredient.fromJson(GsonHelper.getNonNull(object, "module"));
            Ingredient ingredient = Ingredient.fromJson(GsonHelper.getNonNull(object, "ingredient"));
            int charge = GsonHelper.getAsInt(object, "charge");
            if (charge < 1) {
                throw new IllegalArgumentException("Configuration module refill charge must be positive");
            }
            return new ConfigurationModuleRefillRecipe(recipeId, category, module, ingredient, charge);
        }

        @Override
        public ConfigurationModuleRefillRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            CraftingBookCategory category = buffer.readEnum(CraftingBookCategory.class);
            Ingredient module = Ingredient.fromNetwork(buffer);
            Ingredient ingredient = Ingredient.fromNetwork(buffer);
            int charge = buffer.readVarInt();
            return new ConfigurationModuleRefillRecipe(recipeId, category, module, ingredient, charge);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, ConfigurationModuleRefillRecipe recipe) {
            buffer.writeEnum(recipe.category);
            recipe.moduleIngredient.toNetwork(buffer);
            recipe.refillIngredient.toNetwork(buffer);
            buffer.writeVarInt(recipe.refillCharge);
        }
    }
}
