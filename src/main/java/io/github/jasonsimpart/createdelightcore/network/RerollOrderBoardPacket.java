package io.github.jasonsimpart.createdelightcore.network;

import io.github.jasonsimpart.createdelightcore.content.order.board.OrderBoardBlockEntity;
import io.github.jasonsimpart.createdelightcore.content.order.board.OrderBoardMenu;
import io.github.jasonsimpart.createdelightcore.content.order.board.OrderBoardState;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

public record RerollOrderBoardPacket(BlockPos pos) {
    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
    }

    public static RerollOrderBoardPacket decode(FriendlyByteBuf buf) {
        return new RerollOrderBoardPacket(buf.readBlockPos());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null || !(player.containerMenu instanceof OrderBoardMenu menu)
                    || !menu.getPos().equals(pos)) {
                return;
            }
            BlockEntity blockEntity = player.level().getBlockEntity(pos);
            if (!(blockEntity instanceof OrderBoardBlockEntity board)) {
                return;
            }
            OrderBoardState.RerollResult result = OrderBoardState.reroll(player, board);
            player.displayClientMessage(result.message(), true);
            CDNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                    new SyncOrderBoardPacket(pos, result.snapshot()));
        });
        ctx.get().setPacketHandled(true);
    }
}
