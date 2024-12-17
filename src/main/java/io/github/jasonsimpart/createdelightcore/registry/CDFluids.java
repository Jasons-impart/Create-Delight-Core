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
                    .tickRate(20))
            .register();
    public static final FluidEntry<ForgeFlowingFluid.Flowing> MOLTEN_AZURE_NEODYMIUM = createFluid("molten_azure_neodymium")
            .fluidProperties(properties -> properties
                    .tickRate(20))
            .register();
    public static final FluidEntry<ForgeFlowingFluid.Flowing> MOLTEN_SCARLET_NEODYMIUM = createFluid("molten_scarlet_neodymium")
            .fluidProperties(properties -> properties
                    .tickRate(20))
            .register();
    public static final FluidEntry<ForgeFlowingFluid.Flowing> MOLTEN_DESH = createFluid("molten_desh")
            .fluidProperties(properties -> properties
                    .tickRate(20))
            .register();
    public static final FluidEntry<ForgeFlowingFluid.Flowing> MOLTEN_OSTRUM = createFluid("molten_ostrum")
            .fluidProperties(properties -> properties
                    .tickRate(20))
            .register();
    public static final FluidEntry<ForgeFlowingFluid.Flowing> MOLTEN_CLAORITE = createFluid("molten_calorite")
            .fluidProperties(properties -> properties
                    .tickRate(20))
            .register();
    public static final FluidEntry<ForgeFlowingFluid.Flowing> MOLTEN_FIRE_STEEL = createFluid("molten_fire_steel")
            .fluidProperties(properties -> properties
                    .tickRate(20))
            .register();
    public static final FluidEntry<ForgeFlowingFluid.Flowing> MOLTEN_ICE_STEEL = createFluid("molten_ice_steel")
            .fluidProperties(properties -> properties
                    .tickRate(20))
            .register();
    public static final FluidEntry<ForgeFlowingFluid.Flowing> MOLTEN_LIGHTNING_STEEL = createFluid("molten_lightning_steel")
            .fluidProperties(properties -> properties
                    .tickRate(20))
            .register();

    public static FluidBuilder<ForgeFlowingFluid.Flowing, Registrate> createFluid(String name) {
        return REGISTRATE.fluid(name,
                new ResourceLocation(CreateDelightCore.MODID, "block/" + name + "_still"),
                new ResourceLocation(CreateDelightCore.MODID, "block/" + name + "_flowing"));
    }

    public static void init() {

    }
}
