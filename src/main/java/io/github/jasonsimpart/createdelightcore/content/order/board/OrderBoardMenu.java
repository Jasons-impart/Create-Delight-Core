package io.github.jasonsimpart.createdelightcore.content.order.board;

import io.github.jasonsimpart.createdelightcore.registry.CDBlocks;
import io.github.jasonsimpart.createdelightcore.registry.CDMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.List;

public class OrderBoardMenu extends AbstractContainerMenu {
    private final BlockPos pos;
    private final OrderBoardBlockEntity blockEntity;
    private List<OrderBoardCandidate> candidates = List.of();
    private boolean accepted;
    private int ticksUntilRefresh;
    private int rerollCost;

    public OrderBoardMenu(int containerId, Inventory inventory, FriendlyByteBuf data) {
        this(containerId, inventory, data.readBlockPos());
    }

    public OrderBoardMenu(int containerId, Inventory inventory, BlockPos pos) {
        super(CDMenus.ORDER_BOARD.get(), containerId);
        this.pos = pos;
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
        this.blockEntity = blockEntity instanceof OrderBoardBlockEntity board ? board : null;
    }

    public BlockPos getPos() {
        return pos;
    }

    public OrderBoardBlockEntity getBlockEntity() {
        return blockEntity;
    }

    public List<OrderBoardCandidate> getCandidates() {
        return candidates;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public int getTicksUntilRefresh() {
        return ticksUntilRefresh;
    }

    public int getRerollCost() {
        return rerollCost;
    }

    public void setBoardState(List<OrderBoardCandidate> candidates, boolean accepted, int ticksUntilRefresh,
                              int rerollCost) {
        this.candidates = candidates == null ? List.of() : List.copyOf(candidates);
        this.accepted = accepted;
        this.ticksUntilRefresh = Math.max(0, ticksUntilRefresh);
        this.rerollCost = Math.max(0, rerollCost);
    }

    public void clientTick() {
        if (ticksUntilRefresh > 0) {
            ticksUntilRefresh--;
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return blockEntity != null && !blockEntity.isRemoved()
                && player.level().getBlockEntity(pos) == blockEntity
                && player.level().getBlockState(pos).is(CDBlocks.ORDER_BOARD.get())
                && player.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
}
