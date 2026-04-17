package io.github.jasonsimpart.createdelightcore.mixin.trueuuid;

import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Pseudo
@Mixin(targets = "cn.alini.trueuuid.server.SkinRefreshHandler")
public abstract class SkinRefreshHandlerMixin {
    @Inject(method = "lambda$onLogin$0", at = @At("HEAD"), cancellable = true, require = 0)
    private static void createdelightcore$refreshOtherPlayersOnly(MinecraftServer server, ServerPlayer joiningPlayer, CallbackInfo ci) {
        var playerList = server.getPlayerList();
        var removePacket = new ClientboundPlayerInfoRemovePacket(List.of(joiningPlayer.getUUID()));
        var addPacket = ClientboundPlayerInfoUpdatePacket.createPlayerInitializing(List.of(joiningPlayer));

        for (var onlinePlayer : playerList.getPlayers()) {
            if (onlinePlayer == joiningPlayer) {
                continue;
            }
            onlinePlayer.connection.send(removePacket);
            onlinePlayer.connection.send(addPacket);
        }

        ci.cancel();
    }
}
