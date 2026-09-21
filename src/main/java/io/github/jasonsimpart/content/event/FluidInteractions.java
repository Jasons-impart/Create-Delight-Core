package io.github.jasonsimpart.content.event;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.decoration.palettes.AllPaletteStoneTypes;
import fr.lucreeper74.createmetallurgy.registries.CMBlocks;
import fr.lucreeper74.createmetallurgy.registries.CMFluids;
import io.github.jasonsimpart.CreateDelightCore;
import io.github.jasonsimpart.registry.ModBlocks;
import io.github.jasonsimpart.registry.ModFluids;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.fluids.FluidInteractionRegistry;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public final class FluidInteractions {
    private static final Map<FluidType, Supplier<BlockState>> MOLTEN_SOURCE_BLOCKS = new HashMap<>();
    private static boolean initialized;

    private FluidInteractions() {
    }

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(FluidInteractions::commonSetup);
    }

    private static void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(FluidInteractions::registerInteractions);
    }

    private static void registerInteractions() {
        initializeMappings();
        MOLTEN_SOURCE_BLOCKS.forEach((fluidType, sourceBlock) -> {
            if (sourceBlock.get() != null) {
                addWaterInteraction(fluidType, sourceBlock);
                return;
            }

            CreateDelightCore.LOGGER.info("Falling back to default molten fluid interaction because target block is unavailable: {}", NeoForgeRegistries.FLUID_TYPES.getKey(fluidType));
            addFallbackWaterInteraction(fluidType);
        });
        registerMoltenFallbacks();
    }

    private static void registerMoltenFallbacks() {
        for (var fluidEntry : CMFluids.ALL_MOLTEN_FLUIDS) {
            FluidType fluidType = fluidEntry.getType();
            if (MOLTEN_SOURCE_BLOCKS.containsKey(fluidType)) {
                continue;
            }

            addFallbackWaterInteraction(fluidType);
        }
    }

    private static void addFallbackWaterInteraction(FluidType fluidType) {
        FluidInteractionRegistry.addInteraction(fluidType, new FluidInteractionRegistry.InteractionInformation(
                NeoForgeMod.WATER_TYPE.value(),
                fluidState -> fluidState.isSource()
                        ? CMBlocks.SLAG_BLOCK.get().defaultBlockState()
                        : Blocks.COBBLESTONE.defaultBlockState()
        ));
    }

    private static void initializeMappings() {
        if (initialized) {
            return;
        }

        initialized = true;
        add(ModFluids.MOLTEN_ANDESITE.fluidType().get(), AllBlocks.ANDESITE_ALLOY_BLOCK::getDefaultState);
        add(ModFluids.MOLTEN_FORGED_STEEL.fluidType().get(), () -> ModBlocks.FORGED_STEEL_BLOCK.get().defaultBlockState());
        add(ModFluids.MOLTEN_GLASS.fluidType().get(), Blocks.GLASS::defaultBlockState);
        add(ModFluids.MOLTEN_TITANIUM.fluidType().get(), blockSupplier("northstar", "titanium_block"));
        add(ModFluids.MOLTEN_MARTIAN_STEEL.fluidType().get(), blockSupplier("northstar", "martian_steel_block"));
        add(ModFluids.MOLTEN_FIRE_STEEL.fluidType().get(), blockSupplier("iceandfire", "dragonsteel_fire_block"));
        add(ModFluids.MOLTEN_ICE_STEEL.fluidType().get(), blockSupplier("iceandfire", "dragonsteel_ice_block"));
        add(ModFluids.MOLTEN_LIGHTNING_STEEL.fluidType().get(), blockSupplier("iceandfire", "dragonsteel_lightning_block"));
        add(ModFluids.MOLTEN_AZURE_NEODYMIUM.fluidType().get(), blockSupplier("alexscavesup", "block_of_azure_neodymium"));
        add(ModFluids.MOLTEN_SCARLET_NEODYMIUM.fluidType().get(), blockSupplier("alexscavesup", "block_of_scarlet_neodymium"));
        add(ModFluids.MOLTEN_QUARTZ_GLASS.fluidType().get(), blockSupplier("ae2", "quartz_glass"));
        add(ModFluids.MOLTEN_QUARTZ_VIBRANT_GLASS.fluidType().get(), blockSupplier("ae2", "quartz_vibrant_glass"));

        add(CMFluids.MOLTEN_IRON.get().getFluidType(), Blocks.IRON_BLOCK::defaultBlockState);
        add(CMFluids.MOLTEN_COPPER.get().getFluidType(), Blocks.COPPER_BLOCK::defaultBlockState);
        add(CMFluids.MOLTEN_GOLD.get().getFluidType(), Blocks.GOLD_BLOCK::defaultBlockState);
        add(CMFluids.MOLTEN_ZINC.get().getFluidType(), AllBlocks.ZINC_BLOCK::getDefaultState);
        add(CMFluids.MOLTEN_TIN.get().getFluidType(), () -> ModBlocks.TIN_BLOCK.get().defaultBlockState());
        add(CMFluids.MOLTEN_SILVER.get().getFluidType(), blockSupplier("iceandfire", "silver_block"));
        add(CMFluids.MOLTEN_TUNGSTEN.get().getFluidType(), CMBlocks.TUNGSTEN_BLOCK::getDefaultState);
        add(CMFluids.MOLTEN_BRASS.get().getFluidType(), AllBlocks.BRASS_BLOCK::getDefaultState);
        add(CMFluids.MOLTEN_BRONZE.get().getFluidType(), () -> ModBlocks.BRONZE_BLOCK.get().defaultBlockState());
        add(CMFluids.MOLTEN_ELECTRUM.get().getFluidType(), blockSupplier("createaddition", "electrum_block"));
        add(CMFluids.MOLTEN_NETHERITE.get().getFluidType(), Blocks.NETHERITE_BLOCK::defaultBlockState);
        add(CMFluids.MOLTEN_STEEL.get().getFluidType(), CMBlocks.STEEL_BLOCK::getDefaultState);
        add(CMFluids.MOLTEN_VOID_STEEL.get().getFluidType(), blockSupplier("createutilities", "void_steel_block"));
        add(CMFluids.MOLTEN_OBDURIUM.get().getFluidType(), CMBlocks.OBDURIUM_BLOCK::getDefaultState);
        add(CMFluids.MOLTEN_SLAG.get().getFluidType(), Blocks.TUFF::defaultBlockState);
    }

    private static void add(FluidType fluidType, Supplier<BlockState> blockState) {
        MOLTEN_SOURCE_BLOCKS.put(fluidType, blockState);
    }

    private static Supplier<BlockState> blockSupplier(String modId, String blockName) {
        ResourceLocation blockId = ResourceLocation.fromNamespaceAndPath(modId, blockName);
        return () -> BuiltInRegistries.BLOCK.getOptional(blockId).map(Block::defaultBlockState).orElse(null);
    }

    private static void addWaterInteraction(FluidType fluidType, Supplier<BlockState> sourceBlock) {
        FluidInteractionRegistry.addInteraction(fluidType, new FluidInteractionRegistry.InteractionInformation(
                NeoForgeMod.WATER_TYPE.value(),
                fluidState -> resolveInteractionState(fluidState, sourceBlock)
        ));
    }

    private static BlockState resolveInteractionState(FluidState fluidState, Supplier<BlockState> sourceBlock) {
        if (!fluidState.isSource()) {
            return AllPaletteStoneTypes.SCORCHIA.getBaseBlock().get().defaultBlockState();
        }

        return sourceBlock.get();
    }
}
