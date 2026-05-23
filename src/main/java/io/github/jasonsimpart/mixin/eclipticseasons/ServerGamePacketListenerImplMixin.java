package io.github.jasonsimpart.mixin.eclipticseasons;

import io.github.jasonsimpart.compat.eclipticseasons.EclipticSeasonsChunkSyncTracker;
import net.minecraft.network.protocol.game.ServerboundChunkBatchReceivedPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin {
    @Shadow
    public ServerPlayer player;

    @Inject(method = "handleChunkBatchReceived", at = @At("TAIL"))
    private void cdc$acknowledgeEclipticSeasonsChunkBatch(ServerboundChunkBatchReceivedPacket packet, CallbackInfo ci) {
        EclipticSeasonsChunkSyncTracker.acknowledgeBatch(this.player);
    }
}
