package io.github.jasonsimpart.createdelightcore.content.order.machine;

import com.simibubi.create.content.logistics.packager.InventorySummary;
import com.simibubi.create.content.logistics.stockTicker.StockCheckingBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import io.github.jasonsimpart.createdelightcore.content.order.OrderCandidateFinder;
import io.github.jasonsimpart.createdelightcore.content.order.OrderEntry;
import io.github.jasonsimpart.createdelightcore.content.order.OrderEntryCandidates;
import io.github.jasonsimpart.createdelightcore.content.order.OrderInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class OrderMachineBlockEntity extends StockCheckingBlockEntity {
    public static final int ORDER_SLOT = 0;

    protected final ItemStackHandler inventory;
    private final LazyOptional<IItemHandler> inventoryCapability;

    protected OrderMachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        inventory = new ItemStackHandler(1) {
            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                return slot == ORDER_SLOT && isValidOrderMachineStack(stack);
            }

            @Override
            public int getSlotLimit(int slot) {
                return 1;
            }

            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
            }
        };
        inventoryCapability = LazyOptional.of(() -> inventory);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return inventoryCapability.cast();
        }
        return super.getCapability(cap, side);
    }

    public ItemStack getOrderStack() {
        return inventory.getStackInSlot(ORDER_SLOT);
    }

    public boolean hasOrder() {
        return OrderInfo.isOrder(getOrderStack());
    }

    public Optional<OrderInfo> getOrderInfo() {
        return OrderInfo.fromStack(getOrderStack());
    }

    protected boolean isValidOrderMachineStack(ItemStack stack) {
        return OrderInfo.isOrder(stack);
    }

    public boolean setOrderStack(ItemStack stack) {
        if (!isValidOrderMachineStack(stack)) {
            return false;
        }
        inventory.setStackInSlot(ORDER_SLOT, stack.copyWithCount(1));
        setChanged();
        return true;
    }

    public ItemStack removeOrderStack() {
        ItemStack stack = inventory.extractItem(ORDER_SLOT, 1, false);
        setChanged();
        return stack;
    }

    public List<OrderEntryCandidates> getRecentCandidateGroups() {
        return getCandidateGroups(getRecentSummary());
    }

    public List<OrderEntryCandidates> getAccurateCandidateGroups() {
        return getCandidateGroups(getAccurateSummary());
    }

    public List<OrderEntryCandidates> getCandidateGroups(InventorySummary summary) {
        Optional<OrderInfo> order = getOrderInfo();
        if (order.isEmpty()) {
            return List.of();
        }

        List<OrderEntryCandidates> groups = new ArrayList<>();
        for (OrderEntry entry : order.get().entries()) {
            groups.add(new OrderEntryCandidates(entry, OrderCandidateFinder.findCandidates(summary, entry)));
        }
        return List.copyOf(groups);
    }

    @Override
    protected void read(CompoundTag tag, boolean clientPacket) {
        super.read(tag, clientPacket);
        inventory.deserializeNBT(tag.getCompound("Inventory"));
    }

    @Override
    protected void write(CompoundTag tag, boolean clientPacket) {
        super.write(tag, clientPacket);
        tag.put("Inventory", inventory.serializeNBT());
    }

    @Override
    public void writeSafe(CompoundTag tag) {
        super.writeSafe(tag);
        tag.put("Inventory", inventory.serializeNBT());
    }
}
