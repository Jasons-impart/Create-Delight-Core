package io.github.jasonsimpart.createdelightcore.mixin.extendedae_plus;

import appeng.api.networking.IGridConnection;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

/**
 * ExtendedAE Plus 1.5.5: LabelLink.destroyConnection's primary IGridConnection.destroy()
 * is unprotected. Unload during AE2 energy extract throws and aborts chunk save.
 */
@Pseudo
@Mixin(targets = "com.extendedae_plus.ae.wireless.LabelLink", remap = false)
public abstract class LabelLinkDestroyConnectionMixin {
    @WrapOperation(
            method = "destroyConnection()V",
            at = @At(
                    value = "INVOKE",
                    target = "Lappeng/api/networking/IGridConnection;destroy()V",
                    remap = false
            ),
            remap = false,
            require = 0
    )
    private void createdelightcore$safeDestroyGridConnection(IGridConnection connection, Operation<Void> original) {
        try {
            original.call(connection);
        } catch (Throwable ignored) {
            // AE2 EnergyService forbids removeProvider while extracting; keep unload alive.
        }
    }
}
