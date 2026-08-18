package io.github.jasonsimpart.createdelightcore.content.order.supply;

import io.github.jasonsimpart.createdelightcore.content.order.data.OrderDataManager;
import io.github.jasonsimpart.createdelightcore.content.order.data.OrderSupplyData;
import io.github.jasonsimpart.createdelightcore.registry.CDBlocks;
import io.github.jasonsimpart.createdelightcore.registry.CDMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.SlotItemHandler;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class SupplyCommissionMenu extends AbstractContainerMenu {
    private static final int DATA_COUNT = 10;
    private static final int MACHINE_SLOT_COUNT = 2;
    private static final int PLAYER_INVENTORY_START = MACHINE_SLOT_COUNT;
    private final BlockPos pos;
    private final SupplyCommissionBlockEntity blockEntity;
    private final SelectionState selection;
    private final ContainerData data;

    public SupplyCommissionMenu(int containerId, Inventory inventory, FriendlyByteBuf data) {
        this(containerId, inventory, blockEntity(inventory, data.readBlockPos()),
                new SelectionState(), new SimpleContainerData(DATA_COUNT));
    }

    public SupplyCommissionMenu(int containerId, Inventory inventory, SupplyCommissionBlockEntity blockEntity) {
        this(containerId, inventory, blockEntity, new SelectionState(), null);
    }

    private SupplyCommissionMenu(int containerId, Inventory inventory, SupplyCommissionBlockEntity blockEntity,
                                 SelectionState selection, ContainerData suppliedData) {
        super(CDMenus.SUPPLY_COMMISSION_TABLE.get(), containerId);
        this.blockEntity = blockEntity;
        this.pos = blockEntity == null ? BlockPos.ZERO : blockEntity.getBlockPos();
        this.selection = selection;

        if (inventory.player instanceof ServerPlayer serverPlayer && blockEntity != null) {
            refreshCatalog(serverPlayer, null);
        }
        this.data = suppliedData == null ? serverData(inventory, blockEntity, selection) : suppliedData;

        var handler = blockEntity == null ? new net.minecraftforge.items.ItemStackHandler(2) : blockEntity.inventory();
        addSlot(new SlotItemHandler(handler, SupplyCommissionBlockEntity.VOUCHER_SLOT, 20, 36));
        addSlot(new SlotItemHandler(handler, SupplyCommissionBlockEntity.OUTPUT_SLOT, 20, 80) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

            @Override
            public boolean mayPickup(Player player) {
                boolean allowed = blockEntity == null || blockEntity.canTakeOutput(player);
                if (!allowed && !player.level().isClientSide) {
                    player.displayClientMessage(Component.translatable(
                            "createdelightcore.supply_commission.output_reserved",
                            blockEntity.outputOwnerName()), true);
                }
                return allowed;
            }
        });
        addPlayerInventory(inventory);
        addDataSlots(this.data);
    }

    private static SupplyCommissionBlockEntity blockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
        return blockEntity instanceof SupplyCommissionBlockEntity table ? table : null;
    }

    private static ContainerData serverData(Inventory inventory, SupplyCommissionBlockEntity blockEntity,
                                            SelectionState selection) {
        return new ContainerData() {
            @Override
            public int get(int index) {
                if (!(inventory.player instanceof ServerPlayer player) || blockEntity == null) {
                    return 0;
                }
                OrderSupplyData entry = selection.entry().orElse(null);
                selection.clampQueuePage(blockEntity.commissions().size());
                return switch (index) {
                    case 0 -> entry == null ? 0 : entry.count();
                    case 1 -> entry == null ? 0 : entry.tickets();
                    case 2 -> entry == null ? 0 : entry.money();
                    case 3 -> entry == null ? 0 : entry.days() * 24000;
                    case 4 -> selection.catalog.size();
                    case 5 -> selection.selectedIndex;
                    case 6 -> blockEntity.pendingPickupCount();
                    case 7 -> selection.queuePage;
                    case 8 -> selection.queuePages(blockEntity.commissions().size());
                    case 9 -> entry == null ? 0 : BuiltInRegistries.ITEM.getId(
                            BuiltInRegistries.ITEM.get(entry.item()));
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
            }

            @Override
            public int getCount() {
                return DATA_COUNT;
            }
        };
    }

    private void refreshCatalog(ServerPlayer player, ResourceLocation preferred) {
        selection.refresh(player, preferred);
        broadcastChanges();
    }

    public void refreshCatalogAfterDiscovery(ServerPlayer player) {
        refreshCatalog(player, selection.target());
    }

    private void cycleCatalog(ServerPlayer player, int direction) {
        if (selection.catalog.isEmpty()) {
            refreshCatalog(player, null);
            return;
        }
        selection.selectedIndex = Math.floorMod(selection.selectedIndex + direction, selection.catalog.size());
        refreshCatalog(player, selection.target());
    }

    public int previewCount() { return data.get(0); }
    public int previewTickets() { return data.get(1); }
    public int previewMoney() { return data.get(2); }
    public int previewDurationTicks() { return data.get(3); }
    public int catalogCount() { return data.get(4); }
    public int selectedIndex() { return data.get(5); }
    public int readyCount() { return data.get(6); }
    public int queuePage() { return data.get(7); }
    public int queuePages() { return Math.max(1, data.get(8)); }
    public ItemStack previewStack() { return new ItemStack(BuiltInRegistries.ITEM.byId(data.get(9))); }
    public SupplyCommissionBlockEntity getBlockEntity() { return blockEntity; }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (!(player instanceof ServerPlayer serverPlayer) || blockEntity == null) {
            return false;
        }
        if (id == 0) {
            SupplyCommissionBlockEntity.SubmissionResult result = blockEntity.submit(serverPlayer, selection.target());
            serverPlayer.sendSystemMessage(Component.translatable(
                    "createdelightcore.supply_commission.submit." + result.name().toLowerCase()));
            broadcastChanges();
            return true;
        }
        if (id == 1 || id == 2) {
            cycleCatalog(serverPlayer, id == 1 ? -1 : 1);
            return true;
        }
        if (id == 3 || id == 4) {
            selection.cycleQueuePage(blockEntity.commissions().size(), id == 3 ? -1 : 1);
            broadcastChanges();
            return true;
        }
        int index = selection.queuePage * 3 + id - 5;
        if (index >= 0 && index < blockEntity.commissions().size()) {
            SupplyCommissionBlockEntity.CancellationResult result = blockEntity.cancel(
                    serverPlayer, blockEntity.commissions().get(index).id());
            serverPlayer.sendSystemMessage(Component.translatable(
                    "createdelightcore.supply_commission.cancel." + result.name().toLowerCase()));
            selection.clampQueuePage(blockEntity.commissions().size());
            broadcastChanges();
            return true;
        }
        return false;
    }

    @Override
    public boolean stillValid(Player player) {
        return blockEntity != null && !blockEntity.isRemoved()
                && player.level().getBlockEntity(pos) == blockEntity
                && player.level().getBlockState(pos).is(CDBlocks.SUPPLY_COMMISSION_TABLE.get())
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
        if (!slot.mayPickup(player)) {
            return ItemStack.EMPTY;
        }
        if (index < MACHINE_SLOT_COUNT) {
            if (!moveItemStackTo(original, PLAYER_INVENTORY_START, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(original, SupplyCommissionBlockEntity.VOUCHER_SLOT,
                SupplyCommissionBlockEntity.VOUCHER_SLOT + 1, false)) {
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
                addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 172 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 8 + column * 18, 230));
        }
    }

    private static final class SelectionState {
        private final List<ResourceLocation> catalog = new ArrayList<>();
        private int selectedIndex = -1;
        private int queuePage;

        private void refresh(ServerPlayer player, ResourceLocation preferred) {
            ResourceLocation previous = preferred != null ? preferred : target();
            SupplyCommissionPlayerData.discoverInventorySupplies(player);
            catalog.clear();
            OrderDataManager.supplyCatalog().values().stream()
                    .filter(entry -> SupplyCommissionPlayerData.isSupplyUnlocked(player, entry.item()))
                    .map(OrderSupplyData::item)
                    .sorted(Comparator.comparing(ResourceLocation::toString))
                    .forEach(catalog::add);
            if (catalog.isEmpty()) {
                selectedIndex = -1;
                return;
            }
            int preferredIndex = previous == null ? -1 : catalog.indexOf(previous);
            selectedIndex = preferredIndex >= 0 ? preferredIndex : Math.max(0, Math.min(selectedIndex, catalog.size() - 1));
        }

        private ResourceLocation target() {
            return selectedIndex >= 0 && selectedIndex < catalog.size() ? catalog.get(selectedIndex) : null;
        }

        private Optional<OrderSupplyData> entry() {
            return OrderDataManager.supply(target());
        }

        private int queuePages(int size) {
            return Math.max(1, (size + 2) / 3);
        }

        private void clampQueuePage(int size) {
            queuePage = Math.max(0, Math.min(queuePage, queuePages(size) - 1));
        }

        private void cycleQueuePage(int size, int direction) {
            queuePage = Math.floorMod(queuePage + direction, queuePages(size));
        }
    }
}
