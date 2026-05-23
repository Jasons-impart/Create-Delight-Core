package io.github.jasonsimpart.mixin.eclipticseasons;

import io.github.jasonsimpart.Config;
import io.github.jasonsimpart.compat.eclipticseasons.EclipticSeasonsChunkSyncTracker;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "com.teamtea.eclipticseasons.common.core.snow.SnowyStatusHandler", remap = false)
public abstract class SnowyStatusHandlerMixin {
    @Inject(method = "sendToPlayer", at = @At("HEAD"), cancellable = true, remap = false)
    private void cdc$onlySendSnowyStatusToKnownChunks(IAttachmentHolder holder, ServerPlayer player, CallbackInfoReturnable<Boolean> cir) {
        if (Config.ENABLE_ECLIPTIC_SEASONS_CHUNK_ATTACHMENT_SYNC_PATCH.get() && holder instanceof LevelChunk chunk) {
            cir.setReturnValue(EclipticSeasonsChunkSyncTracker.canSendSnowyStatus(chunk, player));
        }
    }
}
