package io.github.jasonsimpart.content.item;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ReturningFoodItem extends Item {
    private final ResourceLocation returnedItem;

    public ReturningFoodItem(Properties properties, ResourceLocation returnedItem) {
        super(properties);
        this.returnedItem = returnedItem;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        ItemStack result = super.finishUsingItem(stack, level, livingEntity);
        if (level instanceof ServerLevel && livingEntity instanceof Player player && !player.isCreative()) {
            BuiltInRegistries.ITEM.getOptional(returnedItem)
                    .map(ItemStack::new)
                    .ifPresent(returnStack -> {
                        if (!player.getInventory().add(returnStack)) {
                            player.drop(returnStack, false);
                        }
                    });
        }
        return result;
    }
}
