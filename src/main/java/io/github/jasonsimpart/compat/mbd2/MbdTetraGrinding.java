package io.github.jasonsimpart.compat.mbd2;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import se.mickelus.tetra.items.modular.IModularItem;

final class MbdTetraGrinding {
    private MbdTetraGrinding() {}

    static void hone(Player player, ItemStack stack, int damage) {
        if (!(stack.getItem() instanceof IModularItem modular) || modular.isBroken(stack)) return;
        int limit = modular.getHoningLimit(stack);
        if (limit <= 0) return;
        double multiplier = Math.max(0, (double) modular.getHoningProgress(stack) / limit - .2) / .8;
        modular.tickHoningProgression(player, stack, (int) Math.ceil(damage * multiplier));
    }
}
