package io.github.jasonsimpart.createdelightcore.mixin.extendedae_plus;

import appeng.api.networking.IGridConnection;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

/** Same AE2 energy-extract race as LabelLink, for frequency wireless slave links. */
@Pseudo
@Mixin(targets = "com.extendedae_plus.ae.wireless.WirelessSlaveLink", remap = false)
public abstract class WirelessSlaveLinkDestroyConnectionMixin {
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
        }
    }
}
