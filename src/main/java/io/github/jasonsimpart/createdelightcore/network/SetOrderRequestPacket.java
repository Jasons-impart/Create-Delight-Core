package io.github.jasonsimpart.createdelightcore.network;

import io.github.jasonsimpart.createdelightcore.content.order.OrderRequestMode;
import io.github.jasonsimpart.createdelightcore.content.order.OrderRequestStrategy;
import io.github.jasonsimpart.createdelightcore.content.order.machine.OrderMachineMenu;
import io.github.jasonsimpart.createdelightcore.content.order.machine.OrderRequesterBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SetOrderRequestPacket {
    private final BlockPos pos;
    private final OrderRequestStrategy strategy;
    private final String targetAddress;
    private final boolean allowPartial;
    private final boolean trigger;

    public SetOrderRequestPacket(BlockPos pos, OrderRequestStrategy strategy, String targetAddress,
                                 boolean allowPartial, boolean trigger) {
        this.pos = pos;
        this.strategy = strategy == null ? OrderRequestStrategy.empty() : strategy;
        this.targetAddress = targetAddress == null ? "" : targetAddress;
        this.allowPartial = allowPartial;
        this.trigger = trigger;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        strategy.send(buf);
        buf.writeUtf(targetAddress, 128);
        buf.writeBoolean(allowPartial);
        buf.writeBoolean(trigger);
    }

    public static SetOrderRequestPacket decode(FriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        OrderRequestStrategy strategy = OrderRequestStrategy.receive(buf);
        return new SetOrderRequestPacket(pos, strategy, buf.readUtf(128), buf.readBoolean(), buf.readBoolean());
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
            if (strategy.isEmpty()) {
                player.displayClientMessage(Component.translatable("createdelightcore.order_request.no_selection"), true);
                return;
            }
            requester.setRequestStrategy(strategy, targetAddress, allowPartial);
            if (trigger) {
                boolean enoughItems = strategy.mode() == OrderRequestMode.RATIO
                        ? requester.hasEnoughSelectedStacks()
                        : allowPartial || requester.hasEnoughSelectedStacks();
                boolean success = requester.triggerSelectedRequest();
                if (success) {
                    player.displayClientMessage(Component.translatable("createdelightcore.order_request.sent"), true);
                } else if (!enoughItems) {
                    player.displayClientMessage(Component.translatable("createdelightcore.order_request.not_enough_items"), true);
                } else {
                    player.displayClientMessage(Component.translatable("createdelightcore.order_request.failed"), true);
                }
            } else {
                player.displayClientMessage(Component.translatable("createdelightcore.order_request.saved"), true);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
