package io.github.jasonsimpart.compat.createmetallurgy.backport;

import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import fr.lucreeper74.createmetallurgy.content.blocks.industrial_crucible.CrucibleBlockEntity;
import fr.lucreeper74.createmetallurgy.content.blocks.industrial_crucible.foundry.FoundryItemHandler;
import fr.lucreeper74.createmetallurgy.content.blocks.industrial_crucible.foundry.FoundryTank;
import fr.lucreeper74.createmetallurgy.content.blocks.industrial_crucible.foundry.recipes.base.FoundryRecipe;
import fr.lucreeper74.createmetallurgy.registries.CMRecipeTypes;
import fr.lucreeper74.createmetallurgy.utils.SideAttachment;
import io.github.jasonsimpart.mixin.createmetallurgy.CrucibleAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class AlloyingBackportLogic {
    private AlloyingBackportLogic() {
    }

    public static void tickCrucible(CrucibleBlockEntity be) {
        Level level = be.getLevel();
        if (level == null || level.isClientSide() || !be.isController()) {
            return;
        }

        CrucibleGaugeCacheAccess gaugeCache = (CrucibleGaugeCacheAccess) be;
        if (!gaugeCache.createdelightcore$isGaugeCacheValid()) {
            gaugeCache.createdelightcore$setGaugeAttachmentCache(scanGaugeAttachment(be));
        }
        if (!gaugeCache.createdelightcore$hasGaugeAttachmentCached()) {
            return;
        }

        FoundryTank tank = be.getTank();
        if (tank == null) {
            return;
        }

        ProcessingRecipe<?, ?> recipe = findMatchingRecipe(be, level.getRecipeManager());
        if (recipe == null) {
            return;
        }

        applyRecipe(be, recipe);
    }

    private static boolean scanGaugeAttachment(CrucibleBlockEntity be) {
        Level level = be.getLevel();
        if (level == null) {
            return false;
        }

        BlockPos controllerPos = be.getBlockPos();
        Set<BlockPos> visited = new HashSet<>();
        Deque<BlockPos> queue = new ArrayDeque<>();
        visited.add(controllerPos);
        queue.add(controllerPos);

        while (!queue.isEmpty()) {
            BlockPos currentPos = Objects.requireNonNull(queue.removeFirst());
            BlockEntity blockEntity = level.getBlockEntity(currentPos);
            if (!(blockEntity instanceof CrucibleBlockEntity currentCrucible)) {
                continue;
            }
            if (!controllerPos.equals(currentCrucible.getController())) {
                continue;
            }

            for (Direction dir : Direction.values()) {
                if (currentCrucible.getSideAttachment(dir) == SideAttachment.GAUGE) {
                    return true;
                }
            }

            for (Direction dir : Direction.values()) {
                BlockPos neighborPos = currentPos.relative(dir);
                if (!visited.add(neighborPos)) {
                    continue;
                }

                BlockEntity neighborBlockEntity = level.getBlockEntity(neighborPos);
                if (!(neighborBlockEntity instanceof CrucibleBlockEntity neighborCrucible)) {
                    continue;
                }
                if (!controllerPos.equals(neighborCrucible.getController())) {
                    continue;
                }

                queue.addLast(neighborPos);
            }
        }

        return false;
    }

    private static ProcessingRecipe<?, ?> findMatchingRecipe(CrucibleBlockEntity be, RecipeManager manager) {
        for (var holder : manager.getAllRecipesFor(CMRecipeTypes.ALLOYING.getType())) {
            if (!(holder.value() instanceof ProcessingRecipe<?, ?> processing)) {
                continue;
            }
            if (!FoundryRecipe.matchHeatCondition(be, processing)) {
                continue;
            }
            FoundryItemHandler inputInv = ((CrucibleAccess) be).createdelightcore$getFoundryData().getInputInv();
            if (!matchItemIngredients(inputInv, processing.getIngredients())) {
                continue;
            }
            if (!matchFluidIngredients(be.getTank(), processing.getFluidIngredients())) {
                continue;
            }
            return processing;
        }
        return null;
    }

    private static boolean matchItemIngredients(FoundryItemHandler inv, List<Ingredient> ingredients) {
        if (ingredients.isEmpty()) {
            return true;
        }
        boolean[] used = new boolean[inv.getSlots()];
        for (Ingredient ingredient : ingredients) {
            boolean matched = false;
            for (int i = 0; i < inv.getSlots(); i++) {
                if (used[i]) {
                    continue;
                }
                if (ingredient.test(inv.getStackInSlot(i))) {
                    used[i] = true;
                    matched = true;
                    break;
                }
            }
            if (!matched) {
                return false;
            }
        }
        return true;
    }

    private static boolean matchFluidIngredients(FoundryTank tank, List<SizedFluidIngredient> ingredients) {
        if (ingredients.isEmpty()) {
            return true;
        }
        for (SizedFluidIngredient ingredient : ingredients) {
            int required = ingredient.amount();
            int available = 0;
            for (FluidStack fluid : tank.getFluids()) {
                if (ingredient.test(fluid)) {
                    available += fluid.getAmount();
                }
            }
            if (available < required) {
                return false;
            }
        }
        return true;
    }

    private static void applyRecipe(CrucibleBlockEntity be, ProcessingRecipe<?, ?> recipe) {
        FoundryTank tank = be.getTank();
        FoundryItemHandler inputInv = ((CrucibleAccess) be).createdelightcore$getFoundryData().getInputInv();

        int totalOutput = 0;
        for (FluidStack result : recipe.getFluidResults()) {
            totalOutput += result.getAmount();
        }
        if (tank.getCapacity() - tank.getFillAmount() < totalOutput) {
            return;
        }

        consumeItemIngredients(inputInv, recipe.getIngredients());
        consumeFluidIngredients(tank, recipe.getFluidIngredients());
        produceFluidResults(tank, recipe.getFluidResults());
    }

    private static void consumeItemIngredients(FoundryItemHandler inv, List<Ingredient> ingredients) {
        for (Ingredient ingredient : ingredients) {
            for (int i = 0; i < inv.getSlots(); i++) {
                ItemStack stack = inv.getStackInSlot(i);
                if (!ingredient.test(stack)) {
                    continue;
                }
                stack.shrink(1);
                if (stack.isEmpty()) {
                    inv.setStackInSlot(i, ItemStack.EMPTY);
                } else {
                    inv.setStackInSlot(i, stack);
                }
                break;
            }
        }
    }

    private static void consumeFluidIngredients(FoundryTank tank, List<SizedFluidIngredient> ingredients) {
        for (SizedFluidIngredient ingredient : ingredients) {
            int remaining = ingredient.amount();
            for (FluidStack fluid : tank.getFluids()) {
                if (!ingredient.test(fluid)) {
                    continue;
                }
                int drainAmount = Math.min(remaining, fluid.getAmount());
                FluidStack toDrain = fluid.copyWithAmount(drainAmount);
                tank.drain(toDrain, IFluidHandler.FluidAction.EXECUTE);
                remaining -= drainAmount;
                if (remaining <= 0) {
                    break;
                }
            }
        }
    }

    private static void produceFluidResults(FoundryTank tank, List<FluidStack> results) {
        for (FluidStack result : results) {
            tank.fill(result.copy(), IFluidHandler.FluidAction.EXECUTE);
        }
    }
}
