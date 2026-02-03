package io.github.jasonsimpart.createdelightcore.content.event.FluidInteraction;

import appeng.core.definitions.AEBlocks;
import com.github.alexmodguy.alexscaves.server.block.ACBlockRegistry;
import com.github.alexthe666.iceandfire.block.IafBlockRegistry;
import com.lightning.northstar.content.NorthstarBlocks;
import com.mrh0.createaddition.index.CABlocks;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.decoration.palettes.AllPaletteStoneTypes;
import com.tterrag.registrate.util.entry.FluidEntry;
import earth.terrarium.adastra.common.registry.ModBlocks;
import fr.lucreeper74.createmetallurgy.registries.CMBlocks;
import fr.lucreeper74.createmetallurgy.registries.CMFluids;
import io.github.jasonsimpart.createdelightcore.CreateDelightCore;
import io.github.jasonsimpart.createdelightcore.registry.CDBlocks;
import io.github.jasonsimpart.createdelightcore.registry.CDFluids;
import io.github.jasonsimpart.createutilitiesj.blocks.CUBlocks;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidInteractionRegistry;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = CreateDelightCore.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class MoltenMetalFluidInteraction {
    public static final FluidEntry<ForgeFlowingFluid.Flowing> ANDESITE = CDFluids.MOLTEN_ANDESITE;
    public static final FluidEntry<ForgeFlowingFluid.Flowing> AZURE_NEODYMIUM = CDFluids.MOLTEN_AZURE_NEODYMIUM;
    public static final FluidEntry<ForgeFlowingFluid.Flowing> SCARLET_NEODYMIUM = CDFluids.MOLTEN_SCARLET_NEODYMIUM;
    public static final FluidEntry<ForgeFlowingFluid.Flowing> DESH = CDFluids.MOLTEN_DESH;
    public static final FluidEntry<ForgeFlowingFluid.Flowing> OSTRUM = CDFluids.MOLTEN_OSTRUM;
    public static final FluidEntry<ForgeFlowingFluid.Flowing> CLAORITE = CDFluids.MOLTEN_CLAORITE;
    public static final FluidEntry<ForgeFlowingFluid.Flowing> TITANIUM = CDFluids.MOLTEN_TITANIUM;
    public static final FluidEntry<ForgeFlowingFluid.Flowing> MARTIAN_STEEL = CDFluids.MOLTEN_MARTIAN_STEEL;
    public static final FluidEntry<ForgeFlowingFluid.Flowing> FIRE_STEEL = CDFluids.MOLTEN_FIRE_STEEL;
    public static final FluidEntry<ForgeFlowingFluid.Flowing> ICE_STEEL = CDFluids.MOLTEN_ICE_STEEL;
    public static final FluidEntry<ForgeFlowingFluid.Flowing> LIGHTNING_STEEL = CDFluids.MOLTEN_LIGHTNING_STEEL;
    public static final FluidEntry<ForgeFlowingFluid.Flowing> IRON = CMFluids.MOLTEN_IRON;
    public static final FluidEntry<ForgeFlowingFluid.Flowing> COPPER = CMFluids.MOLTEN_COPPER;
    public static final FluidEntry<ForgeFlowingFluid.Flowing> GOLD = CMFluids.MOLTEN_GOLD;
    public static final FluidEntry<ForgeFlowingFluid.Flowing> ZINC = CMFluids.MOLTEN_ZINC;
    public static final FluidEntry<ForgeFlowingFluid.Flowing> TIN = CMFluids.MOLTEN_TIN;
    public static final FluidEntry<ForgeFlowingFluid.Flowing> SILVER = CMFluids.MOLTEN_SILVER;
    public static final FluidEntry<ForgeFlowingFluid.Flowing> TUNGSTEN = CMFluids.MOLTEN_TUNGSTEN;
    public static final FluidEntry<ForgeFlowingFluid.Flowing> BRASS = CMFluids.MOLTEN_BRASS;
    public static final FluidEntry<ForgeFlowingFluid.Flowing> BRONZE = CMFluids.MOLTEN_BRONZE;
    public static final FluidEntry<ForgeFlowingFluid.Flowing> FORGED_STEEL = CDFluids.MOLTEN_FORGED_STEEL;
    public static final FluidEntry<ForgeFlowingFluid.Flowing> ELECTRUM = CMFluids.MOLTEN_ELECTRUM;
    public static final FluidEntry<ForgeFlowingFluid.Flowing> NETHERITE = CMFluids.MOLTEN_NETHERITE;
    public static final FluidEntry<ForgeFlowingFluid.Flowing> STEEL = CMFluids.MOLTEN_STEEL;
    public static final FluidEntry<ForgeFlowingFluid.Flowing> VOID_STEEL = CMFluids.MOLTEN_VOID_STEEL;
    public static final FluidEntry<ForgeFlowingFluid.Flowing> OBDURIUM = CMFluids.MOLTEN_OBDURIUM;
    public static final FluidEntry<ForgeFlowingFluid.Flowing> SLAG = CMFluids.MOLTEN_SLAG;
    public static final FluidEntry<ForgeFlowingFluid.Flowing> GLASS = CDFluids.MOLTEN_GLASS;
    public static final FluidEntry<ForgeFlowingFluid.Flowing> QUARTZ_GLASS = CDFluids.MOLTEN_QUARTZ_GLASS;
    public static final FluidEntry<ForgeFlowingFluid.Flowing> QUARTZ_VIBRANT_GLASS = CDFluids.MOLTEN_QUARTZ_VIBRANT_GLASS;

    @SubscribeEvent
    public static void register(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> addInteraction(ANDESITE.get().getFluidType()));
        event.enqueueWork(() -> addInteraction(AZURE_NEODYMIUM.get().getFluidType()));
        event.enqueueWork(() -> addInteraction(SCARLET_NEODYMIUM.get().getFluidType()));
        event.enqueueWork(() -> addInteraction(DESH.get().getFluidType()));
        event.enqueueWork(() -> addInteraction(OSTRUM.get().getFluidType()));
        event.enqueueWork(() -> addInteraction(CLAORITE.get().getFluidType()));
        event.enqueueWork(() -> addInteraction(FIRE_STEEL.get().getFluidType()));
        event.enqueueWork(() -> addInteraction(ICE_STEEL.get().getFluidType()));
        event.enqueueWork(() -> addInteraction(LIGHTNING_STEEL.get().getFluidType()));
        event.enqueueWork(() -> addInteraction(IRON.get().getFluidType()));
        event.enqueueWork(() -> addInteraction(COPPER.get().getFluidType()));
        event.enqueueWork(() -> addInteraction(GOLD.get().getFluidType()));
        event.enqueueWork(() -> addInteraction(ZINC.get().getFluidType()));
        event.enqueueWork(() -> addInteraction(TIN.get().getFluidType()));
        event.enqueueWork(() -> addInteraction(SILVER.get().getFluidType()));
        event.enqueueWork(() -> addInteraction(TUNGSTEN.get().getFluidType()));
        event.enqueueWork(() -> addInteraction(BRASS.get().getFluidType()));
        event.enqueueWork(() -> addInteraction(BRONZE.get().getFluidType()));
        event.enqueueWork(() -> addInteraction(FORGED_STEEL.get().getFluidType()));
        event.enqueueWork(() -> addInteraction(ELECTRUM.get().getFluidType()));
        event.enqueueWork(() -> addInteraction(NETHERITE.get().getFluidType()));
        event.enqueueWork(() -> addInteraction(STEEL.get().getFluidType()));
        event.enqueueWork(() -> addInteraction(VOID_STEEL.get().getFluidType()));
        event.enqueueWork(() -> addInteraction(OBDURIUM.get().getFluidType()));
        event.enqueueWork(() -> addInteraction(SLAG.get().getFluidType()));
        event.enqueueWork(() -> addInteraction(GLASS.get().getFluidType()));
        event.enqueueWork(() -> addInteraction(QUARTZ_GLASS.get().getFluidType()));
        event.enqueueWork(() -> addInteraction(QUARTZ_VIBRANT_GLASS.get().getFluidType()));
    }

    private static void addMoltenMetalInteraction(FluidType fluidType, BlockState blockState) {
        FluidInteractionRegistry.addInteraction(fluidType, new FluidInteractionRegistry.InteractionInformation(ForgeMod.WATER_TYPE.get(), fluidState -> fluidState.isSource() ? blockState : AllPaletteStoneTypes.SCORCHIA.getBaseBlock().get().defaultBlockState()));
    }

    private static final Map<FluidType, BlockState> moltenMetalBlocks = new HashMap<>();

    private static void addInteraction(FluidType fluidType) {
        if (moltenMetalBlocks.isEmpty()) {
            moltenMetalBlocks.put(ANDESITE.get().getFluidType(), AllBlocks.ANDESITE_ALLOY_BLOCK.getDefaultState());
            moltenMetalBlocks.put(AZURE_NEODYMIUM.get().getFluidType(), ACBlockRegistry.BLOCK_OF_AZURE_NEODYMIUM.get().defaultBlockState());
            moltenMetalBlocks.put(SCARLET_NEODYMIUM.get().getFluidType(), ACBlockRegistry.BLOCK_OF_SCARLET_NEODYMIUM.get().defaultBlockState());
            moltenMetalBlocks.put(TITANIUM.get().getFluidType(), NorthstarBlocks.TITANIUM_BLOCK.getDefaultState());
            moltenMetalBlocks.put(MARTIAN_STEEL.get().getFluidType(), NorthstarBlocks.MARTIAN_STEEL_BLOCK.getDefaultState());

            moltenMetalBlocks.put(DESH.get().getFluidType(), ModBlocks.DESH_BLOCK.get().defaultBlockState());
            moltenMetalBlocks.put(OSTRUM.get().getFluidType(), ModBlocks.OSTRUM_BLOCK.get().defaultBlockState());
            moltenMetalBlocks.put(CLAORITE.get().getFluidType(), ModBlocks.CALORITE_BLOCK.get().defaultBlockState());
            moltenMetalBlocks.put(FIRE_STEEL.get().getFluidType(), IafBlockRegistry.DRAGONSTEEL_FIRE_BLOCK.get().defaultBlockState());
            moltenMetalBlocks.put(ICE_STEEL.get().getFluidType(), IafBlockRegistry.DRAGONSTEEL_ICE_BLOCK.get().defaultBlockState());
            moltenMetalBlocks.put(LIGHTNING_STEEL.get().getFluidType(), IafBlockRegistry.DRAGONSTEEL_LIGHTNING_BLOCK.get().defaultBlockState());
            moltenMetalBlocks.put(IRON.get().getFluidType(), Blocks.IRON_BLOCK.defaultBlockState());
            moltenMetalBlocks.put(COPPER.get().getFluidType(), Blocks.COPPER_BLOCK.defaultBlockState());
            moltenMetalBlocks.put(GOLD.get().getFluidType(), Blocks.GOLD_BLOCK.defaultBlockState());
            moltenMetalBlocks.put(ZINC.get().getFluidType(), AllBlocks.ZINC_BLOCK.get().defaultBlockState());
            moltenMetalBlocks.put(TIN.get().getFluidType(), CDBlocks.TIN.getDefaultState());
            moltenMetalBlocks.put(SILVER.get().getFluidType(), IafBlockRegistry.SILVER_BLOCK.get().defaultBlockState());
            moltenMetalBlocks.put(TUNGSTEN.get().getFluidType(), CMBlocks.TUNGSTEN_BLOCK.get().defaultBlockState());
            moltenMetalBlocks.put(BRASS.get().getFluidType(), AllBlocks.BRASS_BLOCK.get().defaultBlockState());
            moltenMetalBlocks.put(BRONZE.get().getFluidType(), CDBlocks.BRONZE.getDefaultState());
            moltenMetalBlocks.put(FORGED_STEEL.get().getFluidType(), CDBlocks.FORGED_STEEL.getDefaultState());
            moltenMetalBlocks.put(ELECTRUM.get().getFluidType(), CABlocks.ELECTRUM_BLOCK.getDefaultState());
            moltenMetalBlocks.put(NETHERITE.get().getFluidType(), Blocks.NETHERITE_BLOCK.defaultBlockState());
            moltenMetalBlocks.put(STEEL.get().getFluidType(), CMBlocks.STEEL_BLOCK.get().defaultBlockState());
            moltenMetalBlocks.put(VOID_STEEL.get().getFluidType(), CUBlocks.VOID_STEEL_BLOCK.get().defaultBlockState());
            moltenMetalBlocks.put(OBDURIUM.get().getFluidType(), CMBlocks.OBDURIUM_BLOCK.getDefaultState());
            moltenMetalBlocks.put(SLAG.get().getFluidType(), Blocks.TUFF.defaultBlockState());
            moltenMetalBlocks.put(GLASS.get().getFluidType(), Blocks.GLASS.defaultBlockState());
            moltenMetalBlocks.put(QUARTZ_GLASS.get().getFluidType(), AEBlocks.QUARTZ_GLASS.block().defaultBlockState());
            moltenMetalBlocks.put(QUARTZ_VIBRANT_GLASS.get().getFluidType(), AEBlocks.QUARTZ_VIBRANT_GLASS.block().defaultBlockState());
        }

        addMoltenMetalInteraction(fluidType, moltenMetalBlocks.get(fluidType));
    }
}
