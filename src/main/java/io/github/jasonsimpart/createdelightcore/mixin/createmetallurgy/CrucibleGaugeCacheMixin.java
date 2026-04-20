package io.github.jasonsimpart.createdelightcore.mixin.createmetallurgy;

import fr.lucreeper74.createmetallurgy.content.blocks.industrial_crucible.CrucibleBlockEntity;
import io.github.jasonsimpart.createdelightcore.compat.createmetallurgy.backport.CrucibleGaugeCacheAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = CrucibleBlockEntity.class, remap = false)
public class CrucibleGaugeCacheMixin implements CrucibleGaugeCacheAccess {
    @Unique
    private boolean createdelightcore$gaugeCacheValid;

    @Unique
    private boolean createdelightcore$hasGaugeAttachment;

    @Override
    public boolean createdelightcore$isGaugeCacheValid() {
        return createdelightcore$gaugeCacheValid;
    }

    @Override
    public boolean createdelightcore$hasGaugeAttachmentCached() {
        return createdelightcore$hasGaugeAttachment;
    }

    @Override
    public void createdelightcore$setGaugeAttachmentCache(boolean hasGauge) {
        createdelightcore$hasGaugeAttachment = hasGauge;
        createdelightcore$gaugeCacheValid = true;
    }

    @Override
    public void createdelightcore$invalidateGaugeAttachmentCache() {
        createdelightcore$gaugeCacheValid = false;
    }

    @Inject(method = "notifyMultiUpdated", at = @At("TAIL"))
    private void createdelightcore$invalidateOnStructureUpdate(CallbackInfo ci) {
        createdelightcore$invalidateGaugeAttachmentCache();
        CrucibleBlockEntity controller = ((CrucibleBlockEntity) (Object) this).getControllerBE();
        if (controller != null) {
            ((CrucibleGaugeCacheAccess) controller).createdelightcore$invalidateGaugeAttachmentCache();
        }
    }

    @Inject(method = "setAttachment", at = @At("TAIL"))
    private void createdelightcore$invalidateOnAttachmentUpdate(CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue()) {
            return;
        }
        createdelightcore$invalidateGaugeAttachmentCache();
        CrucibleBlockEntity controller = ((CrucibleBlockEntity) (Object) this).getControllerBE();
        if (controller != null) {
            ((CrucibleGaugeCacheAccess) controller).createdelightcore$invalidateGaugeAttachmentCache();
        }
    }
}
