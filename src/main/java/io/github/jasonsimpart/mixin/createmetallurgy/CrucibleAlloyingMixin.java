package io.github.jasonsimpart.mixin.createmetallurgy;

import fr.lucreeper74.createmetallurgy.content.blocks.industrial_crucible.CrucibleBlockEntity;
import io.github.jasonsimpart.compat.createmetallurgy.backport.AlloyingBackportLogic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = CrucibleBlockEntity.class, remap = false)
public abstract class CrucibleAlloyingMixin {
    @Inject(method = "tick", at = @At("TAIL"))
    private void createdelightcore$alloyingTick(CallbackInfo ci) {
        AlloyingBackportLogic.tickCrucible((CrucibleBlockEntity) (Object) this);
    }
}
