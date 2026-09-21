package io.github.jasonsimpart.mixin.iceandfire;

import com.iafenvoy.iceandfire.entity.GorgonEntity;
import com.iafenvoy.iceandfire.registry.IafItems;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.theillusivec4.curios.api.CuriosApi;

@Mixin(value = GorgonEntity.class, remap = false)
public abstract class GorgonEntityMixin {
    @Inject(method = "isBlindfolded", at = @At("RETURN"), cancellable = true)
    private static void createdelightcore$curiosBlindfold(LivingEntity attackTarget, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue()) {
            return;
        }
        CuriosApi.getCuriosInventory(attackTarget).flatMap(handler -> handler.getStacksHandler("head")).ifPresent(stacks -> {
            for (int i = 0; i < stacks.getSlots(); i++) {
                if (stacks.getStacks().getStackInSlot(i).is(IafItems.BLINDFOLD.get())) {
                    cir.setReturnValue(true);
                    return;
                }
            }
        });
    }
}
