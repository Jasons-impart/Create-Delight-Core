package io.github.jasonsimpart.createdelightcore.compat.alcohol;

import io.github.createdelight.qualityfoodfluids.api.QualityFoodFluidsApi;
import io.github.createdelight.qualityfoodfluids.api.QualityFoodFluidsCreateRules;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

/** Carries native Youkai tank quality until the newly filled bottle enters the inventory. */
public final class YoukaiBottleQuality {
    private static final ThreadLocal<Batch> CURRENT = new ThreadLocal<>();

    private YoukaiBottleQuality() {}

    public static Scope begin(BlockEntity tile) {
        Batch previous = CURRENT.get();
        Batch batch = null;
        if (tile != null && tile.getClass().getName().startsWith("dev.xkmc.youkaishomecoming.")) {
            var tank = tile.getCapability(ForgeCapabilities.FLUID_HANDLER).orElse(null);
            if (tank != null && tank.getTanks() > 0) {
                var fluid = tank.getFluidInTank(0).copy();
                if (QualityFoodFluidsApi.canCarryQuality(fluid)) batch = new Batch(tank, fluid);
            }
        }
        set(batch);
        return new Scope(previous);
    }

    public static ItemStack qualify(ItemStack bottle) {
        Batch batch = CURRENT.get();
        if (batch == null || bottle.isEmpty()) return bottle;
        FluidStack remaining = batch.tank.getFluidInTank(0);
        if ((!remaining.isEmpty() && remaining.getFluid() != batch.input.getFluid())
                || remaining.getAmount() >= batch.input.getAmount()) return bottle;
        // The native method has drained already. Use the snapshot even when the tank is empty.
        return QualityFoodFluidsCreateRules.applyFillingQuality(batch.input, bottle.copy());
    }

    private static void set(Batch batch) {
        if (batch == null) CURRENT.remove();
        else CURRENT.set(batch);
    }

    private record Batch(IFluidHandler tank, FluidStack input) {}

    public static final class Scope implements AutoCloseable {
        private final Batch previous;
        private Scope(Batch previous) { this.previous = previous; }
        @Override public void close() { set(previous); }
    }
}
