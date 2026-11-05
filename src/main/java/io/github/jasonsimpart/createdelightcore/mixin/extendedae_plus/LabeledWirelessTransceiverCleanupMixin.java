package io.github.jasonsimpart.createdelightcore.mixin.extendedae_plus;

import appeng.api.networking.IManagedGridNode;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

/**
 * ExtendedAE Plus 1.5.5 calls cleanupForRemoval from onChunkUnloaded; managedNode.destroy()
 * can hit the same AE2 energy-provider race after the label link is torn down.
 */
@Pseudo
@Mixin(targets = "com.extendedae_plus.content.wireless.LabeledWirelessTransceiverBlockEntity", remap = false)
public abstract class LabeledWirelessTransceiverCleanupMixin {
    @WrapOperation(
            method = "cleanupForRemoval()V",
            at = @At(
                    value = "INVOKE",
                    target = "Lappeng/api/networking/IManagedGridNode;destroy()V",
                    remap = false
            ),
            remap = false,
            require = 0
    )
    private void createdelightcore$safeDestroyManagedNode(IManagedGridNode node, Operation<Void> original) {
        try {
            original.call(node);
        } catch (Throwable ignored) {
        }
    }
}
