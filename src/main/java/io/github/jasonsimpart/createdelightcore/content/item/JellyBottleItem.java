package io.github.jasonsimpart.createdelightcore.content.item;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public class JellyBottleItem extends BlockItem {

    public JellyBottleItem(Block pBlock, Properties pProperties) {
        super(pBlock, pProperties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level worldIn, LivingEntity entity) {
        super.finishUsingItem(stack, worldIn, entity);
        if (entity instanceof ServerPlayer serverPlayer) {
            CriteriaTriggers.CONSUME_ITEM.trigger(serverPlayer, stack);
            serverPlayer.awardStat(Stats.ITEM_USED.get(this));
        }

        if (stack.isEmpty()) {
            return new ItemStack(Items.GLASS_BOTTLE);
        } else {
            if (entity instanceof Player player) {
                if (!player.getAbilities().instabuild) {
                    ItemStack itemstack = new ItemStack(Items.GLASS_BOTTLE);
                    if (!player.getInventory().add(itemstack)) {
                        player.drop(itemstack, false);
                    }
                }
            }
            return stack;
        }
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
    public InteractionResult place(BlockPlaceContext ctx) {
        if (ctx.getPlayer() != null && !ctx.getPlayer().isShiftKeyDown())
            return InteractionResult.PASS;
        return super.place(ctx);
    }

}
