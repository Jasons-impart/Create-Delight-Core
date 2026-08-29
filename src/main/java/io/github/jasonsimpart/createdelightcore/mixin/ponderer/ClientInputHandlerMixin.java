package io.github.jasonsimpart.createdelightcore.mixin.ponderer;

import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Keeps embedded GUI hover/focus state aligned with Ponderer's synthetic click
 * coordinates before the forwarded mouse event is delivered.
 */
@Pseudo
@Mixin(targets = "com.nododiiiii.ponderer.forge.sticksnapshot.client.ClientInputHandler")
public abstract class ClientInputHandlerMixin {
    @Inject(
            method = "handleMirrorMousePressed(Lnet/minecraft/client/gui/screens/Screen;DDIZ)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screens/Screen;mouseClicked(DDI)Z"
            ),
            require = 0
    )
    private static void createdelightcore$syncPressedHover(
            Screen screen,
            double x,
            double y,
            int button,
            boolean automated,
            CallbackInfoReturnable<Boolean> cir
    ) {
        screen.mouseMoved(x, y);
    }

    @Inject(
            method = "handleMirrorMouseReleased(Lnet/minecraft/client/gui/screens/Screen;DDIZ)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screens/Screen;mouseReleased(DDI)Z"
            ),
            require = 0
    )
    private static void createdelightcore$syncReleasedHover(
            Screen screen,
            double x,
            double y,
            int button,
            boolean automated,
            CallbackInfoReturnable<Boolean> cir
    ) {
        screen.mouseMoved(x, y);
    }
}
