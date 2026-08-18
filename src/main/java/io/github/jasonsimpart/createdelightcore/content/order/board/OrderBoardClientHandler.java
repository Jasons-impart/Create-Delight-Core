package io.github.jasonsimpart.createdelightcore.content.order.board;

import io.github.jasonsimpart.createdelightcore.network.SyncOrderBoardPacket;
import net.minecraft.client.Minecraft;

public final class OrderBoardClientHandler {
    private OrderBoardClientHandler() {
    }

    public static void handleSync(SyncOrderBoardPacket packet) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen instanceof OrderBoardScreen screen
                && screen.getMenu().getPos().equals(packet.pos())) {
            screen.acceptSync(packet.candidates(), packet.accepted(), packet.ticksUntilRefresh(), packet.rerollCost());
        }
    }
}
