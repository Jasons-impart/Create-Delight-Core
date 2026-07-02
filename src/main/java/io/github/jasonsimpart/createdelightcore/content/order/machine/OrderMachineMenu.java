package io.github.jasonsimpart.createdelightcore.content.order.machine;

import io.github.jasonsimpart.createdelightcore.content.order.OrderEntryCandidates;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

import java.util.ArrayList;
import java.util.List;

public abstract class OrderMachineMenu extends AbstractContainerMenu {
    private static final int PLAYER_INVENTORY_X = 8;
    private static final int PLAYER_INVENTORY_Y = 202;
    private static final int HOTBAR_Y = 260;

    protected final Inventory playerInventory;
    protected final BlockPos pos;
    protected final OrderMachineBlockEntity blockEntity;
    private List<OrderEntryCandidates> candidateGroups = List.of();

    protected OrderMachineMenu(MenuType<?> type, int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(type, containerId, playerInventory, getBlockEntity(playerInventory.player.level(), data.readBlockPos()));
    }

    protected OrderMachineMenu(MenuType<?> type, int containerId, Inventory playerInventory,
                               OrderMachineBlockEntity blockEntity) {
        super(type, containerId);
        this.playerInventory = playerInventory;
        this.blockEntity = blockEntity;
        this.pos = blockEntity != null ? blockEntity.getBlockPos() : BlockPos.ZERO;

        addSlot(new SlotItemHandler(blockEntity != null ? blockEntity.inventory : new ItemStackHandler(1),
                OrderMachineBlockEntity.ORDER_SLOT, 28, 42));
        addPlayerInventory(playerInventory);
    }

    private static OrderMachineBlockEntity getBlockEntity(Level level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        return blockEntity instanceof OrderMachineBlockEntity orderMachine ? orderMachine : null;
    }

    public BlockPos getPos() {
        return pos;
    }

    public OrderMachineBlockEntity getBlockEntity() {
        return blockEntity;
    }

    public List<OrderEntryCandidates> getCandidateGroups() {
        return candidateGroups;
    }

    public void setCandidateGroups(List<OrderEntryCandidates> candidateGroups) {
        this.candidateGroups = List.copyOf(candidateGroups);
    }

    public boolean isRequester() {
        return false;
    }

    @Override
    public boolean stillValid(Player player) {
        return blockEntity != null && !blockEntity.isRemoved()
                && player.level().getBlockEntity(pos) == blockEntity
                && player.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack original = slot.getItem();
        ItemStack copy = original.copy();
        if (index == 0) {
            if (!moveItemStackTo(original, 1, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(original, 0, 1, false)) {
            return ItemStack.EMPTY;
        }

        if (original.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return copy;
    }

    private void addPlayerInventory(Inventory inventory) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9,
                        PLAYER_INVENTORY_X + column * 18, PLAYER_INVENTORY_Y + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, PLAYER_INVENTORY_X + column * 18, HOTBAR_Y));
        }
    }
}
