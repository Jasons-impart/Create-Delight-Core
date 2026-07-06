package io.github.jasonsimpart.createdelightcore.content.quality.harvest;

import io.github.jasonsimpart.createdelightcore.registry.CDBlockEntities;
import io.github.jasonsimpart.createdelightcore.registry.CDTags;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemStackHandler;

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
        }
    };
    private int lifeMatterStored;
    private ItemStack lifeMatterPrototype = ItemStack.EMPTY;

    public QualityHarvestControllerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
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
        int inserted = Math.min(Math.min(maxAmount, stack.getCount()), CAPACITY - lifeMatterStored);
        lifeMatterStored += inserted;
        if (lifeMatterPrototype.isEmpty()) {
            lifeMatterPrototype = stack.copyWithCount(1);
        }
        setChanged();
        return inserted;
    }

    public boolean consumeLifeMatter(int amount) {
        if (amount <= 0 || lifeMatterStored < amount) {
            return false;
        }
        lifeMatterStored -= amount;
        setChanged();
        return true;
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
    public void load(CompoundTag tag) {
        super.load(tag);
        readControllerData(tag);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        writeControllerData(tag);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        writeControllerData(tag);
        return tag;
    }

    private void readControllerData(CompoundTag tag) {
        inventory.deserializeNBT(tag.getCompound(INVENTORY_TAG));
        lifeMatterStored = Math.max(0, Math.min(CAPACITY, tag.getInt(LIFE_MATTER_STORED_TAG)));
        lifeMatterPrototype = tag.contains(LIFE_MATTER_ITEM_TAG)
                ? ItemStack.of(tag.getCompound(LIFE_MATTER_ITEM_TAG))
                : ItemStack.EMPTY;
    }

    private void writeControllerData(CompoundTag tag) {
        tag.put(INVENTORY_TAG, inventory.serializeNBT());
        tag.putInt(LIFE_MATTER_STORED_TAG, lifeMatterStored);
        if (!lifeMatterPrototype.isEmpty()) {
            tag.put(LIFE_MATTER_ITEM_TAG, lifeMatterPrototype.save(new CompoundTag()));
        }
    }

    public static boolean isCalibrator(ItemStack stack) {
        return !stack.isEmpty() && stack.is(CDTags.AllItemTags.QUALITY_HARVEST_CALIBRATORS.tag);
    }

    public static boolean isLifeMatter(ItemStack stack) {
        return !stack.isEmpty() && stack.is(CDTags.AllItemTags.LIFE_MATTER.tag);
    }

    public static ItemStack getCalibrator(CompoundTag tag) {
        ItemStackHandler handler = new ItemStackHandler(1);
        handler.deserializeNBT(tag.getCompound(INVENTORY_TAG));
        return handler.getStackInSlot(CALIBRATOR_SLOT);
    }

    public static int getLifeMatterStored(CompoundTag tag) {
        return Math.max(0, Math.min(CAPACITY, tag.getInt(LIFE_MATTER_STORED_TAG)));
    }

    public static CompoundTag setLifeMatterStored(CompoundTag tag, int amount) {
        CompoundTag copy = tag.copy();
        copy.putInt(LIFE_MATTER_STORED_TAG, Math.max(0, Math.min(CAPACITY, amount)));
        return copy;
    }

    public static CompoundTag setLifeMatterStored(CompoundTag tag, int amount, ItemStack prototype) {
        CompoundTag copy = setLifeMatterStored(tag, amount);
        if (!prototype.isEmpty() && isLifeMatter(prototype)) {
            copy.put(LIFE_MATTER_ITEM_TAG, prototype.copyWithCount(1).save(new CompoundTag()));
        }
        return copy;
    }
}
