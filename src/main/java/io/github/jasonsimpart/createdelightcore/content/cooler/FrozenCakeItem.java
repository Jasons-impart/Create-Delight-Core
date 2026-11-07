package io.github.jasonsimpart.createdelightcore.content.cooler;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** The pack's cold-food effect lives with the item, including non-player consumers. */
public class FrozenCakeItem extends Item {
    public FrozenCakeItem(Properties properties) { super(properties); }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity consumer) {
        ItemStack result = super.finishUsingItem(stack, level, consumer);
        if (!level.isClientSide) consumer.setTicksFrozen(consumer.getTicksFrozen() + 800);
        return result;
    }
}
