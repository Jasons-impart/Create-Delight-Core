package io.github.jasonsimpart.createdelightcore.mixin.alexscaves;

import com.github.alexmodguy.alexscaves.server.enchantment.ACEnchantmentRegistry;
import com.github.alexmodguy.alexscaves.server.item.GalenaGauntletItem;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import se.mickelus.tetra.items.modular.ModularItem;
;

@Mixin(GalenaGauntletItem.class)
public class GalenaGauntletItemMixin {
    @Inject(method = "use", at = @At(value = "HEAD"), cancellable = true)
    public void useMixin(Level level, Player player, InteractionHand interactionHand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        ItemStack itemstack = player.getItemInHand(interactionHand);
        ItemStack otherHand = interactionHand == InteractionHand.MAIN_HAND ? player.getItemInHand(InteractionHand.OFF_HAND) : player.getItemInHand(InteractionHand.MAIN_HAND);
        if (otherHand.getItem() instanceof ModularItem modularItem) {
            boolean crystallization = itemstack.getEnchantmentLevel(ACEnchantmentRegistry.CRYSTALLIZATION.get()) > 0;
            boolean magnetizable = modularItem.getEffects(otherHand).stream().anyMatch(itemEffect -> itemEffect.getKey().equals("createdelight:magnetizable"));
            boolean crystal = modularItem.getEffects(otherHand).stream().anyMatch(itemEffect -> itemEffect.getKey().equals("createdelight:crystal_magnetizable"));
            if (magnetizable || (crystallization && crystal)) {
                if (!player.isCreative()) {
                    itemstack.hurtAndBreak(1, player, (player1) -> {
                        player1.broadcastBreakEvent(player1.getUsedItemHand());
                    });
                }

                player.startUsingItem(interactionHand);
                cir.setReturnValue(InteractionResultHolder.consume(itemstack));
            } else {
                cir.setReturnValue(InteractionResultHolder.fail(itemstack));
            }
        }
    }
}
