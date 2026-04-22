package io.github.jasonsimpart.createdelightcore.mixin.createmetallurgy;

import fr.lucreeper74.createmetallurgy.content.blocks.industrial_crucible.CrucibleBlockEntity;
import io.github.jasonsimpart.createdelightcore.compat.createmetallurgy.backport.AlloyingBackportLogic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = CrucibleBlockEntity.class, remap = false)
public class CrucibleAlloyingMixin {
    @Inject(method = "tick", at = @At("TAIL"))
    private void createdelightcore$tick(CallbackInfo ci) {
        AlloyingBackportLogic.tickCrucible((CrucibleBlockEntity) (Object) this);
    }
}
