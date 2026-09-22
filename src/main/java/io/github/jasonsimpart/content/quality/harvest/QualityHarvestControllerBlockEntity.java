package io.github.jasonsimpart.content.quality.harvest;

import io.github.jasonsimpart.registry.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;

public class QualityHarvestControllerBlockEntity extends BlockEntity {
    public static final int CAPACITY = 512;
    public static final String INVENTORY_TAG = "Inventory";
    public static final String LIFE_MATTER_STORED_TAG = "LifeMatterStored";
    public static final String LIFE_MATTER_ITEM_TAG = "LifeMatterItem";
    private static final int CALIBRATOR_SLOT = 0;

    private final ItemStackHandler inventory = new ItemStackHandler(1) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return slot == CALIBRATOR_SLOT && isCalibrator(stack);
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            notifyUpdate();
        }
    };
    private int lifeMatterStored;
    private ItemStack lifeMatterPrototype = ItemStack.EMPTY;

    public QualityHarvestControllerBlockEntity(BlockPos pos, BlockState state) {
        super(io.github.jasonsimpart.registry.ModBlockEntities.QUALITY_HARVEST_CONTROLLER.get(), pos, state);
    }

    public ItemStack getCalibrator() {
        return inventory.getStackInSlot(CALIBRATOR_SLOT);
    }

    public boolean hasCalibrator() {
        return !getCalibrator().isEmpty();
    }

    public boolean setCalibrator(ItemStack stack) {
        if (!isCalibrator(stack) || hasCalibrator()) {
            return false;
        }
        inventory.setStackInSlot(CALIBRATOR_SLOT, stack.copyWithCount(1));
        setChanged();
        return true;
    }

    public ItemStack removeCalibrator() {
        ItemStack stack = inventory.extractItem(CALIBRATOR_SLOT, 1, false);
        setChanged();
        return stack;
    }

    public int getLifeMatterStored() {
        return lifeMatterStored;
    }

    public int getLifeMatterCapacity() {
        return CAPACITY;
    }

    public int insertLifeMatter(ItemStack stack, int maxAmount) {
        if (!isLifeMatter(stack) || maxAmount <= 0 || lifeMatterStored >= CAPACITY) {
            return 0;
        }
        if (!lifeMatterPrototype.isEmpty() && !ItemStack.isSameItemSameComponents(stack, lifeMatterPrototype)) {
            return 0;
        }
        int inserted = Math.min(Math.min(maxAmount, stack.getCount()), CAPACITY - lifeMatterStored);
        lifeMatterStored += inserted;
        if (lifeMatterPrototype.isEmpty()) {
            lifeMatterPrototype = stack.copyWithCount(1);
        }
        setChanged();
        notifyUpdate();
        return inserted;
    }

    public void dropContents() {
        if (level == null) {
            return;
        }
        ItemStack calibrator = removeCalibrator();
        if (!calibrator.isEmpty()) {
            Block.popResource(level, worldPosition, calibrator);
        }

        while (lifeMatterStored > 0 && !lifeMatterPrototype.isEmpty()) {
            int count = Math.min(lifeMatterStored, lifeMatterPrototype.getMaxStackSize());
            lifeMatterStored -= count;
            Block.popResource(level, worldPosition, lifeMatterPrototype.copyWithCount(count));
        }
        setChanged();
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        readControllerData(tag, registries);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        writeControllerData(tag, registries);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        writeControllerData(tag, registries);
        return tag;
    }

    private void readControllerData(CompoundTag tag, HolderLookup.Provider registries) {
        inventory.deserializeNBT(registries, tag.getCompound(INVENTORY_TAG));
        lifeMatterStored = Math.max(0, Math.min(CAPACITY, tag.getInt(LIFE_MATTER_STORED_TAG)));
        lifeMatterPrototype = tag.contains(LIFE_MATTER_ITEM_TAG)
                ? ItemStack.parseOptional(registries, tag.getCompound(LIFE_MATTER_ITEM_TAG))
                : ItemStack.EMPTY;
    }

    private void notifyUpdate() {
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
            level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
        }
    }

    private void writeControllerData(CompoundTag tag, HolderLookup.Provider registries) {
        tag.put(INVENTORY_TAG, inventory.serializeNBT(registries));
        tag.putInt(LIFE_MATTER_STORED_TAG, lifeMatterStored);
        if (!lifeMatterPrototype.isEmpty()) {
            tag.put(LIFE_MATTER_ITEM_TAG, lifeMatterPrototype.save(registries));
        }
    }

    public static boolean isCalibrator(ItemStack stack) {
        return !stack.isEmpty() && stack.is(ModTags.Items.QUALITY_HARVEST_CALIBRATORS);
    }

    public static boolean isLifeMatter(ItemStack stack) {
        return !stack.isEmpty() && stack.is(ModTags.Items.LIFE_MATTER);
    }

    public static ItemStack getCalibrator(CompoundTag tag, HolderLookup.Provider registries) {
        ItemStackHandler handler = new ItemStackHandler(1);
        handler.deserializeNBT(registries, tag.getCompound(INVENTORY_TAG));
        return handler.getStackInSlot(CALIBRATOR_SLOT);
    }

    public static int getLifeMatterStored(CompoundTag tag) {
        return Math.max(0, Math.min(CAPACITY, tag.getInt(LIFE_MATTER_STORED_TAG)));
    }

    public static CompoundTag setLifeMatterStored(CompoundTag tag, int amount) {
        CompoundTag copy = tag.copy();
        int clamped = Math.max(0, Math.min(CAPACITY, amount));
        copy.putInt(LIFE_MATTER_STORED_TAG, clamped);
        if (clamped == 0) {
            copy.remove(LIFE_MATTER_ITEM_TAG);
        }
        return copy;
    }

    public static CompoundTag setLifeMatterStored(CompoundTag tag, int amount, ItemStack prototype, HolderLookup.Provider registries) {
        CompoundTag copy = setLifeMatterStored(tag, amount);
        if (!prototype.isEmpty() && isLifeMatter(prototype)) {
            copy.put(LIFE_MATTER_ITEM_TAG, prototype.copyWithCount(1).save(registries));
        }
        return copy;
    }
}
