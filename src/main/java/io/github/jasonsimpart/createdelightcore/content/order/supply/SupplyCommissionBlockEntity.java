package io.github.jasonsimpart.createdelightcore.content.order.supply;

import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import io.github.jasonsimpart.createdelightcore.content.order.data.OrderDataManager;
import io.github.jasonsimpart.createdelightcore.content.order.data.OrderSupplyData;
import io.github.jasonsimpart.createdelightcore.content.util.MoneyUtil;
import io.github.jasonsimpart.createdelightcore.registry.CDBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
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
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class SupplyCommissionBlockEntity extends SmartBlockEntity {
    public static final int VOUCHER_SLOT = 0;
    public static final int OUTPUT_SLOT = 1;
    public static final int GRACE_TICKS = 1200;
    private static final ResourceLocation VOUCHER_ID = ResourceLocation.fromNamespaceAndPath("lightmanscurrency", "ticket");
    private static final int VOUCHER_COLOR = 14464140;
    private static final int VOUCHER_TYPE = -10;
    private static final String PACKAGE_OWNER_TAG = "CDSupplyCommissionOwner";
    private static final String PACKAGE_OWNER_NAME_TAG = "CDSupplyCommissionOwnerName";
    private static final String PACKAGE_ITEM_TAG = "CDSupplyCommissionItem";
    private static final String PACKAGE_COUNT_TAG = "CDSupplyCommissionCount";

    private final ItemStackHandler inventory = new ItemStackHandler(2) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return slot == VOUCHER_SLOT && isGuildVoucher(stack);
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChangedAndSync();
        }
    };
    private final LazyOptional<IItemHandler> combinedCapability = LazyOptional.of(() -> safeHandler(-1, true, true));
    private final LazyOptional<IItemHandler> voucherCapability = LazyOptional.of(() -> safeHandler(VOUCHER_SLOT, true, false));
    private final LazyOptional<IItemHandler> outputCapability = LazyOptional.of(() -> safeHandler(OUTPUT_SLOT, false, true));
    private final List<SupplyCommissionRecord> commissions = new ArrayList<>();
    private final List<ItemStack> readyPackages = new ArrayList<>();

    public SupplyCommissionBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public SupplyCommissionBlockEntity(BlockPos pos, BlockState state) {
        this(CDBlockEntities.SUPPLY_COMMISSION_TABLE.get(), pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
    }

    @Override
    public void tick() {
        super.tick();
        if (level == null || level.isClientSide || level.getGameTime() % 20L != 0L) {
            return;
        }
        deliverCompletedCommissions();
    }

    public ItemStackHandler inventory() {
        return inventory;
    }

    public List<SupplyCommissionRecord> commissions() {
        return List.copyOf(commissions);
    }

    public List<ItemStack> readyPackages() {
        return readyPackages.stream().map(ItemStack::copy).toList();
    }

    public int pendingPickupCount() {
        return readyPackages.size() + (inventory.getStackInSlot(OUTPUT_SLOT).isEmpty() ? 0 : 1);
    }

    public boolean canTakeOutput(Player player) {
        ItemStack output = inventory.getStackInSlot(OUTPUT_SLOT);
        if (output.isEmpty() || player.isCreative()) {
            return true;
        }
        CompoundTag tag = output.getTag();
        return tag == null || !tag.hasUUID(PACKAGE_OWNER_TAG)
                || tag.getUUID(PACKAGE_OWNER_TAG).equals(player.getUUID());
    }

    public String outputOwnerName() {
        ItemStack output = inventory.getStackInSlot(OUTPUT_SLOT);
        CompoundTag tag = output.getTag();
        return tag == null ? "" : tag.getString(PACKAGE_OWNER_NAME_TAG);
    }

    public SubmissionResult submit(ServerPlayer player, ResourceLocation target) {
        Optional<OrderSupplyData> optionalEntry = OrderDataManager.supply(target);
        if (optionalEntry.isEmpty()) {
            return SubmissionResult.CATALOG_MISSING;
        }
        OrderSupplyData entry = optionalEntry.get();
        SupplyCommissionPlayerData.discoverInventorySupplies(player);
        if (!SupplyCommissionPlayerData.isSupplyUnlocked(player, entry.item())) {
            return SubmissionResult.ITEM_NOT_DISCOVERED;
        }
        if (!player.isCreative() && countVouchers() < entry.tickets()) {
            return SubmissionResult.VOUCHERS_MISSING;
        }

        var money = MoneyUtil.baseCoinNumberToCoinValue(entry.money());
        if (!player.isCreative() && !MoneyUtil.playerCanAfford(player, money)) {
            return SubmissionResult.MONEY_MISSING;
        }
        if (!player.isCreative() && !MoneyUtil.extractPlayerMoney(player, money)) {
            return SubmissionResult.PAYMENT_FAILED;
        }
        if (!player.isCreative()
                && inventory.extractItem(VOUCHER_SLOT, entry.tickets(), false).getCount() < entry.tickets()) {
            MoneyUtil.insertPlayerMoney(player, money);
            return SubmissionResult.PAYMENT_FAILED;
        }

        long now = player.level().getGameTime();
        commissions.add(SupplyCommissionRecord.create(player.getUUID(), player.getGameProfile().getName(), entry, now));
        setChangedAndSync();
        return SubmissionResult.SUCCESS;
    }

    public CancellationResult cancel(ServerPlayer player, UUID commissionId) {
        for (int i = 0; i < commissions.size(); i++) {
            SupplyCommissionRecord record = commissions.get(i);
            if (!record.id().equals(commissionId)) {
                continue;
            }
            if (!record.owner().equals(player.getUUID())) {
                return CancellationResult.NOT_OWNER;
            }
            long now = player.level().getGameTime();
            if (now >= record.dueTime()) {
                return CancellationResult.ALREADY_COMPLETE;
            }

            boolean grace = now - record.createdTime() <= GRACE_TICKS;
            int refundMoney = grace ? record.money() : record.money() / 2;
            int refundTickets = grace ? record.tickets() : 0;
            if (refundMoney > 0) {
                MoneyUtil.insertPlayerMoney(player, MoneyUtil.baseCoinNumberToCoinValue(refundMoney));
            }
            if (refundTickets > 0) {
                giveOrDrop(player, guildVoucher(refundTickets));
            }
            commissions.remove(i);
            setChangedAndSync();
            return grace ? CancellationResult.FULL_REFUND : CancellationResult.PARTIAL_REFUND;
        }
        return CancellationResult.NOT_FOUND;
    }

    public boolean hasActiveCommissions() {
        return !commissions.isEmpty() || pendingPickupCount() > 0;
    }

    public void releaseCommissionsOnRemoval() {
        commissions.clear();
        readyPackages.clear();
        setChanged();
    }

    private void deliverCompletedCommissions() {
        if (level == null || level.isClientSide) {
            return;
        }
        long now = level.getGameTime();
        Iterator<SupplyCommissionRecord> iterator = commissions.iterator();
        while (iterator.hasNext()) {
            SupplyCommissionRecord record = iterator.next();
            if (now < record.dueTime()) {
                continue;
            }
            Item item = BuiltInRegistries.ITEM.get(record.targetItem());
            if (item == net.minecraft.world.item.Items.AIR) {
                continue;
            }
            readyPackages.add(createPackage(record, item));
            iterator.remove();
            setChangedAndSync();
            ServerPlayer owner = level.getServer().getPlayerList().getPlayer(record.owner());
            if (owner != null && (!inventory.getStackInSlot(OUTPUT_SLOT).isEmpty() || readyPackages.size() > 1)) {
                owner.sendSystemMessage(net.minecraft.network.chat.Component.translatable(
                        "createdelightcore.supply_commission.completed_waiting",
                        new ItemStack(item).getHoverName(), record.count(), getBlockPos().toShortString()));
            }
        }
        if (!inventory.getStackInSlot(OUTPUT_SLOT).isEmpty() || readyPackages.isEmpty()) {
            return;
        }
        ItemStack packageStack = readyPackages.remove(0);
        inventory.setStackInSlot(OUTPUT_SLOT, packageStack);
        setChangedAndSync();
        CompoundTag packageTag = packageStack.getTag();
        if (packageTag != null && packageTag.hasUUID(PACKAGE_OWNER_TAG)) {
            ServerPlayer owner = level.getServer().getPlayerList().getPlayer(packageTag.getUUID(PACKAGE_OWNER_TAG));
            if (owner != null) {
                ResourceLocation target = ResourceLocation.tryParse(packageTag.getString(PACKAGE_ITEM_TAG));
                ItemStack contained = target == null ? ItemStack.EMPTY
                        : new ItemStack(BuiltInRegistries.ITEM.get(target), Math.max(1, packageTag.getInt(PACKAGE_COUNT_TAG)));
                owner.sendSystemMessage(net.minecraft.network.chat.Component.translatable(
                        "createdelightcore.supply_commission.arrived",
                        contained.getHoverName(), contained.getCount(), getBlockPos().toShortString()));
            }
        }
    }

    private static ItemStack createPackage(SupplyCommissionRecord record, Item item) {
        ItemStack packageStack = PackageItem.containing(List.of(new ItemStack(item, record.count())));
        PackageItem.addAddress(packageStack, record.ownerName());
        CompoundTag packageTag = packageStack.getOrCreateTag();
        packageTag.putUUID(PACKAGE_OWNER_TAG, record.owner());
        packageTag.putString(PACKAGE_OWNER_NAME_TAG, record.ownerName());
        packageTag.putString(PACKAGE_ITEM_TAG, record.targetItem().toString());
        packageTag.putInt(PACKAGE_COUNT_TAG, record.count());
        return packageStack;
    }

    private int countVouchers() {
        ItemStack stack = inventory.getStackInSlot(VOUCHER_SLOT);
        return isGuildVoucher(stack) ? stack.getCount() : 0;
    }

    private static boolean isGuildVoucher(ItemStack stack) {
        if (stack.isEmpty() || !BuiltInRegistries.ITEM.getKey(stack.getItem()).equals(VOUCHER_ID) || !stack.hasTag()) {
            return false;
        }
        CompoundTag tag = stack.getTag();
        return tag != null && tag.getInt("TicketColor") == VOUCHER_COLOR && tag.getInt("TicketID") == VOUCHER_TYPE;
    }

    public static ItemStack guildVoucher(int count) {
        ItemStack stack = new ItemStack(BuiltInRegistries.ITEM.get(VOUCHER_ID), Math.max(1, count));
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt("TicketColor", VOUCHER_COLOR);
        tag.putInt("TicketID", VOUCHER_TYPE);
        return stack;
    }

    private static void giveOrDrop(ServerPlayer player, ItemStack stack) {
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
    }

    private IItemHandler safeHandler(int exposedSlot, boolean allowInsert, boolean allowExtract) {
        return new IItemHandler() {
            @Override
            public int getSlots() {
                return exposedSlot < 0 ? 2 : 1;
            }

            private int mappedSlot(int slot) {
                return exposedSlot < 0 ? slot : slot == 0 ? exposedSlot : -1;
            }

            @Override
            public ItemStack getStackInSlot(int slot) {
                int mapped = mappedSlot(slot);
                return mapped >= 0 && mapped < 2 ? inventory.getStackInSlot(mapped) : ItemStack.EMPTY;
            }

            @Override
            public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
                int mapped = mappedSlot(slot);
                return allowInsert && mapped == VOUCHER_SLOT
                        ? inventory.insertItem(mapped, stack, simulate) : stack;
            }

            @Override
            public ItemStack extractItem(int slot, int amount, boolean simulate) {
                int mapped = mappedSlot(slot);
                boolean permitted = allowExtract && mapped == OUTPUT_SLOT;
                return permitted ? inventory.extractItem(mapped, amount, simulate) : ItemStack.EMPTY;
            }

            @Override
            public int getSlotLimit(int slot) {
                int mapped = mappedSlot(slot);
                return mapped >= 0 && mapped < 2 ? inventory.getSlotLimit(mapped) : 0;
            }

            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                return allowInsert && mappedSlot(slot) == VOUCHER_SLOT && isGuildVoucher(stack);
            }
        };
    }

    private void setChangedAndSync() {
        setChanged();
        if (level != null) {
            sendData();
        }
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            if (side == Direction.DOWN) {
                return outputCapability.cast();
            }
            if (side != null) {
                return voucherCapability.cast();
            }
            return combinedCapability.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        combinedCapability.invalidate();
        voucherCapability.invalidate();
        outputCapability.invalidate();
    }

    @Override
    protected void read(CompoundTag tag, boolean clientPacket) {
        super.read(tag, clientPacket);
        inventory.deserializeNBT(tag.getCompound("Inventory"));
        commissions.clear();
        ListTag list = tag.getList("Commissions", Tag.TAG_COMPOUND);
        for (Tag value : list) {
            SupplyCommissionRecord.read((CompoundTag) value).ifPresent(commissions::add);
        }
        readyPackages.clear();
        ListTag readyList = tag.getList("ReadyPackages", Tag.TAG_COMPOUND);
        for (Tag value : readyList) {
            ItemStack packageStack = ItemStack.of((CompoundTag) value);
            if (!packageStack.isEmpty()) {
                readyPackages.add(packageStack);
            }
        }
    }

    @Override
    protected void write(CompoundTag tag, boolean clientPacket) {
        super.write(tag, clientPacket);
        writeState(tag);
    }

    @Override
    public void writeSafe(CompoundTag tag) {
        super.writeSafe(tag);
        writeState(tag);
    }

    private void writeState(CompoundTag tag) {
        tag.put("Inventory", inventory.serializeNBT());
        ListTag list = new ListTag();
        commissions.forEach(record -> list.add(record.write()));
        tag.put("Commissions", list);
        ListTag readyList = new ListTag();
        readyPackages.forEach(packageStack -> readyList.add(packageStack.save(new CompoundTag())));
        tag.put("ReadyPackages", readyList);
    }

    public enum SubmissionResult {
        SUCCESS,
        CATALOG_MISSING,
        ITEM_NOT_DISCOVERED,
        VOUCHERS_MISSING,
        MONEY_MISSING,
        PAYMENT_FAILED
    }

    public enum CancellationResult {
        FULL_REFUND,
        PARTIAL_REFUND,
        NOT_OWNER,
        ALREADY_COMPLETE,
        NOT_FOUND
    }
}
