package io.github.jasonsimpart.createdelightcore.content.order;

import net.minecraft.world.item.ItemStack;

public record OrderCandidate(ItemStack stack, int count, int quality) {
    public OrderCandidate {
        stack = stack.copyWithCount(Math.max(1, Math.min(stack.getMaxStackSize(), count)));
        count = Math.max(0, count);
    }
}
