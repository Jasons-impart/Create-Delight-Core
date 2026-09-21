package io.github.jasonsimpart.mixin.iceandfire;

import com.iafenvoy.iceandfire.entity.SirenEntity;
import com.iafenvoy.iceandfire.registry.IafItems;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.theillusivec4.curios.api.CuriosApi;

@Mixin(value = SirenEntity.class, remap = false)
public abstract class SirenEntityMixin {
    @Inject(method = "isWearingEarplugs", at = @At("RETURN"), cancellable = true)
    private static void createdelightcore$curiosEarplugs(LivingEntity entity, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue()) {
            return;
        }
        CuriosApi.getCuriosInventory(entity).flatMap(handler -> handler.getStacksHandler("head")).ifPresent(stacks -> {
            for (int i = 0; i < stacks.getSlots(); i++) {
                if (stacks.getStacks().getStackInSlot(i).is(IafItems.EARPLUGS.get())) {
                    cir.setReturnValue(true);
                    return;
                }
            }
        });
    }
}
