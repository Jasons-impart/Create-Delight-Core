package io.github.jasonsimpart.createdelightcore.compat.fluidlogistics;

import com.yision.fluidlogistics.content.processing.blazeCooler.BlazeCoolerFuelManager;
import io.github.jasonsimpart.createdelightcore.util.Triplet;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;

public final class BlazeCoolerFuels {
    private BlazeCoolerFuels() {
    }

    public static Map<ResourceLocation, Triplet<Integer, Boolean, Integer>> snapshot() {
        Map<ResourceLocation, Triplet<Integer, Boolean, Integer>> fuels = new HashMap<>();
        ForgeRegistries.FLUIDS.getEntries().forEach(entry -> {
            var fuel = BlazeCoolerFuelManager.find(new FluidStack(entry.getValue(), 1));
            if (fuel != null) {
                fuels.put(entry.getKey().location(), Triplet.of(fuel.coolTime(), fuel.supercooled(), fuel.amount()));
            }
        });
        return fuels;
    }
}
