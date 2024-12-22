package io.github.jasonsimpart.createdelightcore.content.event.FluidInteraction;

import com.github.alexmodguy.alexscaves.server.block.ACBlockRegistry;
import com.github.alexthe666.iceandfire.block.IafBlockRegistry;
import com.simibubi.create.AllBlocks;
import com.tterrag.registrate.util.entry.FluidEntry;
import earth.terrarium.adastra.common.registry.ModBlocks;
import fr.lucreeper74.createmetallurgy.registries.CMBlocks;
import fr.lucreeper74.createmetallurgy.registries.CMFluids;
import io.github.jasonsimpart.createdelightcore.CreateDelightCore;
import io.github.jasonsimpart.createdelightcore.registry.CDFluids;
import me.duquee.createutilities.blocks.CUBlocks;
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
    public static final FluidEntry<ForgeFlowingFluid.Flowing> ELECTRUM = CMFluids.MOLTEN_ELECTRUM;
    public static final FluidEntry<ForgeFlowingFluid.Flowing> NETHERITE = CMFluids.MOLTEN_NETHERITE;
    public static final FluidEntry<ForgeFlowingFluid.Flowing> STEEL = CMFluids.MOLTEN_STEEL;
    public static final FluidEntry<ForgeFlowingFluid.Flowing> VOID_STEEL = CMFluids.MOLTEN_VOID_STEEL;

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
//        event.enqueueWork(() -> addInteraction(TIN.get().getFluidType()));
        event.enqueueWork(() -> addInteraction(SILVER.get().getFluidType()));
        event.enqueueWork(() -> addInteraction(TUNGSTEN.get().getFluidType()));
        event.enqueueWork(() -> addInteraction(BRASS.get().getFluidType()));
//        event.enqueueWork(() -> addInteraction(BRONZE.get().getFluidType()));
//        event.enqueueWork(() -> addInteraction(ELECTRUM.get().getFluidType()));
        event.enqueueWork(() -> addInteraction(NETHERITE.get().getFluidType()));
        event.enqueueWork(() -> addInteraction(STEEL.get().getFluidType()));
        event.enqueueWork(() -> addInteraction(VOID_STEEL.get().getFluidType()));

    }

    private static void addMoltenMetalInteraction(FluidType fluidType, BlockState blockState) {
        FluidInteractionRegistry.addInteraction(fluidType, new FluidInteractionRegistry.InteractionInformation(ForgeMod.WATER_TYPE.get(), fluidState -> fluidState.isSource() ? blockState : Blocks.TUFF.defaultBlockState()));
        FluidInteractionRegistry.addInteraction(fluidType, new FluidInteractionRegistry.InteractionInformation(CDFluids.ADZUKI_ICE_CREAM.get().getFluidType(), fluidState -> fluidState.isSource() ? blockState : Blocks.TUFF.defaultBlockState()));
        FluidInteractionRegistry.addInteraction(fluidType, new FluidInteractionRegistry.InteractionInformation(CDFluids.BANANA_ICE_CREAM.get().getFluidType(), fluidState -> fluidState.isSource() ? blockState : Blocks.TUFF.defaultBlockState()));
        FluidInteractionRegistry.addInteraction(fluidType, new FluidInteractionRegistry.InteractionInformation(CDFluids.CHOCOLATE_ICE_CREAM.get().getFluidType(), fluidState -> fluidState.isSource() ? blockState : Blocks.TUFF.defaultBlockState()));
        FluidInteractionRegistry.addInteraction(fluidType, new FluidInteractionRegistry.InteractionInformation(CDFluids.MINT_ICE_CREAM.get().getFluidType(), fluidState -> fluidState.isSource() ? blockState : Blocks.TUFF.defaultBlockState()));
        FluidInteractionRegistry.addInteraction(fluidType, new FluidInteractionRegistry.InteractionInformation(CDFluids.STRAWBERRY_ICE_CREAM.get().getFluidType(), fluidState -> fluidState.isSource() ? blockState : Blocks.TUFF.defaultBlockState()));
        FluidInteractionRegistry.addInteraction(fluidType, new FluidInteractionRegistry.InteractionInformation(CDFluids.VANILLA_ICE_CREAM.get().getFluidType(), fluidState -> fluidState.isSource() ? blockState : Blocks.TUFF.defaultBlockState()));
    }

//    private static Map<FluidType, BlockState> moltenMetalBlocks = new HashMap<>();
//
//    static {
//        moltenMetalBlocks.put(ANDESITE.get().getFluidType(), AllBlocks.ANDESITE_ALLOY_BLOCK.getDefaultState());
//        moltenMetalBlocks.put(AZURE_NEODYMIUM.get().getFluidType(), ACBlockRegistry.BLOCK_OF_AZURE_NEODYMIUM.get().defaultBlockState());
//        moltenMetalBlocks.put(SCARLET_NEODYMIUM.get().getFluidType(), ACBlockRegistry.BLOCK_OF_SCARLET_NEODYMIUM.get().defaultBlockState());
//        moltenMetalBlocks.put(DESH.get().getFluidType(), ModBlocks.DESH_BLOCK.get().defaultBlockState());
//        moltenMetalBlocks.put(OSTRUM.get().getFluidType(), ModBlocks.OSTRUM_BLOCK.get().defaultBlockState());
//        moltenMetalBlocks.put(CLAORITE.get().getFluidType(), ModBlocks.CALORITE_BLOCK.get().defaultBlockState());
//        moltenMetalBlocks.put(FIRE_STEEL.get().getFluidType(), IafBlockRegistry.DRAGONSTEEL_FIRE_BLOCK.get().defaultBlockState());
//        moltenMetalBlocks.put(ICE_STEEL.get().getFluidType(), IafBlockRegistry.DRAGONSTEEL_ICE_BLOCK.get().defaultBlockState());
//        moltenMetalBlocks.put(LIGHTNING_STEEL.get().getFluidType(), IafBlockRegistry.DRAGONSTEEL_LIGHTNING_BLOCK.get().defaultBlockState());
//        moltenMetalBlocks.put(IRON.get().getFluidType(), Blocks.IRON_BLOCK.defaultBlockState());
//    }

    private static void addInteraction(FluidType fluidType) {
//        addMoltenMetalInteraction(fluidType, moltenMetalBlocks.get(fluidType));
        if (fluidType.equals(ANDESITE.get().getFluidType())) {
            addMoltenMetalInteraction(fluidType, AllBlocks.ANDESITE_ALLOY_BLOCK.getDefaultState());
        }
        else if (fluidType.equals(AZURE_NEODYMIUM.get().getFluidType())) {
            addMoltenMetalInteraction(fluidType, ACBlockRegistry.BLOCK_OF_AZURE_NEODYMIUM.get().defaultBlockState());
        }
        else if (fluidType.equals(SCARLET_NEODYMIUM.get().getFluidType())) {
            addMoltenMetalInteraction(fluidType, ACBlockRegistry.BLOCK_OF_SCARLET_NEODYMIUM.get().defaultBlockState());
        }
        else if (fluidType.equals(DESH.get().getFluidType())) {
            addMoltenMetalInteraction(fluidType, ModBlocks.DESH_BLOCK.get().defaultBlockState());
        }
        else if (fluidType.equals(OSTRUM.get().getFluidType())) {
            addMoltenMetalInteraction(fluidType, ModBlocks.OSTRUM_BLOCK.get().defaultBlockState());
        }
        else if (fluidType.equals(CLAORITE.get().getFluidType())) {
            addMoltenMetalInteraction(fluidType, ModBlocks.CALORITE_BLOCK.get().defaultBlockState());
        }
        else if (fluidType.equals(FIRE_STEEL.get().getFluidType())) {
            addMoltenMetalInteraction(fluidType, IafBlockRegistry.DRAGONSTEEL_FIRE_BLOCK.get().defaultBlockState());
        }
        else if (fluidType.equals(ICE_STEEL.get().getFluidType())) {
            addMoltenMetalInteraction(fluidType, IafBlockRegistry.DRAGONSTEEL_ICE_BLOCK.get().defaultBlockState());
        }
        else if (fluidType.equals(LIGHTNING_STEEL.get().getFluidType())) {
            addMoltenMetalInteraction(fluidType, IafBlockRegistry.DRAGONSTEEL_LIGHTNING_BLOCK.get().defaultBlockState());
        }
        else if (fluidType.equals(IRON.get().getFluidType())) {
            addMoltenMetalInteraction(fluidType, Blocks.IRON_BLOCK.defaultBlockState());
        }
        else if (fluidType.equals(COPPER.get().getFluidType())) {
            addMoltenMetalInteraction(fluidType, Blocks.COPPER_BLOCK.defaultBlockState());
        }
        else if (fluidType.equals(GOLD.get().getFluidType())) {
            addMoltenMetalInteraction(fluidType, Blocks.GOLD_BLOCK.defaultBlockState());
        }
        else if (fluidType.equals(ZINC.get().getFluidType())) {
            addMoltenMetalInteraction(fluidType, AllBlocks.ZINC_BLOCK.getDefaultState());
        }
        else if (fluidType.equals(SILVER.get().getFluidType())) {
            addMoltenMetalInteraction(fluidType, IafBlockRegistry.SILVER_BLOCK.get().defaultBlockState());
        }
        else if (fluidType.equals(TUNGSTEN.get().getFluidType())) {
            addMoltenMetalInteraction(fluidType, CMBlocks.TUNGSTEN_BLOCK.getDefaultState());
        }
        else if (fluidType.equals(BRASS.get().getFluidType())) {
            addMoltenMetalInteraction(fluidType, AllBlocks.BRASS_BLOCK.getDefaultState());
        }
        else if (fluidType.equals(NETHERITE.get().getFluidType())) {
            addMoltenMetalInteraction(fluidType, Blocks.NETHERITE_BLOCK.defaultBlockState());
        }
        else if (fluidType.equals(STEEL.get().getFluidType())) {
            addMoltenMetalInteraction(fluidType, CMBlocks.STEEL_BLOCK.getDefaultState());
        }
        else if (fluidType.equals(VOID_STEEL.get().getFluidType())) {
            addMoltenMetalInteraction(fluidType, CUBlocks.VOID_STEEL_BLOCK.getDefaultState());
        }
        else {
            throw new IllegalArgumentException("Unknown fluid type: " + fluidType);
        }
    }
}
