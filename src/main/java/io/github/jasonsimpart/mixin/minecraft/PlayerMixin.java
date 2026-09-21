package io.github.jasonsimpart.mixin.minecraft;

import com.github.alexmodguy.alexscaves.server.item.ACItemRegistry;
import com.github.alexmodguy.alexscaves.server.item.SackOfSatingItem;
import com.iafenvoy.iceandfire.registry.IafItems;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.gameevent.GameEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.theillusivec4.curios.api.CuriosApi;

@Mixin(Player.class)
public abstract class PlayerMixin {
    @Inject(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;tick()V", shift = At.Shift.AFTER))
    private void createdelightcore$curiosTick(CallbackInfo ci) {
        Player player = (Player) (Object) this;
        CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
            handler.getStacksHandler("head").ifPresent(stacks -> {
                for (int i = 0; i < stacks.getSlots(); i++) {
                    if (stacks.getStacks().getStackInSlot(i).is(IafItems.BLINDFOLD.get())) {
                        player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 50, 0, false, false));
                    }
                }
            });
            handler.getStacksHandler("belt").ifPresent(stacks -> {
                for (int i = 0; i < stacks.getSlots(); i++) {
                    ItemStack stack = stacks.getStacks().getStackInSlot(i);
                    if (!stack.is(ACItemRegistry.SACK_OF_SATING.get())) {
                        continue;
                    }
                    int hungerValue = SackOfSatingItem.getHunger(stack);
                    long timestamp = SackOfSatingItem.getFeedTimestamp(stack);
                    if (player.level().isClientSide || hungerValue <= 0 || player.getAbilities().invulnerable
                            || player.tickCount % 100 != 0 || !player.canEat(false)
                            || (timestamp != -1L && player.level().getGameTime() - timestamp <= 40L)) {
                        continue;
                    }
                    if (hungerValue >= 5) {
                        player.getFoodData().eat(5, 0.25F);
                        SackOfSatingItem.setHunger(stack, hungerValue - 5);
                    } else {
                        player.getFoodData().eat(hungerValue, 0.05F * hungerValue);
                        SackOfSatingItem.setHunger(stack, 0);
                    }
                    SackOfSatingItem.setFeedTimestamp(stack, player.level().getGameTime());
                    player.level().gameEvent(player, GameEvent.EAT, player.blockPosition());
                    player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GENERIC_EAT, SoundSource.PLAYERS, 0.5F, player.level().random.nextFloat() * 0.1F + 0.9F);
                }
            });
        });
    }
}
