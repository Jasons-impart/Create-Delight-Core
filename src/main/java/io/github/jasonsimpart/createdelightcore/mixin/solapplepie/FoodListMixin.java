package io.github.jasonsimpart.createdelightcore.mixin.solapplepie;

import com.tarinoita.solsweetpotato.api.SOLSweetPotatoAPI;
import com.tarinoita.solsweetpotato.tracking.FoodList;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = FoodList.class, remap = false)
public class FoodListMixin {
    @Inject(method = "get", at = @At("HEAD"), cancellable = true)
    private static void get$nothrow(Player player, CallbackInfoReturnable<FoodList> cir) {
        if (!player.getCapability(SOLSweetPotatoAPI.foodCapability).isPresent())
            cir.setReturnValue(null);
    }
}
