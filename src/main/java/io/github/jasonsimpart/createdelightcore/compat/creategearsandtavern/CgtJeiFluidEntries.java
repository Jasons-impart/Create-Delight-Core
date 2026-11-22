package io.github.jasonsimpart.createdelightcore.compat.creategearsandtavern;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;

public final class CgtJeiFluidEntries {
    private static final ResourceLocation HIDDEN_ID = new ResourceLocation("c", "hidden_from_recipe_viewers");
    private static final TagKey<Item> HIDDEN_ITEMS = TagKey.create(Registries.ITEM, HIDDEN_ID);
    private static final TagKey<Fluid> HIDDEN_FLUIDS = FluidTags.create(HIDDEN_ID);

    private CgtJeiFluidEntries() {
    }

    public static Object withoutBrewingVariant(Object ingredient) {
        if (!(ingredient instanceof FluidStack stack) || !stack.hasTag()) {
            return ingredient;
        }
        var id = ForgeRegistries.FLUIDS.getKey(stack.getFluid());
        if (id == null || !"creategearsandtavern".equals(id.getNamespace())) {
            return ingredient;
        }
        FluidStack copy = stack.copy();
        copy.getTag().remove("DrinkVariant");
        if (copy.getTag().isEmpty()) {
            copy.setTag(null);
        }
        return copy;
    }

    public static List<ItemStack> baseBottleSamples(List<ItemStack> variants) {
        Map<Item, ItemStack> entries = new LinkedHashMap<>();
        for (ItemStack stack : variants) {
            if (!stack.isEmpty() && !stack.is(HIDDEN_ITEMS)) {
                // These bottles only feed CGT's synthetic JEI recipes. Avoid
                // copying brewing levels, stored effects, or quality into them.
                entries.computeIfAbsent(stack.getItem(), ItemStack::new);
            }
        }
        return new ArrayList<>(entries.values());
    }

    public static FluidStack baseRecipeFluid(FluidStack stack) {
        return stack.isEmpty() ? FluidStack.EMPTY : new FluidStack(stack.getFluid(), stack.getAmount());
    }

    public static FluidStack[] baseRecipeOutputs(FluidStack[] variants) {
        FluidStack[] outputs = new FluidStack[variants.length];
        for (int i = 0; i < variants.length; i++) {
            outputs[i] = baseRecipeFluid(variants[i]);
        }
        return outputs;
    }

    public static Collection<FluidStack> baseEntries(Collection<FluidStack> variants) {
        Map<Fluid, FluidStack> entries = new LinkedHashMap<>();
        for (FluidStack stack : variants) {
            if (stack.isEmpty()) {
                continue;
            }
            Fluid fluid = stack.getFluid();
            Fluid source = fluid instanceof FlowingFluid flowing ? flowing.getSource() : fluid;
            if (fluid.is(HIDDEN_FLUIDS) || source.is(HIDDEN_FLUIDS)) continue;
            // These are JEI list representatives, never the actual recipe or
            // stored stacks. The ingredient list only needs the ordinary fluid.
            entries.computeIfAbsent(source, key -> new FluidStack(key, 1000));
        }
        return new ArrayList<>(entries.values());
    }
}
