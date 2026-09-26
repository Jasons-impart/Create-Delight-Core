package io.github.jasonsimpart.createdelightcore.compat.createliquidfuel;

import com.forsteri.createliquidfuel.core.BurnerStomachHandler;
import io.github.jasonsimpart.createdelightcore.util.Triplet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;

/**
 * Isolates Create Liquid Fuel API access so SyncFuelMapsPacket never hard-links
 * against a missing or partially loaded BurnerStomachHandler.
 */
public final class BlazeBurnerFuels {
    private BlazeBurnerFuels() {
    }

    public static Map<ResourceLocation, Triplet<Integer, Boolean, Integer>> snapshot() {
        Map<ResourceLocation, Triplet<Integer, Boolean, Integer>> fuels = new HashMap<>();
        BurnerStomachHandler.LIQUID_BURNER_FUEL_MAP.forEach((fluid, pair) -> {
            ResourceLocation rl = ForgeRegistries.FLUIDS.getKey(fluid);
            if (rl != null && pair != null && pair.getSecond() != null) {
                var t = pair.getSecond();
                fuels.put(rl, Triplet.of(t.getFirst(), t.getSecond(), t.getThird()));
            }
        });
        return fuels;
    }

    /**
     * Remaining burn ticks from one current fuel cycle plus all liquid fuel in the stack.
     */
    public static long totalBurnTicks(int remainingBurnTime, FluidStack fluid) {
        long total = Math.max(remainingBurnTime, 0);
        if (fluid == null || fluid.isEmpty()) {
            return total;
        }
        var pair = BurnerStomachHandler.LIQUID_BURNER_FUEL_MAP.get(fluid.getFluid());
        if (pair == null || pair.getSecond() == null) {
            return total;
        }
        var t = pair.getSecond();
        Integer burnTime = t.getFirst();
        Integer amountConsume = t.getThird();
        if (burnTime != null && amountConsume != null && amountConsume > 0 && burnTime > 0) {
            total += (long) (fluid.getAmount() / amountConsume) * burnTime;
        }
        return total;
    }

    public static long totalBurnTicks(int remainingBurnTime, Fluid fluid, int amount) {
        return totalBurnTicks(remainingBurnTime, fluid == null ? FluidStack.EMPTY : new FluidStack(fluid, amount));
    }
}
