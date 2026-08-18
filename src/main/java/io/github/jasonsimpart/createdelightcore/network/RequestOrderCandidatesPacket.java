package io.github.jasonsimpart.createdelightcore.network;

import io.github.jasonsimpart.createdelightcore.content.order.OrderEntryCandidates;
import io.github.jasonsimpart.createdelightcore.content.order.machine.OrderMachineMenu;
import io.github.jasonsimpart.createdelightcore.content.order.machine.OrderRequesterBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.List;
import java.util.function.Supplier;

public class RequestOrderCandidatesPacket {
    private final BlockPos pos;

    public RequestOrderCandidatesPacket(BlockPos pos) {
        this.pos = pos;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
    }

    public static RequestOrderCandidatesPacket decode(FriendlyByteBuf buf) {
        return new RequestOrderCandidatesPacket(buf.readBlockPos());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null || !(player.containerMenu instanceof OrderMachineMenu menu) || !menu.getPos().equals(pos)) {
                return;
            }
            BlockEntity blockEntity = player.level().getBlockEntity(pos);
            if (!(blockEntity instanceof OrderRequesterBlockEntity requester)) {
                return;
            }
            List<OrderEntryCandidates> groups = requester.getAccurateCandidateGroups();
            CDNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new SyncOrderCandidatesPacket(
                    pos,
                    groups,
                    requester.getRequestStrategy(),
                    requester.getTargetAddress(),
                    requester.isAllowPartialRequests()
            ));
        });
        ctx.get().setPacketHandled(true);
    }
}
