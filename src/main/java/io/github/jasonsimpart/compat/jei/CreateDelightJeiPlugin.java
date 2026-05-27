package io.github.jasonsimpart.compat.jei;

import com.simibubi.create.AllBlocks;
import fr.iglee42.cmr.init.CMRRegistries;
import io.github.jasonsimpart.compat.jei.category.JeiCategoryBlazeBurnerFluid;
import io.github.jasonsimpart.compat.jei.category.JeiCategorySnowmanCoolerFluid;
import io.github.jasonsimpart.CreateDelightCore;
import io.github.jasonsimpart.network.ClientFuelCache;
import io.github.jasonsimpart.registry.ModItems;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@JeiPlugin
public final class CreateDelightJeiPlugin implements IModPlugin {
    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(CreateDelightCore.MODID, "jei_plugin");
    private static final String FORCE_SHOW_PROPERTY = "createdelightcore.showIntermediatesInJei";
    private static final String FORCE_HIDE_PROPERTY = "createdelightcore.hideIntermediatesInJei";
    private static IJeiRuntime jeiRuntime;
    private static List<JeiCategoryBlazeBurnerFluid.BlazeBurnerFluidRecipe> visibleBlazeRecipes = List.of();
    private static List<JeiCategorySnowmanCoolerFluid.SnowmanCoolerFluidRecipe> visibleCoolerRecipes = List.of();

    @Override
    public ResourceLocation getPluginUid() {
        return ID;
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime runtime) {
        jeiRuntime = runtime;
        ClientFuelCache.onUpdate = CreateDelightJeiPlugin::onFuelCacheUpdated;

        if (!shouldHideIntermediates()) {
            return;
        }

        List<ItemStack> stacks = intermediateStacks();
        runtime.getIngredientManager().removeIngredientsAtRuntime(VanillaTypes.ITEM_STACK, stacks);
        CreateDelightCore.LOGGER.debug("CDC hid {} intermediate items from JEI", stacks.size());
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new JeiCategoryBlazeBurnerFluid(registration.getJeiHelpers()));
        registration.addRecipeCategories(new JeiCategorySnowmanCoolerFluid(registration.getJeiHelpers()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        visibleBlazeRecipes = buildBlazeBurnerFluidRecipes();
        visibleCoolerRecipes = buildSnowmanCoolerFluidRecipes();
        registration.addRecipes(JeiCategoryBlazeBurnerFluid.RECIPE_TYPE, visibleBlazeRecipes);
        registration.addRecipes(JeiCategorySnowmanCoolerFluid.RECIPE_TYPE, visibleCoolerRecipes);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(AllBlocks.BLAZE_BURNER.asStack(), JeiCategoryBlazeBurnerFluid.RECIPE_TYPE);
        registration.addRecipeCatalyst(CMRRegistries.SNOWMAN_COOLER.asStack(), JeiCategorySnowmanCoolerFluid.RECIPE_TYPE);
    }

    private static void onFuelCacheUpdated() {
        if (jeiRuntime == null) {
            return;
        }

        jeiRuntime.getRecipeManager().hideRecipes(JeiCategoryBlazeBurnerFluid.RECIPE_TYPE, visibleBlazeRecipes);
        jeiRuntime.getRecipeManager().hideRecipes(JeiCategorySnowmanCoolerFluid.RECIPE_TYPE, visibleCoolerRecipes);
        visibleBlazeRecipes = buildBlazeBurnerFluidRecipes();
        visibleCoolerRecipes = buildSnowmanCoolerFluidRecipes();
        jeiRuntime.getRecipeManager().addRecipes(JeiCategoryBlazeBurnerFluid.RECIPE_TYPE, visibleBlazeRecipes);
        jeiRuntime.getRecipeManager().addRecipes(JeiCategorySnowmanCoolerFluid.RECIPE_TYPE, visibleCoolerRecipes);
    }

    private static List<JeiCategoryBlazeBurnerFluid.BlazeBurnerFluidRecipe> buildBlazeBurnerFluidRecipes() {
        List<JeiCategoryBlazeBurnerFluid.BlazeBurnerFluidRecipe> recipes = new ArrayList<>();
        ClientFuelCache.BURNER_MAP.forEach((fluid, fuel) -> recipes.add(new JeiCategoryBlazeBurnerFluid.BlazeBurnerFluidRecipe(
                fluid,
                fuel.strongHeat(),
                fuel.burnTime(),
                fuel.amountConsumed()
        )));
        return List.copyOf(recipes);
    }

    private static List<JeiCategorySnowmanCoolerFluid.SnowmanCoolerFluidRecipe> buildSnowmanCoolerFluidRecipes() {
        List<JeiCategorySnowmanCoolerFluid.SnowmanCoolerFluidRecipe> recipes = new ArrayList<>();
        ClientFuelCache.COOLER_MAP.forEach((fluid, fuel) -> recipes.add(new JeiCategorySnowmanCoolerFluid.SnowmanCoolerFluidRecipe(
                fluid,
                fuel.strongHeat(),
                fuel.burnTime(),
                fuel.amountConsumed()
        )));
        return List.copyOf(recipes);
    }

    private static boolean shouldHideIntermediates() {
        if (Boolean.getBoolean(FORCE_SHOW_PROPERTY)) {
            return false;
        }
        return FMLLoader.isProduction() || Boolean.getBoolean(FORCE_HIDE_PROPERTY);
    }

    private static List<ItemStack> intermediateStacks() {
        List<ItemStack> stacks = new ArrayList<>();
        addAll(stacks, ModItems.UNBAKED_MUFFIN_ITEMS);
        addAll(stacks, ModItems.POPSICLE_MOLD_FILLED_ITEMS);
        addAll(stacks, ModItems.POPSICLE_MOLD_SOLID_ITEMS);
        add(stacks, ModItems.POTATO_STEW_BEEF);
        add(stacks, ModItems.OIL_DOUGH_WITH_BUTTER);
        addAll(stacks, ModItems.AE_INTERMEDIATE_ITEMS);
        addAll(stacks, ModItems.CREATE_MACHINE_TRANSITIONAL_ITEMS);
        addAll(stacks, ModItems.AMMO_TRANSITIONAL_ITEMS);
        addAll(stacks, ModItems.BURGER_SANDWICH_TRANSITIONAL_ITEMS);
        addAll(stacks, ModItems.SUSHI_TRANSITIONAL_ITEMS);
        addAll(stacks, ModItems.GENERATED_UNFINISHED_ITEMS);
        return List.copyOf(stacks);
    }

    private static void addAll(List<ItemStack> stacks, Collection<? extends DeferredItem<? extends Item>> items) {
        items.forEach(item -> add(stacks, item));
    }

    private static void add(List<ItemStack> stacks, DeferredItem<? extends Item> item) {
        stacks.add(item.get().getDefaultInstance());
    }
}
