package io.github.jasonsimpart.createdelightcore.registry;

import com.github.alexmodguy.alexscaves.server.misc.ACSoundRegistry;
import com.tterrag.registrate.Registrate;
import com.tterrag.registrate.builders.FluidBuilder;
import com.tterrag.registrate.util.entry.FluidEntry;
import io.github.jasonsimpart.createdelightcore.CreateDelightCore;
import io.github.jasonsimpart.createdelightcore.content.fluid.*;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.SoundActions;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.fml.DistExecutor;

import static com.simibubi.create.AllTags.forgeFluidTag;
import static io.github.jasonsimpart.createdelightcore.registry.CDRegistration.REGISTRATE;

public class CDFluids {
    public static final ResourceKey<CreativeModeTab> FLUID_TAB = CDCreativeTabs.FLUID.getKey();

    // all molten metal
    public static final FluidEntry<ForgeFlowingFluid.Flowing> MOLTEN_ANDESITE = moltenFluid("andesite");
    public static final FluidEntry<ForgeFlowingFluid.Flowing> MOLTEN_AZURE_NEODYMIUM = moltenFluid("azure_neodymium");
    public static final FluidEntry<ForgeFlowingFluid.Flowing> MOLTEN_SCARLET_NEODYMIUM = moltenFluid("scarlet_neodymium");
    public static final FluidEntry<ForgeFlowingFluid.Flowing> MOLTEN_DESH = moltenFluid("desh");
    public static final FluidEntry<ForgeFlowingFluid.Flowing> MOLTEN_OSTRUM = moltenFluid("ostrum");
    public static final FluidEntry<ForgeFlowingFluid.Flowing> MOLTEN_CLAORITE = moltenFluid("calorite");
    public static final FluidEntry<ForgeFlowingFluid.Flowing> MOLTEN_FIRE_STEEL = moltenFluid("fire_steel");
    public static final FluidEntry<ForgeFlowingFluid.Flowing> MOLTEN_ICE_STEEL = moltenFluid("ice_steel");
    public static final FluidEntry<ForgeFlowingFluid.Flowing> MOLTEN_LIGHTNING_STEEL = moltenFluid("lightning_steel");
    public static final FluidEntry<ForgeFlowingFluid.Flowing> MOLTEN_FORGED_STEEL = moltenFluid("forged_steel");
    // all ice cream
    public static final FluidEntry<ForgeFlowingFluid.Flowing> ADZUKI_ICE_CREAM = iceCreamFluid("adzuki");
    public static final FluidEntry<ForgeFlowingFluid.Flowing> BANANA_ICE_CREAM = iceCreamFluid("banana");
    public static final FluidEntry<ForgeFlowingFluid.Flowing> CHOCOLATE_ICE_CREAM = iceCreamFluid("chocolate");
    public static final FluidEntry<ForgeFlowingFluid.Flowing> MINT_ICE_CREAM = iceCreamFluid("mint");
    public static final FluidEntry<ForgeFlowingFluid.Flowing> STRAWBERRY_ICE_CREAM = iceCreamFluid("strawberry");
    public static final FluidEntry<ForgeFlowingFluid.Flowing> VANILLA_ICE_CREAM = iceCreamFluid("vanilla");
    public static final FluidEntry<ForgeFlowingFluid.Flowing> LIME_ICE_CREAM = iceCreamFluid("lime");
    public static final FluidEntry<ForgeFlowingFluid.Flowing> POMEGRANATE_ICE_CREAM = iceCreamFluid("pomegranate");
    public static final FluidEntry<ForgeFlowingFluid.Flowing> SWEETBERRY_ICE_CREAM = iceCreamFluid("sweetberry");
    // slime
    public static final FluidEntry<ForgeFlowingFluid.Flowing> SLIME = slimeFluid("slime");
    public static final FluidEntry<ForgeFlowingFluid.Flowing> FERROUSLIME = slimeFluid("ferrouslime");
    //radiation fluid
    public static final FluidEntry<ForgeFlowingFluid.Flowing> NUCLEAR_WASTE = radiationFluid("nuclear_waste");


    public static FluidBuilder<ForgeFlowingFluid.Flowing, Registrate> createFluid(String name) {
        ResourceLocation STILL_RL = CreateDelightCore.id("block/fluid/" + name + "/still");
        ResourceLocation FLOW_RL = CreateDelightCore.id("block/fluid/" + name + "/flowing");
        return REGISTRATE.fluid(name, STILL_RL, FLOW_RL);
    }

    private static FluidEntry<ForgeFlowingFluid.Flowing> moltenFluid(String name) {
        ResourceLocation STILL_RL = CreateDelightCore.id("block/fluid/" + name + "/still");
        ResourceLocation FLOW_RL = CreateDelightCore.id("block/fluid/" + name + "/flowing");
        return REGISTRATE.fluid("molten_" + name, STILL_RL, FLOW_RL, MoltenFluidType::new)
                .properties(b -> b.viscosity(2000)
                        .density(1400)
                        .lightLevel(15)
                        .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY_LAVA)
                        .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL_LAVA)
                        .canHydrate(false)
                        .canDrown(false)
                        .canSwim(false))
                .fluidProperties(p -> p.levelDecreasePerBlock(2)
                        .tickRate(25)
                        .slopeFindDistance(3)
                        .explosionResistance((float) 100.0))
                .tag(forgeFluidTag("molten_" + name), forgeFluidTag("molten_materials"))
                .source(MoltenFluidSource::new)
                .bucket()
                .tab(FLUID_TAB)
                .build()
                .register();
    }

    private static FluidEntry<ForgeFlowingFluid.Flowing> iceCreamFluid(String name) {
        ResourceLocation STILL_RL = CreateDelightCore.id("block/fluid/" + name + "/still");
        ResourceLocation FLOW_RL = CreateDelightCore.id("block/fluid/" + name + "/flowing");
        return REGISTRATE.fluid(name + "_ice_cream", STILL_RL, FLOW_RL, IceCreamFluidType::new)
                .properties(b -> b.viscosity(2000)
                        .density(1400)
                        .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY_POWDER_SNOW)
                        .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL_POWDER_SNOW)
                        .canHydrate(false)
                        .supportsBoating(true))
                .fluidProperties(p -> p.levelDecreasePerBlock(2)
                        .tickRate(20)
                        .slopeFindDistance(3)
                        .explosionResistance((float) 50.0))
                .tag(forgeFluidTag(name + "_ice_cream"), forgeFluidTag("ice_cream"))
                .source(IceCreamFluidSource::new)
                .bucket()
                .tab(FLUID_TAB)
                .build()
                .register();
    }

    private static FluidEntry<ForgeFlowingFluid.Flowing> slimeFluid(String name) {
        ResourceLocation STILL_RL = CreateDelightCore.id("block/fluid/" + name + "/still");
        ResourceLocation FLOW_RL = CreateDelightCore.id("block/fluid/" + name + "/flowing");
        var reg = REGISTRATE.fluid(name, STILL_RL, FLOW_RL, SlimeFluidType::new);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> reg.renderType(RenderType::translucent));
        reg
                .properties(b -> b.viscosity(2000)
                        .density(1400)
                        .sound(SoundActions.BUCKET_EMPTY, SoundEvents.SLIME_BLOCK_BREAK)
                        .sound(SoundActions.BUCKET_FILL, SoundEvents.SLIME_BLOCK_BREAK)
                        .canHydrate(false)
                        .supportsBoating(true))
                .fluidProperties(p -> p.levelDecreasePerBlock(3)
                        .tickRate(25)
                        .slopeFindDistance(3)
                        .explosionResistance((float) 50.0))
                .source(ForgeFlowingFluid.Source::new)
                .bucket()
                .tab(FLUID_TAB)
                .build();
            return reg.register();
    }

    private static FluidEntry<ForgeFlowingFluid.Flowing> radiationFluid(String name) {
        ResourceLocation STILL_RL = CreateDelightCore.id("block/fluid/" + name + "/still");
        ResourceLocation FLOW_RL = CreateDelightCore.id("block/fluid/" + name + "/flowing");
        return REGISTRATE.fluid(name, STILL_RL, FLOW_RL, RadiationFluidType::new)
                .properties(b -> b.viscosity(2000)
                        .density(1400)
                        .lightLevel(5)
                        .sound(SoundActions.BUCKET_EMPTY, ACSoundRegistry.ACID_UNSUBMERGE.get())
                        .sound(SoundActions.BUCKET_FILL, ACSoundRegistry.ACID_SUBMERGE.get())
                        .canHydrate(true)
                        .canDrown(true)
                        .canSwim(true))
                .fluidProperties(p -> p.levelDecreasePerBlock(2)
                        .tickRate(5)
                        .explosionResistance((float) 100.0))
                .source(ForgeFlowingFluid.Source::new)
                .bucket()
                .tab(FLUID_TAB)
                .build()
                .register();
    }


    public static void init() {
    }
}
