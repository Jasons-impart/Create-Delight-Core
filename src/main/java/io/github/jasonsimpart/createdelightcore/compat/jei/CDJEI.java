package io.github.jasonsimpart.createdelightcore.compat.jei;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.simibubi.create.AllItems;
import com.simibubi.create.Create;
import com.simibubi.create.compat.jei.*;
import com.simibubi.create.compat.jei.category.CreateRecipeCategory;
import com.simibubi.create.foundation.recipe.IRecipeTypeInfo;
import com.simibubi.create.infrastructure.config.AllConfigs;
import com.simibubi.create.infrastructure.config.CRecipes;

import com.simibubi.create.AllBlocks;
import fr.iglee42.cmr.init.CMRRegistries;
import io.github.jasonsimpart.createdelightcore.CreateDelightCore;
import io.github.jasonsimpart.createdelightcore.compat.jei.category.CDProcessingViaFanCategory;
import io.github.jasonsimpart.createdelightcore.compat.jei.category.FanFreezingCategory;
import io.github.jasonsimpart.createdelightcore.compat.jei.category.JeiCategoryBlazeBurnerFluid;
import io.github.jasonsimpart.createdelightcore.compat.jei.category.JeiCategorySnowmanCoolerFluid;
import io.github.jasonsimpart.createdelightcore.content.recipe.FanFreezingRecipe;
import io.github.jasonsimpart.createdelightcore.network.ClientFuelCache;
import io.github.jasonsimpart.createdelightcore.registry.CDRecipeTypes;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IJeiRuntime;
import net.createmod.catnip.config.ConfigBase;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;

@JeiPlugin
@SuppressWarnings("unused")
@ParametersAreNonnullByDefault
public class CDJEI implements IModPlugin {
    private static final ResourceLocation ID = CreateDelightCore.id("jei_plugin");
    public static IJeiRuntime jeiRuntime = null;

    private final List<CreateRecipeCategory<?>> allCategories = new ArrayList<>();
    private IIngredientManager ingredientManager;

    private void loadCategories() {
        allCategories.clear();

        CreateRecipeCategory<?> fan_freezing = builder(FanFreezingRecipe.class)
                .addTypedRecipes(CDRecipeTypes.FAN_FREEZING)
                .catalystStack(CDProcessingViaFanCategory.getFan("fan_freezing"))
                .doubleItemIcon(AllItems.PROPELLER.get(), Items.POWDER_SNOW_BUCKET)
                .emptyBackground(178, 72)
                .build("fan_freezing", FanFreezingCategory::new);
    }

    private <T extends Recipe<?>> CategoryBuilder<T> builder(Class<? extends T> recipeClass) {
        return new CategoryBuilder<>(recipeClass);
    }

    @Override
    @Nonnull
    public ResourceLocation getPluginUid() {
        return ID;
    }

    @Override
    public void onRuntimeAvailable(@Nonnull IJeiRuntime runtime) {
        jeiRuntime = runtime;
        // Register callback so SyncFuelMapsPacket can trigger a JEI update
        ClientFuelCache.onUpdate = CDJEI::onFuelCacheUpdated;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        loadCategories();
        registration.addRecipeCategories(allCategories.toArray(IRecipeCategory[]::new));
        registration.addRecipeCategories(new JeiCategoryBlazeBurnerFluid(registration.getJeiHelpers()));
        registration.addRecipeCategories(new JeiCategorySnowmanCoolerFluid(registration.getJeiHelpers()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        ingredientManager = registration.getIngredientManager();

        allCategories.forEach(c -> c.registerRecipes(registration));

        registration.addRecipes(RecipeTypes.CRAFTING, ToolboxColoringRecipeMaker.createRecipes().toList());
        registration.addRecipes(JeiCategoryBlazeBurnerFluid.RECIPE_TYPE, buildFluidRecipeList());
        registration.addRecipes(JeiCategorySnowmanCoolerFluid.RECIPE_TYPE, buildCoolerFluidRecipeList());
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        allCategories.forEach(c -> c.registerCatalysts(registration));
        registration.addRecipeCatalyst(AllBlocks.BLAZE_BURNER.asStack(), JeiCategoryBlazeBurnerFluid.RECIPE_TYPE);
        registration.addRecipeCatalyst(CMRRegistries.SNOWMAN_COOLER.asStack(), JeiCategorySnowmanCoolerFluid.RECIPE_TYPE);
    }

    /** Build recipes from the client-side fuel cache (populated via network from server). */
    public static List<JeiCategoryBlazeBurnerFluid.BlazeBurnerFluidRecipe> buildFluidRecipeList() {
        List<JeiCategoryBlazeBurnerFluid.BlazeBurnerFluidRecipe> recipes = new ArrayList<>();
        ClientFuelCache.BURNER_MAP.forEach((fluid, triplet) -> {
            Integer burnTime = triplet.getFirst();
            Boolean isSuperHeat = triplet.getSecond();
            Integer amountConsume = triplet.getThird();
            if (burnTime != null && isSuperHeat != null && amountConsume != null) {
                recipes.add(new JeiCategoryBlazeBurnerFluid.BlazeBurnerFluidRecipe(fluid, isSuperHeat, burnTime, amountConsume));
            }
        });
        return recipes;
    }

    /**
     * Called by SyncFuelMapsPacket after the client fuel cache is populated.
     * Adds any new fuel recipes to JEI if the runtime is already available
     * (covers the case where the server packet arrives after JEI has initialized).
     */
    public static void onFuelCacheUpdated() {
        if (jeiRuntime != null) {
            jeiRuntime.getRecipeManager().addRecipes(JeiCategoryBlazeBurnerFluid.RECIPE_TYPE, buildFluidRecipeList());
            jeiRuntime.getRecipeManager().addRecipes(JeiCategorySnowmanCoolerFluid.RECIPE_TYPE, buildCoolerFluidRecipeList());
        }
    }

    /** Build recipes from the client-side cooler cache (populated via network from server). */
    public static List<JeiCategorySnowmanCoolerFluid.SnowmanCoolerFluidRecipe> buildCoolerFluidRecipeList() {
        List<JeiCategorySnowmanCoolerFluid.SnowmanCoolerFluidRecipe> recipes = new ArrayList<>();
        ClientFuelCache.COOLER_MAP.forEach((fluid, triplet) -> {
            Integer coolTime = triplet.getFirst();
            Boolean isFreezing = triplet.getSecond();
            Integer amountConsume = triplet.getThird();
            if (coolTime != null && isFreezing != null && amountConsume != null) {
                recipes.add(new JeiCategorySnowmanCoolerFluid.SnowmanCoolerFluidRecipe(fluid, isFreezing, coolTime, amountConsume));
            }
        });
        return recipes;
    }

    private class CategoryBuilder<T extends Recipe<?>> {
        private final Class<? extends T> recipeClass;
        private Predicate<CRecipes> predicate = cRecipes -> true;

        private IDrawable background;
        private IDrawable icon;

        private final List<Consumer<List<T>>> recipeListConsumers = new ArrayList<>();
        private final List<Supplier<? extends ItemStack>> catalysts = new ArrayList<>();

        public CategoryBuilder(Class<? extends T> recipeClass) {
            this.recipeClass = recipeClass;
        }

        public CategoryBuilder<T> enableIf(Predicate<CRecipes> predicate) {
            this.predicate = predicate;
            return this;
        }

        public CategoryBuilder<T> enableWhen(Function<CRecipes, ConfigBase.ConfigBool> configValue) {
            predicate = c -> configValue.apply(c).get();
            return this;
        }

        public CategoryBuilder<T> addRecipeListConsumer(Consumer<List<T>> consumer) {
            recipeListConsumers.add(consumer);
            return this;
        }

        public CategoryBuilder<T> addRecipes(Supplier<Collection<? extends T>> collection) {
            return addRecipeListConsumer(recipes -> recipes.addAll(collection.get()));
        }

        public CategoryBuilder<T> addAllRecipesIf(Predicate<Recipe<?>> pred) {
            return addRecipeListConsumer(recipes -> CreateJEI.consumeAllRecipes(recipe -> {
                if (pred.test(recipe)) {
                    recipes.add((T) recipe);
                }
            }));
        }

        public CategoryBuilder<T> addAllRecipesIf(Predicate<Recipe<?>> pred, Function<Recipe<?>, T> converter) {
            return addRecipeListConsumer(recipes -> CreateJEI.consumeAllRecipes(recipe -> {
                if (pred.test(recipe)) {
                    recipes.add(converter.apply(recipe));
                }
            }));
        }

        public CategoryBuilder<T> addTypedRecipes(IRecipeTypeInfo recipeTypeEntry) {
            return addTypedRecipes(recipeTypeEntry::getType);
        }

        public CategoryBuilder<T> addTypedRecipes(Supplier<RecipeType<? extends T>> recipeType) {
            return addRecipeListConsumer(recipes -> CreateJEI.<T>consumeTypedRecipes(recipes::add, recipeType.get()));
        }

        public CategoryBuilder<T> addTypedRecipes(Supplier<RecipeType<? extends T>> recipeType, Function<Recipe<?>, T> converter) {
            return addRecipeListConsumer(recipes -> CreateJEI.<T>consumeTypedRecipes(recipe -> recipes.add(converter.apply(recipe)), recipeType.get()));
        }

        public CategoryBuilder<T> addTypedRecipesIf(Supplier<RecipeType<? extends T>> recipeType, Predicate<Recipe<?>> pred) {
            return addRecipeListConsumer(recipes -> CreateJEI.<T>consumeTypedRecipes(recipe -> {
                if (pred.test(recipe)) {
                    recipes.add(recipe);
                }
            }, recipeType.get()));
        }

        public CategoryBuilder<T> addTypedRecipesExcluding(Supplier<RecipeType<? extends T>> recipeType,
                                                           Supplier<RecipeType<? extends T>> excluded) {
            return addRecipeListConsumer(recipes -> {
                List<Recipe<?>> excludedRecipes = CreateJEI.getTypedRecipes(excluded.get());
                CreateJEI.<T>consumeTypedRecipes(recipe -> {
                    for (Recipe<?> excludedRecipe : excludedRecipes) {
                        if (CreateJEI.doInputsMatch(recipe, excludedRecipe)) {
                            return;
                        }
                    }
                    recipes.add(recipe);
                }, recipeType.get());
            });
        }

        public CategoryBuilder<T> removeRecipes(Supplier<RecipeType<? extends T>> recipeType) {
            return addRecipeListConsumer(recipes -> {
                List<Recipe<?>> excludedRecipes = CreateJEI.getTypedRecipes(recipeType.get());
                recipes.removeIf(recipe -> {
                    for (Recipe<?> excludedRecipe : excludedRecipes)
                        if (CreateJEI.doInputsMatch(recipe, excludedRecipe) && CreateJEI.doOutputsMatch(recipe, excludedRecipe))
                            return true;
                    return false;
                });
            });
        }

        public CategoryBuilder<T> removeNonAutomation() {
            return addRecipeListConsumer(recipes -> recipes.removeIf(recipe -> recipe.getId().getPath().endsWith("_manual_only")));
        }

        public CategoryBuilder<T> catalystStack(Supplier<ItemStack> supplier) {
            catalysts.add(supplier);
            return this;
        }

        public CategoryBuilder<T> catalyst(Supplier<ItemLike> supplier) {
            return catalystStack(() -> new ItemStack(supplier.get()
                    .asItem()));
        }

        public CategoryBuilder<T> icon(IDrawable icon) {
            this.icon = icon;
            return this;
        }

        public CategoryBuilder<T> itemIcon(ItemLike item) {
            icon(new ItemIcon(() -> new ItemStack(item)));
            return this;
        }

        public CategoryBuilder<T> doubleItemIcon(ItemLike item1, ItemLike item2) {
            icon(new DoubleItemIcon(() -> new ItemStack(item1), () -> new ItemStack(item2)));
            return this;
        }

        public CategoryBuilder<T> background(IDrawable background) {
            this.background = background;
            return this;
        }

        public CategoryBuilder<T> emptyBackground(int width, int height) {
            background(new EmptyBackground(width, height));
            return this;
        }

        public CreateRecipeCategory<T> build(String name, CreateRecipeCategory.Factory<T> factory) {
            Supplier<List<T>> recipesSupplier;
            if (predicate.test(AllConfigs.server().recipes)) {
                recipesSupplier = () -> {
                    List<T> recipes = new ArrayList<>();
                    for (Consumer<List<T>> consumer : recipeListConsumers)
                        consumer.accept(recipes);
                    return recipes;
                };
            } else {
                recipesSupplier = () -> Collections.emptyList();
            }

            CreateRecipeCategory.Info<T> info = new CreateRecipeCategory.Info<>(
                    new mezz.jei.api.recipe.RecipeType<>(Create.asResource(name), recipeClass),
                    Component.translatable(CreateDelightCore.MODID + ".recipe." + name), background, icon, recipesSupplier, catalysts);
            CreateRecipeCategory<T> category = factory.create(info);
            allCategories.add(category);
            return category;
        }
    }
}
