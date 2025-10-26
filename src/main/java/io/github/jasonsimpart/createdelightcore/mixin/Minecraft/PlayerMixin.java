package io.github.jasonsimpart.createdelightcore.mixin.Minecraft;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import com.github.alexmodguy.alexscaves.server.item.ACItemRegistry;
import com.github.alexmodguy.alexscaves.server.item.SackOfSatingItem;
import com.github.alexthe666.iceandfire.item.IafItemRegistry;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.gameevent.GameEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.theillusivec4.curios.api.CuriosApi;

@Mixin(net.minecraft.world.entity.player.Player.class)
public class PlayerMixin {

    @Inject(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;tick()V", shift = At.Shift.AFTER))
    public void aiStepCuriosMixin(CallbackInfo ci){
        Player player = (Player)(Object)this;
        CuriosApi.getCuriosInventory(player).ifPresent(curiosInventory -> {
            var slotInventory = curiosInventory.getCurios().get("head");
            if (slotInventory != null) {
                for (int i = 0; i < slotInventory.getSlots(); i++) {
                    var item = slotInventory.getStacks().getStackInSlot(i).getItem();
                    if (item == IafItemRegistry.BLINDFOLD.get()) {
                        player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 50, 0, false, false));
                    }
                }
            }
            // 果腹袋
            var beltSlotInventory = curiosInventory.getCurios().get("belt");
            if (beltSlotInventory != null) {
                for (int i = 0; i < beltSlotInventory.getSlots(); i++) {
                    var itemStacks = beltSlotInventory.getStacks().getStackInSlot(i);
                    if (itemStacks.getItem() == ACItemRegistry.SACK_OF_SATING.get()) {
                        int hungerValue = SackOfSatingItem.getHunger(itemStacks);
                        long timestamp = SackOfSatingItem.getFeedTimestamp(itemStacks);
                        if (!player.level().isClientSide && hungerValue > 0 &&!player.getAbilities().invulnerable && player.tickCount % 100 == 0 && player.canEat(false) &&(timestamp == -1L || player.level().getGameTime() - timestamp > 40L)) {
                            if (hungerValue >= 5){
                                player.getFoodData().eat(5, 0.25F);
                                SackOfSatingItem.setHunger(itemStacks, SackOfSatingItem.getHunger(itemStacks) - 5);
                            }else{
                                player.getFoodData().eat(hungerValue, 0.05F * hungerValue);
                                SackOfSatingItem.setHunger(itemStacks, 0);
                            }

                            SackOfSatingItem.setFeedTimestamp(itemStacks, player.level().getGameTime());
                            player.level().gameEvent(player, GameEvent.EAT, player.blockPosition());
                            player.level().playSound((Player)null, player.getX(), player.getY(), player.getZ(), SoundEvents.GENERIC_EAT, SoundSource.PLAYERS, 0.5F, player.level().random.nextFloat() * 0.1F + 0.9F);
                        }
                    }
                }
            }
        });
    }
}
