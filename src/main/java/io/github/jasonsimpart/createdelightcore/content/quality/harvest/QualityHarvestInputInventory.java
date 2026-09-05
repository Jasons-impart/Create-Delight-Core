package io.github.jasonsimpart.createdelightcore.content.quality.harvest;

import io.github.jasonsimpart.createdelightcore.registry.CDTags;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

public final class QualityHarvestInputInventory {
    public static final String INVENTORY_TAG = "CreateDelightCoreQualityHarvestInputs";
    public static final int LIFE_MATTER_SLOT = 0;
    public static final int CALIBRATOR_SLOT = 1;

    private QualityHarvestInputInventory() {
    }

    public static ItemStackHandler create() {
        return new ItemStackHandler(2) {
            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                return isValid(slot, stack);
            }

            @Override
            public int getSlotLimit(int slot) {
                return slot == CALIBRATOR_SLOT ? 1 : super.getSlotLimit(slot);
            }
        };
    }

    public static ItemStackHandler create(CompoundTag parentTag) {
        ItemStackHandler inventory = new ItemStackHandler(2) {
            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                return isValid(slot, stack);
            }

            @Override
            public int getSlotLimit(int slot) {
                return slot == CALIBRATOR_SLOT ? 1 : super.getSlotLimit(slot);
            }

            @Override
            protected void onContentsChanged(int slot) {
                parentTag.put(INVENTORY_TAG, serializeNBT());
            }
        };
        if (parentTag.contains(INVENTORY_TAG)) {
            inventory.deserializeNBT(parentTag.getCompound(INVENTORY_TAG));
        }
        return inventory;
    }

    public static boolean isValid(int slot, ItemStack stack) {
        if (stack.isEmpty()) {
            return true;
        }
        if (slot == LIFE_MATTER_SLOT) {
            return stack.is(CDTags.AllItemTags.LIFE_MATTER.tag);
        }
        return slot == CALIBRATOR_SLOT
                && stack.is(CDTags.AllItemTags.QUALITY_HARVEST_CALIBRATORS.tag);
    }
}
