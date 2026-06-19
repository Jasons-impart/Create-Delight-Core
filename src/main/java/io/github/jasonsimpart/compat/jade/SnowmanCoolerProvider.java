package io.github.jasonsimpart.compat.jade;

import fr.iglee42.cmr.cooler.SnowmanCoolerBlock;
import fr.iglee42.cmr.cooler.SnowmanCoolerBlock.HeatLevel;
import fr.iglee42.cmr.cooler.SnowmanCoolerBlockEntity;
import fr.iglee42.cmr.cooler.SnowmanCoolerBlockEntity.FuelType;
import io.github.jasonsimpart.compat.cmr.CoolerStomachAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.fluid.JadeFluidObject;
import snownee.jade.api.theme.IThemeHelper;
import snownee.jade.api.ui.IElementHelper;

public enum SnowmanCoolerProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    INSTANCE;

    private static final String IS_CREATIVE = "isCreative";
    private static final String FUEL_LEVEL = "fuelLevel";
    private static final String BURN_TIME_REMAINING = "burnTimeRemaining";
    private static final String FLUID = "fluid";
    private static final String FLUID_AMOUNT = "fluidAmount";
    private static final String FLUID_CAPACITY = "fluidCapacity";

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        CompoundTag data = accessor.getServerData();
        appendFuelTooltip(tooltip, accessor, data);
        appendFluidTooltip(tooltip, data);
    }

    private static void appendFuelTooltip(ITooltip tooltip, BlockAccessor accessor, CompoundTag data) {
        FuelType activeFuel = FuelType.NONE;
        boolean isCreative = data.getBoolean(IS_CREATIVE);
        if (isCreative) {
            HeatLevel heatLevel = SnowmanCoolerBlock.getHeatLevelOf(accessor.getBlockState());
            if (heatLevel == HeatLevel.FREEZING) {
                activeFuel = FuelType.SPECIAL;
            } else if (heatLevel != HeatLevel.IDLE) {
                activeFuel = FuelType.NORMAL;
            }
        } else if (data.contains(FUEL_LEVEL)) {
            int ordinal = data.getInt(FUEL_LEVEL);
            FuelType[] values = FuelType.values();
            if (ordinal >= 0 && ordinal < values.length) {
                activeFuel = values[ordinal];
            }
        }

        if (activeFuel == FuelType.NONE) {
            return;
        }

        ItemStack item = new ItemStack(activeFuel == FuelType.SPECIAL ? Items.BLUE_ICE : Items.SNOWBALL);
        tooltip.add(IElementHelper.get().smallItem(item));
        if (isCreative) {
            tooltip.append(IThemeHelper.get().info(Component.translatable("jade.infinity")));
        } else {
            tooltip.append(IThemeHelper.get().seconds(data.getInt(BURN_TIME_REMAINING), 20));
        }
    }

    private static void appendFluidTooltip(ITooltip tooltip, CompoundTag data) {
        if (!data.contains(FLUID) || data.getInt(FLUID_AMOUNT) <= 0) {
            return;
        }

        ResourceLocation fluidId = ResourceLocation.tryParse(data.getString(FLUID));
        Fluid fluid = fluidId == null ? Fluids.EMPTY : BuiltInRegistries.FLUID.get(fluidId);
        if (fluid == Fluids.EMPTY) {
            return;
        }

        int amount = data.getInt(FLUID_AMOUNT);
        int capacity = Math.max(amount, data.getInt(FLUID_CAPACITY));
        tooltip.add(IElementHelper.get().fluid(JadeFluidObject.of(fluid, amount)));
        tooltip.append(IThemeHelper.get().info(Component.literal(amount + " / " + capacity + " mB")));
    }

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        if (!(accessor.getBlockEntity() instanceof SnowmanCoolerBlockEntity cooler)) {
            return;
        }

        if (cooler.isCreative()) {
            data.putBoolean(IS_CREATIVE, true);
        } else if (cooler.getActiveFuel() != FuelType.NONE) {
            data.putInt(FUEL_LEVEL, cooler.getActiveFuel().ordinal());
            data.putInt(BURN_TIME_REMAINING, cooler.getRemainingBurnTime());
        }

        if (cooler instanceof CoolerStomachAccess stomachAccess && stomachAccess.createdelightcore$getStomach() != null) {
            FluidStack fluidStack = stomachAccess.createdelightcore$getStomach().getFluid();
            if (!fluidStack.isEmpty()) {
                data.putString(FLUID, BuiltInRegistries.FLUID.getKey(fluidStack.getFluid()).toString());
                data.putInt(FLUID_AMOUNT, fluidStack.getAmount());
                data.putInt(FLUID_CAPACITY, stomachAccess.createdelightcore$getStomach().getCapacity());
            }
        }
    }

    @Override
    public ResourceLocation getUid() {
        return CreateDelightJadePlugin.SNOWMAN_COOLER;
    }
}
