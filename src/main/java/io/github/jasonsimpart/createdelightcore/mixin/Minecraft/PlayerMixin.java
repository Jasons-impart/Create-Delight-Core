package io.github.jasonsimpart.createdelightcore.mixin.Minecraft;

import com.github.alexthe666.iceandfire.item.IafItemRegistry;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
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
        });
    }
}
