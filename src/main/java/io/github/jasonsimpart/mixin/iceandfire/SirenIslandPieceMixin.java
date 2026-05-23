package io.github.jasonsimpart.mixin.iceandfire;

import io.github.jasonsimpart.compat.iceandfire.IceAndFireWorldgenEntityGuard;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ServerLevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "com.iafenvoy.iceandfire.world.structure.SirenIslandStructure$SirenIslandPiece", remap = false)
public abstract class SirenIslandPieceMixin {
    @Redirect(
            method = "spawnSiren",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/ServerLevelAccessor;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z")
    )
    private boolean cdc$guardSirenWorldgenSpawn(ServerLevelAccessor level, Entity entity) {
        return IceAndFireWorldgenEntityGuard.addFreshEntityIfSafe(level, entity);
    }
}
