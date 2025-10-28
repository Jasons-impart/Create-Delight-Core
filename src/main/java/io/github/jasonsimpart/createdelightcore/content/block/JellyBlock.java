package io.github.jasonsimpart.createdelightcore.content.block;

import io.github.jasonsimpart.createdelightcore.CreateDelightCore;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.HoneyBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class JellyBlock extends HoneyBlock {

    public final String fruit;

    public JellyBlock(Properties properties, String fruit) {
        super(properties);
        this.fruit = fruit;
    }

    @Override
    public boolean isStickyBlock(BlockState state) {
        return true;
    }

    @Override
    public boolean canStickTo(BlockState state, BlockState other) {
        if (other.getBlock() instanceof JelloBlock jello) {
            return Objects.equals(jello.fruit, fruit);
        }
        if (other.isStickyBlock() && other.getBlock() != this)
            return false;
        return super.canStickTo(state, other);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> list, TooltipFlag flag) {
        list.add(Component.translatable("tooltip." + CreateDelightCore.MODID + ".jelly_block").withStyle(ChatFormatting.GRAY));
    }

}
