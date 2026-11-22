package io.github.jasonsimpart.createdelightcore.compat.kaleidoscopetavern;

import com.github.ysbbbbbb.kaleidoscopetavern.blockentity.brew.BarrelBlockEntity;
import com.github.ysbbbbbb.kaleidoscopetavern.crafting.recipe.BarrelRecipe;
import com.github.ysbbbbbb.kaleidoscopetavern.crafting.serializer.BarrelRecipeSerializer;
import com.github.ysbbbbbb.kaleidoscopetavern.init.ModItems;
import com.github.ysbbbbbb.kaleidoscopetavern.item.BottleBlockItem;
import com.simibubi.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour;
import com.simibubi.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour.TransportedResult;
import com.simibubi.create.content.logistics.depot.DepotBehaviour;
import com.simibubi.create.content.logistics.depot.DepotBlockEntity;
import io.github.jasonsimpart.createdelightcore.mixin.create.DepotOutputBufferAccessor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.List;
import java.util.function.Supplier;

/** One native tap operation fills one carrier, using Create's existing processing buffer. */
public final class TavernTapDepot {
    private TavernTapDepot() {}

    /** Mirrors the native recipe/fallback selection without extracting a serving. */
    public static ItemStack previewOutput(BarrelBlockEntity barrel, Level level) {
        if (barrel instanceof TavernBarrelAccess access && access.createdelightcore$getFluidOutput() != null) {
            return access.createdelightcore$getFluidOutput().previewBottle();
        }
        var recipeId = barrel.getRecipeId();
        ItemStack template = recipeId == null || recipeId.equals(BarrelRecipeSerializer.EMPTY_RECIPE_ID)
                ? new ItemStack(ModItems.VINEGAR.get()) : level.getRecipeManager().byKey(recipeId)
                .filter(BarrelRecipe.class::isInstance).map(BarrelRecipe.class::cast)
                .map(BarrelRecipe::result).orElseGet(() -> new ItemStack(ModItems.VINEGAR.get()));
        return previewOutput(barrel, template);
    }

    /** Uses the same bottle reconstruction and shared quality decorator as extractOneOutput. */
    public static ItemStack previewOutput(BarrelBlockEntity barrel, ItemStack template) {
        if (barrel instanceof TavernBarrelAccess access && access.createdelightcore$getFluidOutput() != null) {
            return access.createdelightcore$getFluidOutput().previewBottle();
        }
        ItemStack result = template.copyWithCount(1);
        if (result.getItem() instanceof BottleBlockItem bottle) result = bottle.getFilledStack(barrel.getBrewLevel());
        return barrel instanceof TavernBarrelAccess access ? access.createdelightcore$qualifyOutput(result) : result;
    }

    public static boolean hasCarrier(DepotBlockEntity depot, Ingredient carrier) {
        ItemStack held = depot.getHeldItem();
        return !held.isEmpty() && carrier.test(held);
    }

    public static boolean deliver(DepotBlockEntity depot, Ingredient carrier, ItemStack serving) {
        int carriers = depot.getHeldItem().getCount();
        fill(depot, carrier, serving, () -> serving);
        ItemStack after = depot.getHeldItem();
        return carriers == 1 ? ItemStack.matches(after, serving) : carriers > 1 && after.getCount() == carriers - 1;
    }

    public static boolean canFill(DepotBlockEntity depot, Ingredient carrier, ItemStack preview) {
        var behaviour = depot.getBehaviour(DepotBehaviour.TYPE);
        ItemStack held = depot.getHeldItem();
        if (behaviour == null || held.isEmpty() || !carrier.test(held) || preview.isEmpty()) return false;
        // A single carrier is replaced in place. Stacked carriers leave the wine in the output buffer.
        if (held.getCount() == 1) return true;
        var buffer = ((DepotOutputBufferAccessor) behaviour).createdelightcore$getProcessingOutputBuffer();
        // Simulate the exact Create insertion, including stack limits, quality and all other NBT.
        return ItemHandlerHelper.insertItemStacked(buffer, preview.copy(), true).isEmpty();
    }

    public static void fill(DepotBlockEntity depot, Ingredient carrier, ItemStack preview, Supplier<ItemStack> extractOne) {
        if (!canFill(depot, carrier, preview)) return;
        var handler = depot.getBehaviour(TransportedItemStackHandlerBehaviour.TYPE);
        if (handler == null) return;
        handler.handleProcessingOnAllItems(input -> {
            if (!canFill(depot, carrier, preview) || !carrier.test(input.stack)) return TransportedResult.doNothing();
            ItemStack result = extractOne.get();
            if (result.isEmpty()) return TransportedResult.doNothing();

            var filled = input.copy();
            filled.stack = result;
            var remainder = input.copy();
            remainder.stack.shrink(1);
            return remainder.stack.isEmpty() ? TransportedResult.convertTo(filled)
                    : TransportedResult.convertToAndLeaveHeld(List.of(filled), remainder);
        });
    }
}
