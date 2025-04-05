package io.github.jasonsimpart.createdelightcore.content.event.FluidInteraction;

import com.tterrag.registrate.util.entry.FluidEntry;
import io.github.jasonsimpart.createdelightcore.CreateDelightCore;
import io.github.jasonsimpart.createdelightcore.registry.CDFluids;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidInteractionRegistry;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod.EventBusSubscriber(modid = CreateDelightCore.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class IceCreamFluidInteraction {
    public static final FluidEntry<ForgeFlowingFluid.Flowing> ADZUKI =CDFluids.ADZUKI_ICE_CREAM;
    public static final FluidEntry<ForgeFlowingFluid.Flowing> BANANA =CDFluids.BANANA_ICE_CREAM;
    public static final FluidEntry<ForgeFlowingFluid.Flowing> CHOCOLATE =CDFluids.CHOCOLATE_ICE_CREAM;
    public static final FluidEntry<ForgeFlowingFluid.Flowing> MINT =CDFluids.MINT_ICE_CREAM;
    public static final FluidEntry<ForgeFlowingFluid.Flowing> STRAWBERRY =CDFluids.STRAWBERRY_ICE_CREAM;
    public static final FluidEntry<ForgeFlowingFluid.Flowing> VANILLA =CDFluids.VANILLA_ICE_CREAM;
    public static final FluidEntry<ForgeFlowingFluid.Flowing> LIME =CDFluids.LIME_ICE_CREAM;
    public static final FluidEntry<ForgeFlowingFluid.Flowing> POMEGRANATE =CDFluids.POMEGRANATE_ICE_CREAM;

    @SubscribeEvent
    public static void register(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> addInteraction(ADZUKI.get().getFluidType()));
        event.enqueueWork(() -> addInteraction(BANANA.get().getFluidType()));
        event.enqueueWork(() -> addInteraction(CHOCOLATE.get().getFluidType()));
        event.enqueueWork(() -> addInteraction(MINT.get().getFluidType()));
        event.enqueueWork(() -> addInteraction(STRAWBERRY.get().getFluidType()));
        event.enqueueWork(() -> addInteraction(VANILLA.get().getFluidType()));
        event.enqueueWork(() -> addInteraction(LIME.get().getFluidType()));
        event.enqueueWork(() -> addInteraction(POMEGRANATE.get().getFluidType()));
    }
    private static void addInteraction(FluidType fluidType) {
        FluidInteractionRegistry.addInteraction(ForgeMod.LAVA_TYPE.get(), new FluidInteractionRegistry.InteractionInformation(fluidType, fluidState -> fluidState.isSource() ? Blocks.OBSIDIAN.defaultBlockState() : Blocks.STONE.defaultBlockState()));
        FluidInteractionRegistry.addInteraction(ForgeMod.WATER_TYPE.get(), new FluidInteractionRegistry.InteractionInformation(fluidType, fluidState -> fluidState.isSource() ? Blocks.PACKED_ICE.defaultBlockState() : Blocks.ICE.defaultBlockState()));
    }
}
