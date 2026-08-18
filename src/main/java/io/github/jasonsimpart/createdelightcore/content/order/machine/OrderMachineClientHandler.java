package io.github.jasonsimpart.createdelightcore.content.order.machine;

import io.github.jasonsimpart.createdelightcore.network.SyncOrderCandidatesPacket;
import net.minecraft.client.Minecraft;

public final class OrderMachineClientHandler {
    private OrderMachineClientHandler() {
    }

    public static void handleCandidateSync(SyncOrderCandidatesPacket packet) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen instanceof OrderMachineScreen<?> screen && screen.getMenu().getPos().equals(packet.pos())) {
            screen.acceptCandidateSync(packet.groups(), packet.strategy(), packet.targetAddress(), packet.allowPartial());
        }
    }
}
