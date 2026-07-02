package io.github.jasonsimpart.createdelightcore.content.order.machine;

import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.content.logistics.packager.InventorySummary;
import com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBehaviour;
import com.simibubi.create.content.logistics.stockTicker.PackageOrderWithCrafts;
import io.github.jasonsimpart.createdelightcore.content.order.OrderEntry;
import io.github.jasonsimpart.createdelightcore.content.order.OrderGoodsQuality;
import io.github.jasonsimpart.createdelightcore.content.order.OrderInfo;
import io.github.jasonsimpart.createdelightcore.content.order.OrderRequestMode;
import io.github.jasonsimpart.createdelightcore.content.order.OrderRequestResolver;
import io.github.jasonsimpart.createdelightcore.content.order.OrderRequestSelection;
import io.github.jasonsimpart.createdelightcore.content.order.OrderRequestStrategy;
import io.github.jasonsimpart.createdelightcore.registry.CDBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OrderRequesterBlockEntity extends OrderMachineBlockEntity {
    private OrderRequestStrategy requestStrategy = OrderRequestStrategy.empty();
    private String targetAddress = "";
    private boolean allowPartialRequests;
    private boolean redstonePowered;
    private boolean lastRequestSucceeded;

    public OrderRequesterBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public OrderRequesterBlockEntity(BlockPos pos, BlockState state) {
        this(CDBlockEntities.ORDER_REQUESTER.get(), pos, state);
    }

    public List<OrderRequestSelection> getSelectedSelections() {
        return requestStrategy.fixedSelections();
    }

    public List<BigItemStack> getSelectedStacks() {
        return flattenSelections(requestStrategy.fixedSelections());
    }

    public OrderRequestStrategy getRequestStrategy() {
        return requestStrategy;
    }

    public String getTargetAddress() {
        return targetAddress;
    }

    public boolean isAllowPartialRequests() {
        return allowPartialRequests;
    }

    public boolean didLastRequestSucceed() {
        return lastRequestSucceeded;
    }

    public void setSelectedRequest(List<OrderRequestSelection> selections, String targetAddress, boolean allowPartialRequests) {
        setRequestStrategy(new OrderRequestStrategy(OrderRequestMode.FIXED_COUNT, selections, requestStrategy.ratioSelections()),
                targetAddress, allowPartialRequests);
    }

    public void setRequestStrategy(OrderRequestStrategy strategy, String targetAddress, boolean allowPartialRequests) {
        this.requestStrategy = sanitizeStrategy(strategy);
        this.targetAddress = targetAddress == null ? "" : targetAddress;
        this.allowPartialRequests = allowPartialRequests;
        this.lastRequestSucceeded = false;
        setChanged();
    }

    public void clearSelectedRequest() {
        requestStrategy = OrderRequestStrategy.empty();
        targetAddress = "";
        allowPartialRequests = false;
        lastRequestSucceeded = false;
        setChanged();
    }

    public void onRedstonePowerChanged(boolean powered) {
        if (redstonePowered == powered) {
            return;
        }
        redstonePowered = powered;
        lastRequestSucceeded = false;
        if (powered) {
            triggerSelectedRequest();
        }
        setChanged();
    }

    public boolean triggerSelectedRequest() {
        List<OrderRequestSelection> selections = resolveActiveSelections();
        if (selections.isEmpty()) {
            return false;
        }
        if (!allowPartialRequests && !hasEnoughSelectedStacks()) {
            lastRequestSucceeded = false;
            setChanged();
            return false;
        }

        PackageOrderWithCrafts order = PackageOrderWithCrafts.simple(flattenSelections(selections));
        lastRequestSucceeded = broadcastPackageRequest(
                LogisticallyLinkedBehaviour.RequestType.REDSTONE,
                order,
                null,
                targetAddress
        );
        if (lastRequestSucceeded) {
            outputOrderHeaderPackage();
        }
        setChanged();
        return lastRequestSucceeded;
    }

    private void outputOrderHeaderPackage() {
        ItemStack orderStack = getOrderStack();
        if (level == null || level.isClientSide || !OrderInfo.isOrder(orderStack)) {
            return;
        }

        ItemStack packageStack = PackageItem.containing(List.of(orderStack.copyWithCount(1)));
        if (!targetAddress.isBlank()) {
            PackageItem.addAddress(packageStack, targetAddress);
        }

        inventory.extractItem(ORDER_SLOT, 1, false);
        ItemStack remainder = insertIntoAdjacentInventory(packageStack);
        if (!remainder.isEmpty()) {
            ItemEntity entity = new ItemEntity(level,
                    worldPosition.getX() + 0.5,
                    worldPosition.getY() + 1.1,
                    worldPosition.getZ() + 0.5,
                    remainder);
            entity.setDeltaMovement(0, 0.05, 0);
            level.addFreshEntity(entity);
        }
    }

    private ItemStack insertIntoAdjacentInventory(ItemStack stack) {
        ItemStack remainder = stack;
        for (Direction direction : Direction.values()) {
            if (remainder.isEmpty()) {
                break;
            }
            BlockPos targetPos = worldPosition.relative(direction);
            if (level == null) {
                break;
            }
            var blockEntity = level.getBlockEntity(targetPos);
            if (blockEntity == null) {
                continue;
            }
            IItemHandler handler = blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, direction.getOpposite())
                    .orElse(null);
            if (handler == null) {
                continue;
            }
            remainder = ItemHandlerHelper.insertItemStacked(handler, remainder, false);
        }
        return remainder;
    }

    public boolean hasEnoughSelectedStacks() {
        List<OrderRequestSelection> selections = resolveActiveSelections();
        if (selections.isEmpty()) {
            return false;
        }

        InventorySummary summary = getAccurateSummary();
        for (BigItemStack selected : flattenSelections(selections)) {
            if (summary.getCountOf(selected.stack) < selected.count) {
                return false;
            }
        }
        return true;
    }

    public List<OrderRequestSelection> resolveActiveSelections() {
        if (requestStrategy.mode() == OrderRequestMode.RATIO) {
            OrderRequestResolver.RatioResult result =
                    OrderRequestResolver.resolveRatio(getAccurateCandidateGroups(), requestStrategy.ratioSelections());
            return result.complete() ? result.selections() : List.of();
        }
        return sanitizeSelections(requestStrategy.fixedSelections());
    }

    private OrderRequestStrategy sanitizeStrategy(OrderRequestStrategy strategy) {
        if (strategy == null) {
            return OrderRequestStrategy.empty();
        }
        return new OrderRequestStrategy(
                strategy.mode(),
                sanitizeSelections(strategy.fixedSelections()),
                strategy.ratioSelections()
        );
    }

    private List<OrderRequestSelection> sanitizeSelections(List<OrderRequestSelection> selections) {
        Optional<OrderInfo> orderInfo = getOrderInfo();
        if (orderInfo.isEmpty()) {
            return selections;
        }

        List<OrderRequestSelection> sanitized = new ArrayList<>();
        for (OrderEntry entry : orderInfo.get().entries()) {
            int remaining = entry.count();
            for (OrderRequestSelection selection : selections) {
                if (remaining <= 0 || selection == null || !entry.key().equals(selection.entryId())) {
                    continue;
                }
                int quality = OrderGoodsQuality.getQuality(selection.stack(), entry.id());
                if (quality < entry.minQuality()) {
                    continue;
                }
                int count = Math.min(remaining, selection.count());
                if (count <= 0) {
                    continue;
                }
                sanitized.add(new OrderRequestSelection(entry.key(), selection.stack(), count, quality));
                remaining -= count;
            }
        }
        return List.copyOf(sanitized);
    }

    private static List<BigItemStack> flattenSelections(List<OrderRequestSelection> selections) {
        List<BigItemStack> stacks = new ArrayList<>();
        for (OrderRequestSelection selection : selections) {
            if (selection == null || !selection.isValid()) {
                continue;
            }

            BigItemStack existing = findSameStack(stacks, selection);
            if (existing != null) {
                existing.count += selection.count();
            } else {
                stacks.add(selection.asBigItemStack());
            }
        }
        return List.copyOf(stacks);
    }

    private static BigItemStack findSameStack(List<BigItemStack> stacks, OrderRequestSelection selection) {
        for (BigItemStack stack : stacks) {
            if (net.minecraft.world.item.ItemStack.isSameItemSameTags(stack.stack, selection.stack())) {
                return stack;
            }
        }
        return null;
    }

    @Override
    protected void read(CompoundTag tag, boolean clientPacket) {
        super.read(tag, clientPacket);
        allowPartialRequests = tag.getBoolean("AllowPartial");
        redstonePowered = tag.getBoolean("Powered");
        lastRequestSucceeded = tag.getBoolean("Success");
        targetAddress = tag.getString("TargetAddress");

        if (tag.contains("RequestStrategy", Tag.TAG_COMPOUND)) {
            requestStrategy = sanitizeStrategy(OrderRequestStrategy.read(tag.getCompound("RequestStrategy")));
        } else {
            ListTag list = tag.getList("SelectedSelections", Tag.TAG_COMPOUND);
            if (list.isEmpty()) {
                list = tag.getList("SelectedStacks", Tag.TAG_COMPOUND);
            }
            requestStrategy = sanitizeStrategy(new OrderRequestStrategy(
                    OrderRequestMode.FIXED_COUNT,
                    OrderRequestStrategy.readFixed(list),
                    List.of()
            ));
        }
    }

    @Override
    protected void write(CompoundTag tag, boolean clientPacket) {
        super.write(tag, clientPacket);
        tag.putBoolean("AllowPartial", allowPartialRequests);
        tag.putBoolean("Powered", redstonePowered);
        tag.putBoolean("Success", lastRequestSucceeded);
        tag.putString("TargetAddress", targetAddress);
        tag.put("RequestStrategy", requestStrategy.write());
    }

    @Override
    public void writeSafe(CompoundTag tag) {
        super.writeSafe(tag);
        tag.putBoolean("AllowPartial", allowPartialRequests);
        tag.putString("TargetAddress", targetAddress);
        tag.put("RequestStrategy", requestStrategy.write());
    }
}
