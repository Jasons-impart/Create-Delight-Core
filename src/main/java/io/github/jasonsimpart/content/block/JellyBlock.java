package io.github.jasonsimpart.content.block;

import io.github.jasonsimpart.CreateDelightCore;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HoneyBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Objects;

public class JellyBlock extends HoneyBlock {
    private static final double SLIDE_STARTS_WHEN_VERTICAL_SPEED_IS_AT_LEAST = 0.13;
    private static final double MIN_FALL_SPEED_TO_BE_CONSIDERED_SLIDING = 0.08;
    private static final double THROTTLE_SLIDE_SPEED_TO = 0.05;

    private final String fruit;

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
            return Objects.equals(jello.fruit(), fruit);
        }
        if (other.isStickyBlock() && other.getBlock() != this) {
            return false;
        }
        return super.canStickTo(state, other);
    }

    public String fruit() {
        return fruit;
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (isSlidingDown(pos, entity)) {
            doSlideMovement(entity);
            maybeDoSlideEffects(state, level, entity);
        }
    }

    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
        entity.playSound(SoundEvents.HONEY_BLOCK_SLIDE, 1.0F, 1.0F);
        if (level.isClientSide) {
            showParticles(level, state, entity, 10);
        }

        if (entity.causeFallDamage(fallDistance, 0.2F, level.damageSources().fall())) {
            entity.playSound(soundType.getFallSound(), soundType.getVolume() * 0.5F, soundType.getPitch() * 0.75F);
        }
    }

    private boolean isSlidingDown(BlockPos pos, Entity entity) {
        if (entity.onGround()) {
            return false;
        }
        if (entity.getY() > (double) pos.getY() + 0.9375 - 1.0E-7) {
            return false;
        }
        if (entity.getDeltaMovement().y >= -MIN_FALL_SPEED_TO_BE_CONSIDERED_SLIDING) {
            return false;
        }

        double xDistance = Math.abs((double) pos.getX() + 0.5 - entity.getX());
        double zDistance = Math.abs((double) pos.getZ() + 0.5 - entity.getZ());
        double slideDistance = 0.4375 + entity.getBbWidth() / 2.0F;
        return xDistance + 1.0E-7 > slideDistance || zDistance + 1.0E-7 > slideDistance;
    }

    private void doSlideMovement(Entity entity) {
        Vec3 movement = entity.getDeltaMovement();
        if (movement.y < -SLIDE_STARTS_WHEN_VERTICAL_SPEED_IS_AT_LEAST) {
            double multiplier = -THROTTLE_SLIDE_SPEED_TO / movement.y;
            entity.setDeltaMovement(new Vec3(movement.x * multiplier, -THROTTLE_SLIDE_SPEED_TO, movement.z * multiplier));
        } else {
            entity.setDeltaMovement(new Vec3(movement.x, -THROTTLE_SLIDE_SPEED_TO, movement.z));
        }
        entity.resetFallDistance();
    }

    private void maybeDoSlideEffects(BlockState state, Level level, Entity entity) {
        if (doesEntityDoJellySlideEffects(entity)) {
            if (level.random.nextInt(5) == 0) {
                entity.playSound(SoundEvents.HONEY_BLOCK_SLIDE, 1.0F, 1.0F);
            }
            if (level.isClientSide && level.random.nextInt(5) == 0) {
                showParticles(level, state, entity, 5);
            }
        }
    }

    private static boolean doesEntityDoJellySlideEffects(Entity entity) {
        return entity instanceof LivingEntity || entity instanceof AbstractMinecart || entity instanceof PrimedTnt || entity instanceof Boat;
    }

    private static void showParticles(Level level, BlockState state, Entity entity, int particleCount) {
        for (int i = 0; i < particleCount; i++) {
            level.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, state), entity.getX(), entity.getY(), entity.getZ(), 0.0D, 0.0D, 0.0D);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip." + CreateDelightCore.MODID + ".jelly_block").withStyle(ChatFormatting.GRAY));
    }
}
