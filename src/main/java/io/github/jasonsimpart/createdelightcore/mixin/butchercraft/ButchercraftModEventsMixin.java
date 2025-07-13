package io.github.jasonsimpart.createdelightcore.mixin.butchercraft;

import org.spongepowered.asm.mixin.Mixin;
import com.lance5057.butchercraft.ButchercraftModEvents;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ButchercraftModEvents.class, remap = false)
abstract class ButchercraftModEventsMixin {
    // 在dirtyHands cancel事件之前检查事件是否是可cancel的，避免cancel一个无法cancel的event造成的崩溃
    @Inject(method = "dirtyHands", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/event/entity/living/LivingEntityUseItemEvent$Finish;setCanceled(Z)V"), cancellable = true)
        private static void earlyReturnIfNotCancelable(LivingEntityUseItemEvent.Finish event, CallbackInfo info) {
        if (!event.isCancelable()) {
            info.cancel();
        }
    }
}
