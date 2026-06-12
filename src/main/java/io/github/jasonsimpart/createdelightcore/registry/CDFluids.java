package io.github.jasonsimpart.createdelightcore.registry;

import com.github.alexmodguy.alexscaves.server.misc.ACSoundRegistry;
import com.simibubi.create.AllFluids;
import com.simibubi.create.Create;
import com.simibubi.create.content.fluids.VirtualFluid;
import com.tterrag.registrate.util.entry.FluidEntry;
import fr.lucreeper74.createmetallurgy.content.fluids.MoltenFluidSource;
import io.github.jasonsimpart.createdelightcore.CreateDelightCore;
import io.github.jasonsimpart.createdelightcore.content.fluid.*;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.SoundActions;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.fml.DistExecutor;

import static com.simibubi.create.AllTags.forgeFluidTag;
import static io.github.jasonsimpart.createdelightcore.CreateDelightCore.REGISTRATE;

public class CDFluids {
    public static final ResourceKey<CreativeModeTab> FLUID_TAB = CDCreativeTabs.FLUID.getKey();
    public static final ResourceLocation MILK_STILL = Create.asResource("fluid/milk_still");
    public static final ResourceLocation MILK_FLOW = Create.asResource("fluid/milk_flow");

    // all molten metal
    public static final FluidEntry<ForgeFlowingFluid.Flowing> MOLTEN_ANDESITE = moltenFluid("andesite");
    public static final FluidEntry<ForgeFlowingFluid.Flowing> MOLTEN_AZURE_NEODYMIUM = moltenFluid("azure_neodymium");
    public static final FluidEntry<ForgeFlowingFluid.Flowing> MOLTEN_SCARLET_NEODYMIUM = moltenFluid("scarlet_neodymium");
    public static final FluidEntry<ForgeFlowingFluid.Flowing> MOLTEN_TITANIUM = moltenFluid("titanium");
    public static final FluidEntry<ForgeFlowingFluid.Flowing> MOLTEN_MARTIAN_STEEL = moltenFluid("martian_steel");
    public static final FluidEntry<ForgeFlowingFluid.Flowing> MOLTEN_FIRE_STEEL = moltenFluid("fire_steel");
    public static final FluidEntry<ForgeFlowingFluid.Flowing> MOLTEN_ICE_STEEL = moltenFluid("ice_steel");
    public static final FluidEntry<ForgeFlowingFluid.Flowing> MOLTEN_LIGHTNING_STEEL = moltenFluid("lightning_steel");
    public static final FluidEntry<ForgeFlowingFluid.Flowing> MOLTEN_FORGED_STEEL = moltenFluid("forged_steel");
    public static final FluidEntry<ForgeFlowingFluid.Flowing> MOLTEN_GLASS = moltenFluid("glass");
    public static final FluidEntry<ForgeFlowingFluid.Flowing> MOLTEN_QUARTZ_GLASS = moltenFluid("quartz_glass");
    public static final FluidEntry<ForgeFlowingFluid.Flowing> MOLTEN_QUARTZ_VIBRANT_GLASS = moltenFluid("quartz_vibrant_glass");
    // all ice cream
    // slime
    public static final FluidEntry<ForgeFlowingFluid.Flowing> SLIME = slimeFluid("slime");
    public static final FluidEntry<ForgeFlowingFluid.Flowing> FERROUSLIME = slimeFluid("ferrouslime");
    public static final FluidEntry<ForgeFlowingFluid.Flowing> CHORUSSLIME = slimeFluid("chorusslime");
    // milkShake
    public static final FluidEntry<VirtualFluid> CARROT =  milkShake("carrot", 0xFDC381);
    public static final FluidEntry<VirtualFluid> GLOW_BERRY = milkShake("glow_berry", 0XF5B256);
    public static final FluidEntry<VirtualFluid> ENCHANTED_FRUIT = milkShake("enchanted_fruit", 0Xdfda48);
    public static final FluidEntry<VirtualFluid> APPLE = milkShake("apple", 0Xf6d894);
    public static final FluidEntry<VirtualFluid> BEETROOT = milkShake("beetroot", 0Xea4d5b);
    public static final FluidEntry<VirtualFluid> LUCUMA = milkShake("lucuma", 0Xfcd452);
    public static final FluidEntry<VirtualFluid> PINK_DRAGON_FRUIT = milkShake("pink_dragon_fruit", 0Xe569e7);
    // grapeJuice
    public static final FluidEntry<VirtualFluid> RED_GRAPE = grapeJuice("red_grape", 0X73207a);
    public static final FluidEntry<VirtualFluid> JUNGLE_RED_GRAPE = grapeJuice("jungle_red_grape", 0X4f1d85);
    public static final FluidEntry<VirtualFluid> SAVANNA_RED_GRAPE = grapeJuice("savanna_red_grape", 0Xbe4ee0);
    public static final FluidEntry<VirtualFluid> TAIGA_RED_GRAPE = grapeJuice("taiga_red_grape", 0X7400a8);
    public static final FluidEntry<VirtualFluid> WHITE_GRAPE = grapeJuice("white_grape", 0X819e4c);
    public static final FluidEntry<VirtualFluid> JUNGLE_WHITE_GRAPE = grapeJuice("jungle_white_grape", 0X48531e);
    public static final FluidEntry<VirtualFluid> SAVANNA_WHITE_GRAPE = grapeJuice("savanna_white_grape", 0X98af3d);
    public static final FluidEntry<VirtualFluid> TAIGA_WHITE_GRAPE = grapeJuice("taiga_white_grape", 0X77882f);
    public static final FluidEntry<VirtualFluid> WARPED_GRAPE = grapeJuice("warped_grape", 0X005251);
    public static final FluidEntry<VirtualFluid> CRIMSON_GRAPE = grapeJuice("crimson_grape", 0X651114);
    //radiation fluid
    public static final FluidEntry<ForgeFlowingFluid.Flowing> NUCLEAR_WASTE = radiationFluid("nuclear_waste");


    public static FluidEntry<ForgeFlowingFluid.Flowing> createFluid(String name) {
        ResourceLocation STILL_RL = CreateDelightCore.id("block/fluid/" + name + "/still");
        ResourceLocation FLOW_RL = CreateDelightCore.id("block/fluid/" + name + "/flowing");
        return REGISTRATE.fluid(name, STILL_RL, FLOW_RL).register();
    }

    private static FluidEntry<ForgeFlowingFluid.Flowing> moltenFluid(String name) {
        ResourceLocation STILL_RL = CreateDelightCore.id("block/fluid/" + name + "/still");
        ResourceLocation FLOW_RL = CreateDelightCore.id("block/fluid/" + name + "/flowing");
        return moltenFluid(name, STILL_RL, FLOW_RL);
    }
    private static FluidEntry<ForgeFlowingFluid.Flowing> moltenFluid(String name, ResourceLocation STILL_RL, ResourceLocation FLOW_RL) {
        return REGISTRATE.fluid("molten_" + name, STILL_RL, FLOW_RL, fr.lucreeper74.createmetallurgy.content.fluids.MoltenFluidType::new)
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

    public static FluidEntry<VirtualFluid> milkShake(String name, int colorIn) {
        final int color = 0xFF000000 | colorIn;
        return CreateDelightCore.REGISTRATE.virtualFluid(name + "_milkshake", MILK_STILL, MILK_FLOW, ((p, sT, fT) ->
                        new AllFluids.TintedFluidType(p, sT, fT) {
                            @Override
                            protected int getTintColor(FluidStack stack) {
                                return color;
                            }
                            @Override
                            protected int getTintColor(FluidState state, BlockAndTintGetter getter, BlockPos pos) {
                                return color;
                            }
                        }), VirtualFluid::createSource, VirtualFluid::createFlowing)
                .properties(b -> b
                        .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)
                        .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL))
                .register();
    }

    public static FluidEntry<VirtualFluid> grapeJuice(String name, int colorIn) {
        final int color = 0xFF000000 | colorIn;
        return CreateDelightCore.REGISTRATE.virtualFluid(name + "juice", MILK_STILL, MILK_FLOW, ((p, sT, fT) ->
                        new AllFluids.TintedFluidType(p, sT, fT) {
                            @Override
                            protected int getTintColor(FluidStack stack) {
                                return color;
                            }
                            @Override
                            protected int getTintColor(FluidState state, BlockAndTintGetter getter, BlockPos pos) {
                                return color;
                            }
                        }), VirtualFluid::createSource, VirtualFluid::createFlowing)
                .properties(b -> b
                        .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)
                        .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL))
                .register();
    }



    public static void init() {
    }
}
