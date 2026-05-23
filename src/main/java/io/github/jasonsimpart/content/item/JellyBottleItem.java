package io.github.jasonsimpart.content.item;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.List;

public class JellyBottleItem extends BlockItem {
    private final List<OptionalEffectFoodItem.OptionalEffect> optionalEffects;

    public JellyBottleItem(Block block, Properties properties) {
        this(block, properties, List.of());
    }

    public JellyBottleItem(Block block, Properties properties, List<OptionalEffectFoodItem.OptionalEffect> optionalEffects) {
        super(block, properties);
        this.optionalEffects = List.copyOf(optionalEffects);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        super.finishUsingItem(stack, level, entity);
        if (entity instanceof ServerPlayer serverPlayer) {
            CriteriaTriggers.CONSUME_ITEM.trigger(serverPlayer, stack);
            serverPlayer.awardStat(Stats.ITEM_USED.get(this));
        }

        if (!level.isClientSide && !optionalEffects.isEmpty()) {
            Registry<MobEffect> effects = level.registryAccess().registryOrThrow(Registries.MOB_EFFECT);
            optionalEffects.forEach(optionalEffect -> effects.getHolder(optionalEffect.key())
                    .ifPresent(effect -> entity.addEffect(new MobEffectInstance(effect, optionalEffect.duration(), optionalEffect.amplifier()))));
        }

        if (stack.isEmpty()) {
            return new ItemStack(Items.GLASS_BOTTLE);
        }
        if (entity instanceof Player player && !player.getAbilities().instabuild) {
            ItemStack bottle = new ItemStack(Items.GLASS_BOTTLE);
            if (!player.getInventory().add(bottle)) {
                player.drop(bottle, false);
            }
        }
        return stack;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

    @Override
    public SoundEvent getDrinkingSound() {
        return SoundEvents.HONEY_DRINK;
    }

    @Override
    public SoundEvent getEatingSound() {
        return SoundEvents.HONEY_DRINK;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        OptionalEffectFoodItem.appendOptionalEffectTooltip(optionalEffects, context, tooltip);
    }

    @Override
    public InteractionResult place(BlockPlaceContext context) {
        if (context.getPlayer() != null && !context.getPlayer().isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        return super.place(context);
    }
}
