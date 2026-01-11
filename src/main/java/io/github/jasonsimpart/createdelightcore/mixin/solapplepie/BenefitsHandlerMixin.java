package io.github.jasonsimpart.createdelightcore.mixin.solapplepie;

import com.tarinoita.solsweetpotato.tracking.FoodList;
import com.tarinoita.solsweetpotato.tracking.benefits.BenefitsHandler;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = BenefitsHandler.class, remap = false)
public class BenefitsHandlerMixin {
    @Inject(method = "updateBenefits", at = @At("HEAD"), cancellable = true)
    private static void updateBenefits(Player player, double diversity, CallbackInfo ci) {
        if (FoodList.get(player) == null)
            ci.cancel();
    }
}
