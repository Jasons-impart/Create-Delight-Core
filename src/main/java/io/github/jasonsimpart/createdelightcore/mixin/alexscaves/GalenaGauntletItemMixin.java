package io.github.jasonsimpart.createdelightcore.mixin.alexscaves;

import com.github.alexmodguy.alexscaves.server.enchantment.ACEnchantmentRegistry;
import com.github.alexmodguy.alexscaves.server.entity.ACEntityRegistry;
import com.github.alexmodguy.alexscaves.server.entity.item.MagneticWeaponEntity;
import com.github.alexmodguy.alexscaves.server.item.GalenaGauntletItem;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
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
    @Inject(method = "onUseTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getEnchantmentLevel(Lnet/minecraft/world/item/enchantment/Enchantment;)I",shift = At.Shift.AFTER), remap = false)
    public void useTickMixin(Level level, LivingEntity living, ItemStack stack, int timeUsing, CallbackInfo ci, @Local InteractionHand otherHand) {

        ItemStack otherStack = living.getItemInHand(otherHand);
        boolean otherMagneticWeaponsInUse = false;

        if (otherStack.getItem() instanceof ModularItem modularItem) {
            boolean crystallization = stack.getEnchantmentLevel(ACEnchantmentRegistry.CRYSTALLIZATION.get()) > 0;
            boolean magnetizable = modularItem.getEffects(otherStack).stream().anyMatch(itemEffect -> itemEffect.getKey().equals("createdelight:magnetizable"));
            boolean crystal = modularItem.getEffects(otherStack).stream().anyMatch(itemEffect -> itemEffect.getKey().equals("createdelight:crystal_magnetizable"));
            if (magnetizable || (crystallization && crystal)) {
                for (MagneticWeaponEntity magneticWeapon : level.getEntitiesOfClass(MagneticWeaponEntity.class, living.getBoundingBox().inflate(64, 64, 64))) {
                    Entity controller = magneticWeapon.getController();
                    if (controller != null && controller.is(living)) {
                        otherMagneticWeaponsInUse = true;
                        break;
                    }
                }
                if (!otherMagneticWeaponsInUse) {
                    ItemStack copy = otherStack.copy();
                    otherStack.setCount(0);
                    MagneticWeaponEntity magneticWeapon = ACEntityRegistry.MAGNETIC_WEAPON.get().create(level);
                    magneticWeapon.setItemStack(copy);
                    magneticWeapon.setPos(living.position().add(0, 1, 0));
                    magneticWeapon.setControllerUUID(living.getUUID());
                    level.addFreshEntity(magneticWeapon);
                }
            } else if (!otherStack.isEmpty()) {
                living.stopUsingItem();
            }
        }
    }
}
