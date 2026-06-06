package io.github.jasonsimpart.createdelightcore.mixin.create;

import com.simibubi.create.compat.jei.category.animations.AnimatedSpout;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = AnimatedSpout.class, remap = false)
public class AnimatedSpoutMixin {

    @Shadow
    private List<FluidStack> fluids;

    @Inject(method = "draw", at = @At("HEAD"), cancellable = true)
    private void createdelightcore$skipEmptyFluidAnimation(GuiGraphics graphics, int xOffset, int yOffset, CallbackInfo ci) {
        // Create's spout animation assumes at least one fluid; empty tag ingredients can reach JEI before resolving.
        if (fluids == null || fluids.isEmpty())
            ci.cancel();
    }
}
