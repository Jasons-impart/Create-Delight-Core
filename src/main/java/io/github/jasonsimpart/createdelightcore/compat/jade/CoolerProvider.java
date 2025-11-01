package io.github.jasonsimpart.createdelightcore.compat.jade;

import fr.iglee42.cmr.cooler.SnowmanCoolerBlock;
import fr.iglee42.cmr.cooler.SnowmanCoolerBlock.HeatLevel;
import fr.iglee42.cmr.cooler.SnowmanCoolerBlockEntity;
import fr.iglee42.cmr.cooler.SnowmanCoolerBlockEntity.FuelType;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.theme.IThemeHelper;
import snownee.jade.api.ui.IElementHelper;

public enum CoolerProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    INSTANCE;

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        CompoundTag compound = accessor.getServerData();
        FuelType activeFuel = FuelType.NONE;
        boolean isCreative = compound.getBoolean("isCreative");
        if(isCreative) {
            HeatLevel heatLevel = SnowmanCoolerBlock.getHeatLevelOf(accessor.getBlockState());
            if (heatLevel == HeatLevel.FREEZING) {
                activeFuel = FuelType.SPECIAL;
            } else if (heatLevel != HeatLevel.IDLE) {
                activeFuel = FuelType.NORMAL;
            }
        } else {
            activeFuel = FuelType.values()[compound.getInt("fuelLevel")];
        }
        if (activeFuel == FuelType.NONE) {
            return;
        }
        ItemStack item = new ItemStack(activeFuel == FuelType.SPECIAL ? Items.BLUE_ICE : Items.SNOWBALL);
        tooltip.add(IElementHelper.get().smallItem(item));
        if (isCreative) {
            tooltip.append(IThemeHelper.get().info(Component.translatable("jade.infinity")));
        } else {
            tooltip.append(IThemeHelper.get().seconds(compound.getInt("burnTimeRemaining")));
        }

    }

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        SnowmanCoolerBlockEntity cooler = (SnowmanCoolerBlockEntity) accessor.getBlockEntity();
        if (cooler.isCreative()) {
            data.putBoolean("isCreative", true);
        } else if (cooler.getActiveFuel() != FuelType.NONE) {
            data.putInt("fuelLevel", cooler.getActiveFuel().ordinal());
            data.putInt("burnTimeRemaining", cooler.getRemainingBurnTime());
        }
    }

    @Override
    public ResourceLocation getUid() {
        return CDPlugin.SNOWMAN_COOLER;
    }
}
