package io.github.jasonsimpart.createdelightcore.mixin.kaleidoscopetavern;

import com.github.ysbbbbbb.kaleidoscopetavern.block.brew.BarrelBlock;
import io.github.jasonsimpart.createdelightcore.compat.kaleidoscopetavern.TavernBarrelAccess;
import io.github.jasonsimpart.createdelightcore.compat.kaleidoscopetavern.TavernQuality;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

/** Reuse the upstream provider and multiblock lookup instead of adding a duplicate overlay. */
@Pseudo
@Mixin(targets = "com.github.ysbbbbbb.kaleidoscopetavern.compat.jade.block.BarrelComponentProvider", remap = false)
public abstract class BarrelJadeMixin {
    @Inject(method = "appendTooltip", at = @At("HEAD"), cancellable = true)
    private void createdelightcore$showQualityAging(ITooltip tooltip, BlockAccessor accessor,
                                                    IPluginConfig config, CallbackInfo ci) {
        var barrel = BarrelBlock.getBarrelEntity(accessor.getLevel(), accessor.getPosition(), accessor.getBlockState());
        if (barrel == null || ((TavernBarrelAccess) barrel).createdelightcore$getQualityBatch() == null) return;
        TavernQuality.barrelTooltip(barrel).forEach(tooltip::add);
        ci.cancel();
    }
}
