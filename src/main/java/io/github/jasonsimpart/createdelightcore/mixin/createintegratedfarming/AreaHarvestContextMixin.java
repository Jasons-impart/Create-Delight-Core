package io.github.jasonsimpart.createdelightcore.mixin.createintegratedfarming;

import io.github.jasonsimpart.createdelightcore.content.util.QualityHarvestAutomationContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import plus.dragons.createintegratedfarming.api.harvester.AreaHarvestContext;

@Mixin(value = AreaHarvestContext.class, remap = false)
public abstract class AreaHarvestContextMixin {
    @Inject(method = "collect", at = @At("HEAD"))
    private void createdelightcore$applyQuality(ItemStack stack, CallbackInfo ci) {
        QualityHarvestAutomationContext.applyQuality(stack);
    }
}
