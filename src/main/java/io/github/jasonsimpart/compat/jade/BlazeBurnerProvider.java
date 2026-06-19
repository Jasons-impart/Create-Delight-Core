package io.github.jasonsimpart.compat.jade;

import com.simibubi.create.AllItems;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlockEntity;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlockEntity.FuelType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.theme.IThemeHelper;
import snownee.jade.api.ui.IElementHelper;

public enum BlazeBurnerProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    INSTANCE;

    private static final String IS_CREATIVE = "isCreative";
    private static final String FUEL_LEVEL = "fuelLevel";
    private static final String BURN_TIME_REMAINING = "burnTimeRemaining";

    @Override
    public boolean enabledByDefault() {
        return false;
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        CompoundTag data = accessor.getServerData();
        FuelType activeFuel = FuelType.NONE;
        boolean isCreative = data.getBoolean(IS_CREATIVE);

        if (isCreative) {
            HeatLevel heatLevel = BlazeBurnerBlock.getHeatLevelOf(accessor.getBlockState());
            if (heatLevel == HeatLevel.SEETHING) {
                activeFuel = FuelType.SPECIAL;
            } else if (heatLevel.isAtLeast(HeatLevel.FADING)) {
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

        ItemStack item = activeFuel == FuelType.SPECIAL ? AllItems.BLAZE_CAKE.asStack() : new ItemStack(Items.BLAZE_POWDER);
        tooltip.add(IElementHelper.get().smallItem(item));
        if (isCreative) {
            tooltip.append(IThemeHelper.get().info(Component.translatable("jade.infinity")));
        } else {
            tooltip.append(IThemeHelper.get().seconds(data.getInt(BURN_TIME_REMAINING), 20));
        }
    }

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        if (!(accessor.getBlockEntity() instanceof BlazeBurnerBlockEntity burner)) {
            return;
        }

        if (burner.isCreative()) {
            data.putBoolean(IS_CREATIVE, true);
        } else if (burner.getActiveFuel() != FuelType.NONE) {
            data.putInt(FUEL_LEVEL, burner.getActiveFuel().ordinal());
            data.putInt(BURN_TIME_REMAINING, burner.getRemainingBurnTime());
        }
    }

    @Override
    public ResourceLocation getUid() {
        return CreateDelightJadePlugin.BLAZE_BURNER;
    }
}
