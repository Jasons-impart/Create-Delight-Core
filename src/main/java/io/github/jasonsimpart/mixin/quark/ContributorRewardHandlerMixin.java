package io.github.jasonsimpart.mixin.quark;

import io.github.jasonsimpart.Config;
import io.github.jasonsimpart.CreateDelightCore;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "org.violetmoon.quark.base.handler.ContributorRewardHandler", remap = false)
public class ContributorRewardHandlerMixin {
    @Inject(method = "init()V", at = @At("HEAD"), cancellable = true)
    private static void cdc$disableContributorRewardLoading(CallbackInfo ci) {
        if (!Config.ENABLE_QUARK_CONTRIBUTOR_REWARD_PATCH.get()) {
            return;
        }

        CreateDelightCore.LOGGER.debug("CDC disabled Quark contributor reward loading");
        ci.cancel();
    }

    @Inject(method = "getTier(Ljava/lang/String;)I", at = @At("HEAD"), cancellable = true)
    private static void cdc$disableContributorTier(String name, CallbackInfoReturnable<Integer> cir) {
        if (!Config.ENABLE_QUARK_CONTRIBUTOR_REWARD_PATCH.get()) {
            return;
        }

        cir.setReturnValue(0);
    }

    @Inject(method = "getTier(Lnet/minecraft/world/entity/player/Player;)I", at = @At("HEAD"), cancellable = true)
    private static void cdc$disableContributorTier(Player player, CallbackInfoReturnable<Integer> cir) {
        if (!Config.ENABLE_QUARK_CONTRIBUTOR_REWARD_PATCH.get()) {
            return;
        }

        cir.setReturnValue(0);
    }
}
