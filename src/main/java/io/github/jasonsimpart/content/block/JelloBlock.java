package io.github.jasonsimpart.content.block;

import io.github.jasonsimpart.CreateDelightCore;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SlimeBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.Objects;

public class JelloBlock extends SlimeBlock {
    private final String fruit;

    public JelloBlock(Properties properties, String fruit) {
        super(properties);
        this.fruit = fruit;
    }

    @Override
    public boolean isSlimeBlock(BlockState state) {
        return true;
    }

    @Override
    public boolean isStickyBlock(BlockState state) {
        return true;
    }

    @Override
    public boolean canStickTo(BlockState state, BlockState other) {
        if (other.getBlock() instanceof JellyBlock jelly) {
            return Objects.equals(jelly.fruit(), fruit);
        }
        if (other.getBlock() instanceof JelloBlock jello) {
            return jello == this;
        }
        return false;
    }

    public String fruit() {
        return fruit;
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip." + CreateDelightCore.MODID + ".jello_block").withStyle(ChatFormatting.GRAY));
    }
}
