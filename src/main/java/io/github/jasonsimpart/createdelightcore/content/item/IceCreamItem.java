package io.github.jasonsimpart.createdelightcore.content.item;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BowlFoodItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class IceCreamItem extends BowlFoodItem {
    public IceCreamItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        entity.setTicksFrozen(entity.getTicksFrozen() + 200);
        return super.finishUsingItem(stack, level, entity);
    }

    @Override
    public SoundEvent getEatingSound() {
        return SoundEvents.GENERIC_EAT;
    }
}
