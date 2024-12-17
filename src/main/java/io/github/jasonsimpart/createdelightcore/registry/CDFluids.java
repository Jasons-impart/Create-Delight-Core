package io.github.jasonsimpart.createdelightcore.registry;

import com.tterrag.registrate.Registrate;
import com.tterrag.registrate.builders.FluidBuilder;
import com.tterrag.registrate.util.entry.FluidEntry;
import io.github.jasonsimpart.createdelightcore.CreateDelightCore;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.fluids.ForgeFlowingFluid;

import static io.github.jasonsimpart.createdelightcore.registry.CDRegistration.REGISTRATE;

public class CDFluids {
    public static final ResourceKey<CreativeModeTab> FLUID_TAB = CDCreativeTabs.Fluid.getKey();

    static {
        REGISTRATE.defaultCreativeTab(CDFluids.FLUID_TAB);
    }

    public static final FluidEntry<ForgeFlowingFluid.Flowing> MOLTEN_ANDESITE = createFluid("molten_andesite")
            .fluidProperties(properties -> properties
                    .tickRate(15))
            .register();

    public static FluidBuilder<ForgeFlowingFluid.Flowing, Registrate> createFluid(String name) {
        return REGISTRATE.fluid(name,
                new ResourceLocation(CreateDelightCore.MODID, "block/" + name + "_still"),
                new ResourceLocation(CreateDelightCore.MODID, "block/" + name + "_flowing"));
    }

    public static void init() {

    }
}
