package io.github.jasonsimpart.createdelightcore.mixin.alexscaves;

import com.github.alexmodguy.alexscaves.server.entity.item.NuclearExplosionEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/** Keeps the ticket type stable when a nuclear explosion releases its forced chunks. */
@Mixin(value = NuclearExplosionEntity.class, remap = false)
public class NuclearExplosionEntityMixin {
    @ModifyArg(
            method = "loadChunksAround(Z)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraftforge/common/world/ForgeChunkManager;forceChunk(Lnet/minecraft/server/level/ServerLevel;Ljava/lang/String;Lnet/minecraft/world/entity/Entity;IIZZ)Z"
            ),
            index = 6,
            remap = false,
            require = 1
    )
    private static boolean createdelightcore$keepNuclearChunkTicketType(boolean ticking) {
        return true;
    }
}
