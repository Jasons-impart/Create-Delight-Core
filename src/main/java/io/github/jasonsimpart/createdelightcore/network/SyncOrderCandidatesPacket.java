package io.github.jasonsimpart.createdelightcore.network;

import io.github.jasonsimpart.createdelightcore.content.order.OrderCandidate;
import io.github.jasonsimpart.createdelightcore.content.order.OrderEntry;
import io.github.jasonsimpart.createdelightcore.content.order.OrderEntryCandidates;
import io.github.jasonsimpart.createdelightcore.content.order.OrderRequestStrategy;
import io.github.jasonsimpart.createdelightcore.content.order.machine.OrderMachineClientHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class SyncOrderCandidatesPacket {
    private final BlockPos pos;
    private final List<OrderEntryCandidates> groups;
    private final OrderRequestStrategy strategy;
    private final String targetAddress;
    private final boolean allowPartial;

    public SyncOrderCandidatesPacket(BlockPos pos, List<OrderEntryCandidates> groups) {
        this(pos, groups, OrderRequestStrategy.empty(), "", false);
    }

    public SyncOrderCandidatesPacket(BlockPos pos, List<OrderEntryCandidates> groups,
                                     OrderRequestStrategy strategy, String targetAddress,
                                     boolean allowPartial) {
        this.pos = pos;
        this.groups = List.copyOf(groups);
        this.strategy = strategy == null ? OrderRequestStrategy.empty() : strategy;
        this.targetAddress = targetAddress == null ? "" : targetAddress;
        this.allowPartial = allowPartial;
    }

    public BlockPos pos() {
        return pos;
    }

    public List<OrderEntryCandidates> groups() {
        return groups;
    }

    public OrderRequestStrategy strategy() {
        return strategy;
    }

    public String targetAddress() {
        return targetAddress;
    }

    public boolean allowPartial() {
        return allowPartial;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeVarInt(groups.size());
        for (OrderEntryCandidates group : groups) {
            buf.writeNbt(group.entry().write());
            buf.writeVarInt(group.candidates().size());
            for (OrderCandidate candidate : group.candidates()) {
                buf.writeItem(candidate.stack());
                buf.writeVarInt(candidate.count());
                buf.writeVarInt(candidate.quality());
            }
        }
        strategy.send(buf);
        buf.writeUtf(targetAddress, 128);
        buf.writeBoolean(allowPartial);
    }

    public static SyncOrderCandidatesPacket decode(FriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        int groupCount = buf.readVarInt();
        List<OrderEntryCandidates> groups = new ArrayList<>();
        for (int groupIndex = 0; groupIndex < groupCount; groupIndex++) {
            CompoundTag entryTag = buf.readNbt();
            OrderEntry entry = OrderEntry.read(entryTag == null ? new CompoundTag() : entryTag);
            int candidateCount = buf.readVarInt();
            List<OrderCandidate> candidates = new ArrayList<>();
            for (int candidateIndex = 0; candidateIndex < candidateCount; candidateIndex++) {
                candidates.add(new OrderCandidate(buf.readItem(), buf.readVarInt(), buf.readVarInt()));
            }
            groups.add(new OrderEntryCandidates(entry, candidates));
        }
        OrderRequestStrategy strategy = OrderRequestStrategy.receive(buf);
        return new SyncOrderCandidatesPacket(pos, groups, strategy, buf.readUtf(128), buf.readBoolean());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> OrderMachineClientHandler.handleCandidateSync(this)));
        ctx.get().setPacketHandled(true);
    }
}
