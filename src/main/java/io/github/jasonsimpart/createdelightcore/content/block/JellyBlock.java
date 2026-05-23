package io.github.jasonsimpart.createdelightcore.content.block;

import io.github.jasonsimpart.createdelightcore.CreateDelightCore;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
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

    public void showJellySlideParticles(Entity entity) {
        showJellyParticles(entity, 5);
    }

    public void showJellyJumpParticles(Entity entity) {
        showJellyParticles(entity, 10);
    }

    private void showJellyParticles(Entity entity, int count) {
        Level level = entity.level();
        BlockParticleOption particle = new BlockParticleOption(ParticleTypes.BLOCK, defaultBlockState());
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(particle, entity.getX(), entity.getY(), entity.getZ(), count, 0.0D, 0.0D, 0.0D, 0.0D);
            return;
        }
        if (!level.isClientSide) {
            return;
        }
        for (int i = 0; i < count; i++) {
            level.addParticle(particle, entity.getX(), entity.getY(), entity.getZ(), 0.0D, 0.0D, 0.0D);
        }
    }

}
