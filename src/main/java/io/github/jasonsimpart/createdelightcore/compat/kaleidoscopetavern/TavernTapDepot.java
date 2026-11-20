package io.github.jasonsimpart.createdelightcore.compat.kaleidoscopetavern;

import com.simibubi.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour;
import com.simibubi.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour.TransportedResult;
import com.simibubi.create.content.logistics.depot.DepotBehaviour;
import com.simibubi.create.content.logistics.depot.DepotBlockEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;
import java.util.function.Supplier;

/** One native tap operation fills one carrier, using Create's existing processing buffer. */
public final class TavernTapDepot {
    private TavernTapDepot() {}

    public static boolean canFill(DepotBlockEntity depot, Ingredient carrier) {
        var behaviour = depot.getBehaviour(DepotBehaviour.TYPE);
        ItemStack held = depot.getHeldItem();
        // Create drops processing overflow into the world; reserve its empty buffer before extracting wine.
        return behaviour != null && behaviour.isOutputEmpty() && !held.isEmpty() && carrier.test(held);
    }

    public static void fill(DepotBlockEntity depot, Ingredient carrier, Supplier<ItemStack> extractOne) {
        if (!canFill(depot, carrier)) return;
        var handler = depot.getBehaviour(TransportedItemStackHandlerBehaviour.TYPE);
        if (handler == null) return;
        handler.handleProcessingOnAllItems(input -> {
            if (!canFill(depot, carrier) || !carrier.test(input.stack)) return TransportedResult.doNothing();
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
