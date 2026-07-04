package io.github.jasonsimpart.createdelightcore.content.recipe;

import com.google.gson.JsonObject;
import com.gumillea.cosmopolitan.core.misc.BerrfectFlavorHelper;
import com.gumillea.cosmopolitan.core.reg.CosmoFluids;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.content.processing.basin.BasinRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder;
import io.github.jasonsimpart.createdelightcore.registry.CDFluids;
import io.github.jasonsimpart.createdelightcore.registry.CDRecipeTypes;
import io.github.jasonsimpart.createdelightcore.registry.CDTags;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class BerrySyrupFluidMixingRecipe extends BasinRecipe {
    public static final int BERRY_COUNT = 4;
    public static final int SYRUP_AMOUNT = 250;
    private static final Ingredient BERRIES = Ingredient.of(CDTags.forgeItemTag("berries"));
    private static final ThreadLocal<Deque<BasinBlockEntity>> APPLYING_BASINS =
            ThreadLocal.withInitial(ArrayDeque::new);

    public BerrySyrupFluidMixingRecipe(ProcessingRecipeBuilder.ProcessingRecipeParams params) {
        super(CDRecipeTypes.BERRY_SYRUP_FLUID_MIXING, params);
    }

    public static void pushApplyingBasin(BasinBlockEntity basin) {
        APPLYING_BASINS.get().push(basin);
    }

    public static void popApplyingBasin(BasinBlockEntity basin) {
        Deque<BasinBlockEntity> basins = APPLYING_BASINS.get();
        if (!basins.isEmpty() && basins.peek() == basin) {
            basins.pop();
        } else {
            basins.remove(basin);
        }
        if (basins.isEmpty()) {
            APPLYING_BASINS.remove();
        }
    }

    @Override
    public NonNullList<FluidStack> getFluidResults() {
        List<ItemStack> berries = getApplyingBerries();
        if (berries.size() == BERRY_COUNT) {
            return fluidResults(fluidForFlavor(BerrfectFlavorHelper.getFlavorFromIngredients(berries)));
        }
        return representativeFluidResults();
    }

    public NonNullList<FluidStack> getJeiFluidResults() {
        return previewFluidResults();
    }

    private static List<ItemStack> getApplyingBerries() {
        BasinBlockEntity basin = currentBasin();
        if (basin == null) {
            return List.of();
        }

        IItemHandler items = basin.getCapability(ForgeCapabilities.ITEM_HANDLER).orElse(null);
        if (items == null) {
            return List.of();
        }

        List<ItemStack> berries = new ArrayList<>(BERRY_COUNT);
        int[] extractedItemsFromSlot = new int[items.getSlots()];

        Ingredients:
        for (int ingredient = 0; ingredient < BERRY_COUNT; ingredient++) {
            for (int slot = 0; slot < items.getSlots(); slot++) {
                ItemStack stackInSlot = items.getStackInSlot(slot);
                if (stackInSlot.getCount() <= extractedItemsFromSlot[slot]) {
                    continue;
                }
                ItemStack extracted = items.extractItem(slot, 1, true);
                if (!BERRIES.test(extracted)) {
                    continue;
                }
                ItemStack berry = extracted.copy();
                berry.setCount(1);
                berries.add(berry);
                extractedItemsFromSlot[slot]++;
                continue Ingredients;
            }
            return List.of();
        }

        return berries;
    }

    @Nullable
    private static BasinBlockEntity currentBasin() {
        Deque<BasinBlockEntity> basins = APPLYING_BASINS.get();
        return basins.isEmpty() ? null : basins.peek();
    }

    private static NonNullList<FluidStack> previewFluidResults() {
        NonNullList<FluidStack> results = NonNullList.create();
        results.add(syrup(CosmoFluids.SWEET_BERRY_SYRUP.get()));
        results.add(syrup(CosmoFluids.SOUR_BERRY_SYRUP.get()));
        results.add(syrup(CosmoFluids.BITTER_BERRY_SYRUP.get()));
        results.add(syrup(CosmoFluids.SPICY_BERRY_SYRUP.get()));
        results.add(syrup(CosmoFluids.STRANGE_BERRY_SYRUP.get()));
        return results;
    }

    private static NonNullList<FluidStack> representativeFluidResults() {
        return fluidResults(syrup(CosmoFluids.SWEET_BERRY_SYRUP.get()));
    }

    private static NonNullList<FluidStack> fluidResults(FluidStack stack) {
        NonNullList<FluidStack> results = NonNullList.create();
        results.add(stack);
        return results;
    }

    private static FluidStack fluidForFlavor(String flavor) {
        return switch (flavor) {
            case "sour" -> syrup(CosmoFluids.SOUR_BERRY_SYRUP.get());
            case "bitter" -> syrup(CosmoFluids.BITTER_BERRY_SYRUP.get());
            case "spicy" -> syrup(CosmoFluids.SPICY_BERRY_SYRUP.get());
            case "strange" -> syrup(CosmoFluids.STRANGE_BERRY_SYRUP.get());
            default -> syrup(CosmoFluids.SWEET_BERRY_SYRUP.get());
        };
    }

    private static FluidStack syrup(Fluid fluid) {
        return new FluidStack(fluid, SYRUP_AMOUNT);
    }

    private static BerrySyrupFluidMixingRecipe create(ResourceLocation id) {
        ProcessingRecipeBuilder<BerrySyrupFluidMixingRecipe> builder =
                new ProcessingRecipeBuilder<>(BerrySyrupFluidMixingRecipe::new, id);
        for (int i = 0; i < BERRY_COUNT; i++) {
            builder.require(CDTags.forgeItemTag("berries"));
        }
        builder.require(CDFluids.BASE_SYRUP.get(), SYRUP_AMOUNT);
        return builder.build();
    }

    public static class Serializer implements RecipeSerializer<BerrySyrupFluidMixingRecipe> {
        @Override
        public BerrySyrupFluidMixingRecipe fromJson(ResourceLocation recipeId, JsonObject serializedRecipe) {
            return create(recipeId);
        }

        @Override
        public BerrySyrupFluidMixingRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            return create(recipeId);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, BerrySyrupFluidMixingRecipe recipe) {
        }
    }
}
