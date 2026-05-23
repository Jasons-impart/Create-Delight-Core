package io.github.jasonsimpart.mixin.iceandfire;

import io.github.jasonsimpart.compat.iceandfire.IceAndFireWorldgenEntityGuard;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.WorldGenLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "com.iafenvoy.iceandfire.world.structure.HydraCaveStructure$HydraCavePiece", remap = false)
public abstract class HydraCavePieceMixin {
    @Redirect(
            method = "postProcess",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/WorldGenLevel;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z")
    )
    private boolean cdc$guardHydraWorldgenSpawn(WorldGenLevel level, Entity entity) {
        return IceAndFireWorldgenEntityGuard.addFreshEntityIfSafe(level, entity);
    }
}
