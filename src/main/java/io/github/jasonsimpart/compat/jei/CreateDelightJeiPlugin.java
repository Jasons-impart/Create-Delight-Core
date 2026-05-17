package io.github.jasonsimpart.compat.jei;

import io.github.jasonsimpart.CreateDelightCore;
import io.github.jasonsimpart.registry.ModItems;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
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

    @Override
    public ResourceLocation getPluginUid() {
        return ID;
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime runtime) {
        if (!shouldHideIntermediates()) {
            return;
        }

        List<ItemStack> stacks = intermediateStacks();
        runtime.getIngredientManager().removeIngredientsAtRuntime(VanillaTypes.ITEM_STACK, stacks);
        CreateDelightCore.LOGGER.debug("CDC hid {} intermediate items from JEI", stacks.size());
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
