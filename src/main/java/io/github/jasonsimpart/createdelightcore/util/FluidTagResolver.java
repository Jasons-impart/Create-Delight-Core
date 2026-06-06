package io.github.jasonsimpart.createdelightcore.util;

import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITagManager;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class FluidTagResolver {

    private FluidTagResolver() {
    }

    public static List<FluidStack> resolve(TagKey<Fluid> tag, int amount) {
        Set<Fluid> fluids = new LinkedHashSet<>();

        ITagManager<Fluid> tagManager = ForgeRegistries.FLUIDS.tags();
        if (tagManager != null) {
            tagManager.getTag(tag)
                    .forEach(fluid -> addSourceFluid(fluids, fluid));
        }

        for (Fluid fluid : ForgeRegistries.FLUIDS.getValues()) {
            if (fluid.is(tag))
                addSourceFluid(fluids, fluid);
        }

        return fluids.stream()
                .map(fluid -> new FluidStack(fluid, amount))
                .collect(Collectors.toList());
    }

    private static void addSourceFluid(Set<Fluid> fluids, Fluid fluid) {
        fluids.add(fluid instanceof FlowingFluid flowing ? flowing.getSource() : fluid);
    }
}
