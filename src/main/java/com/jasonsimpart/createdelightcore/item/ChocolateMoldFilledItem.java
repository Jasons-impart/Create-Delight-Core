package com.jasonsimpart.createdelightcore.item;

import com.simibubi.create.foundation.utility.Color;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

public class ChocolateMoldFilledItem extends Item {
    public static final int MAX_CHILLNESS = 50;

    public final ItemStack itemSolid;

    public ChocolateMoldFilledItem(Properties pProperties, ItemStack itemSolid) {
        super(pProperties);
        this.itemSolid = itemSolid;
    }

    public int getChillness(ItemStack stack) {
        return stack.getOrCreateTag().getInt("CollectingChillness");
    }

    public boolean isBarVisible(ItemStack stack) {
        return this.getChillness(stack) > 0;
    }

    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0F * (float)this.getChillness(stack) / MAX_CHILLNESS);
    }

    public int getBarColor(ItemStack stack) {
        return Color.mixColors(4275305, 16777215, (float)this.getChillness(stack) / MAX_CHILLNESS);
    }

    public boolean onEntityItemUpdate(ItemStack stack, @NotNull ItemEntity entity) {
        Level level = entity.getLevel();
        CompoundTag itemData = entity.getItem().getOrCreateTag();
        if (itemData.getInt("CollectingChillness") >= MAX_CHILLNESS) {
            ItemStack newStack = itemSolid.copy();
            ItemEntity newEntity = new ItemEntity(level, entity.getX(), entity.getY(), entity.getZ(), newStack);
            newEntity.setDeltaMovement(entity.getDeltaMovement());
            itemData.remove("CollectingChillness");
            level.addFreshEntity(newEntity);
            stack.split(1);
            entity.setItem(stack);
            if (stack.isEmpty()) {
                entity.discard();
            }

            return false;
        } else {
            final Block FROZEN_BLOCK = ForgeRegistries.BLOCKS.getValue(new ResourceLocation("ratatouille", "frozen_block"));
            BlockPos entityPos = entity.getOnPos();

            for (BlockPos blockPos : BlockPos.betweenClosed(entityPos.offset(-1, -1, -1), entityPos.offset(1, 1, 1))) {
                if (level.getBlockState(blockPos).is(FROZEN_BLOCK)) {
                    stack.getOrCreateTag().putInt("CollectingChillness", itemData.getInt("CollectingChillness") + 1);
                }
            }

            return false;
        }
    }
}
