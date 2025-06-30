package io.github.jasonsimpart.createdelightcore.mixin.solapplepie;

import com.tarinoita.solsweetpotato.tracking.FoodTracker;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = FoodTracker.class, remap = false)
public class FoodTrackerMixin {
    @Inject(method = "updateFoodList", at = @At(value = "HEAD"), cancellable = true, require = 1)
    private static void updateFoodListMixin(Item food, Player player, CallbackInfo ci) {
        if (player.isDeadOrDying()) ci.cancel();
    }
}
