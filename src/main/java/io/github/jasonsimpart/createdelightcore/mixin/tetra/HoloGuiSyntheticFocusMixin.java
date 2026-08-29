package io.github.jasonsimpart.createdelightcore.mixin.tetra;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import se.mickelus.mutil.gui.GuiElement;

/**
 * Updates Tetra's Mutil focus tree immediately before a synthetic click.
 * HoloGui normally updates this tree during render, which is too late for
 * Ponderer's press-and-release pair executed in the same tick.
 */
@Pseudo
@Mixin(targets = "se.mickelus.tetra.items.modular.impl.holo.gui.HoloGui")
public abstract class HoloGuiSyntheticFocusMixin {
    @Shadow(remap = false)
    @Final
    private GuiElement defaultGui;

    @Inject(method = "m_6375_(DDI)Z", at = @At("HEAD"), remap = false)
    private void createdelightcore$syncSyntheticFocus(
            double mouseX,
            double mouseY,
            int button,
            CallbackInfoReturnable<Boolean> cir
    ) {
        int screenWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int screenHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();
        int left = (screenWidth - this.defaultGui.getWidth()) / 2;
        int top = (screenHeight - this.defaultGui.getHeight()) / 2;
        this.defaultGui.updateFocusState(left, top, (int) mouseX, (int) mouseY);
    }
}
