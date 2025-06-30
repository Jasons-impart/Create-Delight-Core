package io.github.jasonsimpart.createdelightcore.mixin.solcarrot;

import com.cazsius.solcarrot.api.SOLCarrotAPI;
import com.cazsius.solcarrot.tracking.FoodList;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = FoodList.class, remap = false)
public class FoodListMixin {
    @Inject(method = "get", at = @At(value = "HEAD"), cancellable = true, require = 1)
    private static void getMixin(Player player, CallbackInfoReturnable<FoodList> cir) {
        cir.setReturnValue((FoodList) player.getCapability(SOLCarrotAPI.foodCapability).orElse(new FoodList()));
    }
}
