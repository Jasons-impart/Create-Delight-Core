package io.github.jasonsimpart.createdelightcore.mixin.solapplepie;

import com.llamalad7.mixinextras.sugar.Local;
import com.tarinoita.solsweetpotato.tracking.FoodTracker;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FoodTracker.class)
public class FoodTrackerMixin {
    @Inject(method = "onFoodEaten", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getItem()Lnet/minecraft/world/item/Item;"), cancellable = true)
    private static void onFoodEatenMixin(LivingEntityUseItemEvent.Finish event, CallbackInfo ci, @Local Player player) {
        if (player.isDeadOrDying()) ci.cancel();
    }
}
