package io.github.jasonsimpart.createdelightcore.network;

import io.github.jasonsimpart.createdelightcore.content.order.board.OrderBoardBlockEntity;
import io.github.jasonsimpart.createdelightcore.content.order.board.OrderBoardMenu;
import io.github.jasonsimpart.createdelightcore.content.order.board.OrderBoardState;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

public record AcceptOrderBoardCandidatePacket(BlockPos pos, int index) {
    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeVarInt(index);
    }

    public static AcceptOrderBoardCandidatePacket decode(FriendlyByteBuf buf) {
        return new AcceptOrderBoardCandidatePacket(buf.readBlockPos(), buf.readVarInt());
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
            OrderBoardState.AcceptResult result = OrderBoardState.accept(player, board, index);
            player.displayClientMessage(Component.translatable(result.messageKey()), true);
            CDNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                    new SyncOrderBoardPacket(pos, result.snapshot()));
        });
        ctx.get().setPacketHandled(true);
    }
}
