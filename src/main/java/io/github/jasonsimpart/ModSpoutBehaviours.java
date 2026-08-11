package io.github.jasonsimpart;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.api.behaviour.spouting.BlockSpoutingBehaviour;
import com.simibubi.create.content.fluids.spout.SpoutBlockEntity;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.List;
import java.util.Optional;

public final class ModSpoutBehaviours {
    private static final int CREATE_SA_GADGET_CAPACITY = 1600;
    private static final String CREATE_SA_WATER_TAG = "tagWater";
    private static final String CREATE_SA_FUEL_TAG = "tagFuel";

    private static final ResourceLocation CREATE_SA_FILLABLE = id("create_sa", "fillable");
    private static final ResourceLocation CREATE_SA_FUELABLE = id("create_sa", "fuelable");
    private static final ResourceLocation SPENT_LIQUOR = id(CreateDelightCore.MODID, "spent_liquor");
    private static final ResourceLocation FUEL_MIXTURES = id(CreateDelightCore.MODID, "fuel_mixtures");
    private static final ResourceLocation FLOWING_FUEL_MIXTURES = id(CreateDelightCore.MODID, "flowing_fuel_mixtures");
    private static final ResourceLocation ALEX_CAVES_ACID = id("alexscavesup", "acid");
    private static final ResourceLocation ALEX_CAVES_FLOWING_ACID = id("alexscavesup", "flowing_acid");

    private static final TagKey<Item> FILLABLE_ITEMS = TagKey.create(Registries.ITEM, CREATE_SA_FILLABLE);
    private static final TagKey<Item> FUELABLE_ITEMS = TagKey.create(Registries.ITEM, CREATE_SA_FUELABLE);

    private static final List<ResourceLocation> AE2_GROWABLE_BUDDING_QUARTZ = List.of(
            id("ae2", "flawless_budding_quartz"),
            id("ae2", "flawed_budding_quartz"),
            id("ae2", "chipped_budding_quartz"),
            id("ae2", "damaged_budding_quartz")
    );
    private static final List<ResourceLocation> AE2_REPAIRABLE_QUARTZ = List.of(
            id("ae2", "quartz_block"),
            id("ae2", "damaged_budding_quartz"),
            id("ae2", "chipped_budding_quartz")
    );

    private ModSpoutBehaviours() {
    }

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(ModSpoutBehaviours::commonSetup);
    }

    private static void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(ModSpoutBehaviours::registerSpoutBehaviours);
    }

    private static void registerSpoutBehaviours() {
        if (Config.ENABLE_CREATE_SA_SPOUT_FILLING.get()) {
            registerBlockEntity(AllBlockEntityTypes.DEPOT.get(), ModSpoutBehaviours::fillCreateSaGadget);
            registerBlockEntity(AllBlockEntityTypes.WEIGHTED_EJECTOR.get(), ModSpoutBehaviours::fillCreateSaGadget);
        }

        if (Config.ENABLE_AE2_SPENT_LIQUOR_SPOUT.get()) {
            AE2_GROWABLE_BUDDING_QUARTZ.forEach(blockId -> registerBlock(blockId, ModSpoutBehaviours::fillAe2Quartz));
            AE2_REPAIRABLE_QUARTZ.forEach(blockId -> registerBlock(blockId, ModSpoutBehaviours::fillAe2Quartz));
        }

        if (Config.ENABLE_ALEXSCAVES_SULFUR_SPOUT.get()) {
            registerBlock(id("alexscavesup", "sulfur"), ModSpoutBehaviours::fillSulfur);
        }
    }

    private static int fillCreateSaGadget(Level level, BlockPos pos, SpoutBlockEntity spout, FluidStack availableFluid, boolean simulate) {
        IItemHandler handler = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, null);
        if (handler == null) {
            return 0;
        }

        for (int slot = 0; slot < handler.getSlots(); slot++) {
            ItemStack stack = handler.getStackInSlot(slot);
            if (stack.isEmpty()) {
                continue;
            }

            int consumed = fillCreateSaGadgetStack(stack, availableFluid, simulate);
            if (consumed <= 0) {
                continue;
            }

            if (!simulate) {
                markChanged(level, pos);
            }
            return consumed;
        }

        return 0;
    }

    private static int fillCreateSaGadgetStack(ItemStack stack, FluidStack availableFluid, boolean simulate) {
        Fluid fluid = availableFluid.getFluid();
        if (fluid.isSame(Fluids.WATER) && stack.is(FILLABLE_ITEMS)) {
            return fillStock(stack, CREATE_SA_WATER_TAG, CREATE_SA_GADGET_CAPACITY, availableFluid.getAmount(), 1.0D, simulate);
        }

        double fuelMultiplier = fuelMultiplier(fluid);
        if (fuelMultiplier <= 0 || stack.is(FILLABLE_ITEMS) && fluid.isSame(Fluids.WATER) || !stack.is(FUELABLE_ITEMS)) {
            return 0;
        }

        return fillStock(stack, CREATE_SA_FUEL_TAG, CREATE_SA_GADGET_CAPACITY, availableFluid.getAmount(), fuelMultiplier, simulate);
    }

    private static int fillStock(ItemStack stack, String key, int capacity, int availableFluid, double fluidPerStockMultiplier, boolean simulate) {
        if (availableFluid <= 0) {
            return 0;
        }

        double current = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getDouble(key);
        int remainingStock = capacity - Mth.floor(current);
        if (remainingStock <= 0) {
            return 0;
        }

        int stockByFluid = Mth.floor(availableFluid / (10.0D * fluidPerStockMultiplier));
        int stockToInsert = Math.min(remainingStock, stockByFluid);
        if (stockToInsert <= 0) {
            return 0;
        }

        if (!simulate) {
            double newStock = Math.min(capacity, current + stockToInsert);
            CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putDouble(key, newStock));
        }

        return Mth.ceil(stockToInsert * 10.0D * fluidPerStockMultiplier);
    }

    private static double fuelMultiplier(Fluid fluid) {
        if (fluid.isSame(Fluids.LAVA)) {
            return 1.0D;
        }

        ResourceLocation fluidId = BuiltInRegistries.FLUID.getKey(fluid);
        if (id("createdieselgenerators", "diesel").equals(fluidId) || id("createdieselgenerators", "biodiesel").equals(fluidId)) {
            return 0.5D;
        }
        if (id("createdieselgenerators", "gasoline").equals(fluidId)) {
            return 0.4D;
        }
        if (FUEL_MIXTURES.equals(fluidId) || FLOWING_FUEL_MIXTURES.equals(fluidId)) {
            return 0.25D;
        }
        return 0.0D;
    }

    private static int fillAe2Quartz(Level level, BlockPos pos, SpoutBlockEntity spout, FluidStack availableFluid, boolean simulate) {
        if (availableFluid.getAmount() < 50 || !hasFluidId(availableFluid, SPENT_LIQUOR)) {
            return 0;
        }

        BlockState state = level.getBlockState(pos);
        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        boolean canGrow = AE2_GROWABLE_BUDDING_QUARTZ.contains(blockId);
        boolean canRepair = AE2_REPAIRABLE_QUARTZ.contains(blockId);
        if (!canGrow && !canRepair) {
            return 0;
        }

        if (!simulate && level instanceof ServerLevel serverLevel) {
            if (canGrow) {
                for (int i = 0; i < 3; i++) {
                    state.randomTick(serverLevel, pos, serverLevel.random);
                }
            }

            if (canRepair && serverLevel.random.nextInt(4) == 0) {
                repairAe2Quartz(serverLevel, pos, blockId);
            }
        }

        return 50;
    }

    private static void repairAe2Quartz(ServerLevel level, BlockPos pos, ResourceLocation currentBlockId) {
        ResourceLocation replacementId = switch (currentBlockId.toString()) {
            case "ae2:quartz_block" -> id("ae2", "damaged_budding_quartz");
            case "ae2:damaged_budding_quartz" -> id("ae2", "chipped_budding_quartz");
            case "ae2:chipped_budding_quartz" -> id("ae2", "flawed_budding_quartz");
            default -> null;
        };
        if (replacementId == null) {
            return;
        }

        BuiltInRegistries.BLOCK.getOptional(replacementId)
                .map(Block::defaultBlockState)
                .ifPresent(newState -> level.setBlockAndUpdate(pos, newState));
    }

    private static int fillSulfur(Level level, BlockPos pos, SpoutBlockEntity spout, FluidStack availableFluid, boolean simulate) {
        if (availableFluid.getAmount() < 10 || !hasFluidId(availableFluid, ALEX_CAVES_ACID, ALEX_CAVES_FLOWING_ACID)) {
            return 0;
        }

        BlockPos budPos = pos.above();
        BlockState budState = level.getBlockState(budPos);
        ResourceLocation budId = BuiltInRegistries.BLOCK.getKey(budState.getBlock());
        if (id("alexscavesup", "sulfur_cluster").equals(budId)) {
            return 0;
        }

        Optional<ResourceLocation> nextBudId = nextSulfurBud(budState);
        if (nextBudId.isEmpty()) {
            return 0;
        }

        if (!simulate && level.random.nextInt(3) == 0) {
            BuiltInRegistries.BLOCK.getOptional(nextBudId.get())
                    .map(Block::defaultBlockState)
                    .map(newState -> newState.hasProperty(BlockStateProperties.FACING)
                            ? newState.setValue(BlockStateProperties.FACING, Direction.UP)
                            : newState)
                    .ifPresent(newState -> level.setBlockAndUpdate(budPos, newState));
        }

        return 10;
    }

    private static Optional<ResourceLocation> nextSulfurBud(BlockState budState) {
        if (budState.isAir()) {
            return Optional.of(id("alexscavesup", "sulfur_bud_small"));
        }

        ResourceLocation currentId = BuiltInRegistries.BLOCK.getKey(budState.getBlock());
        return switch (currentId.toString()) {
            case "alexscavesup:sulfur_bud_small" -> Optional.of(id("alexscavesup", "sulfur_bud_medium"));
            case "alexscavesup:sulfur_bud_medium" -> Optional.of(id("alexscavesup", "sulfur_bud_large"));
            case "alexscavesup:sulfur_bud_large" -> Optional.of(id("alexscavesup", "sulfur_cluster"));
            default -> Optional.empty();
        };
    }

    private static boolean hasFluidId(FluidStack stack, ResourceLocation... expectedIds) {
        ResourceLocation fluidId = BuiltInRegistries.FLUID.getKey(stack.getFluid());
        for (ResourceLocation expectedId : expectedIds) {
            if (expectedId.equals(fluidId)) {
                return true;
            }
        }
        return false;
    }

    private static void registerBlock(ResourceLocation blockId, BlockSpoutingBehaviour behaviour) {
        BuiltInRegistries.BLOCK.getOptional(blockId).ifPresent(block -> {
            if (block != Blocks.AIR && BlockSpoutingBehaviour.BY_BLOCK.get(block) == null) {
                BlockSpoutingBehaviour.BY_BLOCK.register(block, behaviour);
            }
        });
    }

    private static void registerBlockEntity(BlockEntityType<?> type, BlockSpoutingBehaviour behaviour) {
        if (BlockSpoutingBehaviour.BY_BLOCK_ENTITY.get(type) == null) {
            BlockSpoutingBehaviour.BY_BLOCK_ENTITY.register(type, behaviour);
        }
    }

    private static void markChanged(Level level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof SmartBlockEntity smartBlockEntity) {
            smartBlockEntity.notifyUpdate();
        } else if (blockEntity != null) {
            blockEntity.setChanged();
        }
    }

    private static ResourceLocation id(String namespace, String path) {
        return ResourceLocation.fromNamespaceAndPath(namespace, path);
    }
}
