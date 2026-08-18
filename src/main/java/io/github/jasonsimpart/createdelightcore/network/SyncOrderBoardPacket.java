package io.github.jasonsimpart.createdelightcore.network;

import io.github.jasonsimpart.createdelightcore.content.order.board.OrderBoardCandidate;
import io.github.jasonsimpart.createdelightcore.content.order.board.OrderBoardClientHandler;
import io.github.jasonsimpart.createdelightcore.content.order.board.OrderBoardState;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public record SyncOrderBoardPacket(BlockPos pos, List<OrderBoardCandidate> candidates,
                                   boolean accepted, int ticksUntilRefresh, int rerollCost) {
    public SyncOrderBoardPacket(BlockPos pos, OrderBoardState.Snapshot snapshot) {
        this(pos, snapshot.candidates(), snapshot.accepted(), snapshot.ticksUntilRefresh(), snapshot.rerollCost());
    }

    public SyncOrderBoardPacket {
        candidates = candidates == null ? List.of() : List.copyOf(candidates);
        ticksUntilRefresh = Math.max(0, ticksUntilRefresh);
        rerollCost = Math.max(0, rerollCost);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeVarInt(candidates.size());
        candidates.forEach(candidate -> candidate.send(buf));
        buf.writeBoolean(accepted);
        buf.writeVarInt(ticksUntilRefresh);
        buf.writeVarInt(rerollCost);
    }

    public static SyncOrderBoardPacket decode(FriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        int size = Math.max(0, buf.readVarInt());
        List<OrderBoardCandidate> candidates = new ArrayList<>(Math.min(3, size));
        for (int i = 0; i < size; i++) {
            OrderBoardCandidate candidate = OrderBoardCandidate.receive(buf);
            if (i < 3) {
                candidates.add(candidate);
            }
        }
        return new SyncOrderBoardPacket(pos, candidates, buf.readBoolean(), buf.readVarInt(), buf.readVarInt());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> OrderBoardClientHandler.handleSync(this)));
        ctx.get().setPacketHandled(true);
    }
}
