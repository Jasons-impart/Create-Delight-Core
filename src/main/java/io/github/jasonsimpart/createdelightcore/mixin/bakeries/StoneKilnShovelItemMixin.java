package io.github.jasonsimpart.createdelightcore.mixin.bakeries;

import com.renyigesai.bakeries.item.StoneKilnShovelItem;
import de.cadentem.quality_food.core.Quality;
import de.cadentem.quality_food.util.QualityUtils;
import io.github.jasonsimpart.createdelightcore.content.util.QualityFoodBlockUseContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StoneKilnShovelItem.class)
public class StoneKilnShovelItemMixin {
    @Unique
    private static final String create_Delight_Core$STORED_QUALITY_TAG = "CreateDelightCoreStoredQuality";
    @Unique
    private static final String create_Delight_Core$INVENTORY_TAG = "Inventory";

    @Inject(method = "addItem", at = @At("TAIL"), remap = false)
    private void create_Delight_Core$storeContainedItemQuality(ItemStack shovelStack, ItemStack containedStack, CallbackInfo ci) {
        CompoundTag qualityTag = create_Delight_Core$copyQualityTag(containedStack);
        if (qualityTag == null) {
            Quality quality = QualityFoodBlockUseContext.getQuality();
            if (QualityUtils.isValidQuality(quality)) {
                ItemStack qualityStack = containedStack.copy();
                QualityUtils.applyQuality(qualityStack, quality);
                qualityTag = create_Delight_Core$copyQualityTag(qualityStack);
            }
        }

        CompoundTag shovelTag = shovelStack.getOrCreateTag();
        if (qualityTag == null) {
            shovelTag.remove(create_Delight_Core$STORED_QUALITY_TAG);
            return;
        }
        shovelTag.put(create_Delight_Core$STORED_QUALITY_TAG, qualityTag);
        create_Delight_Core$applyQualityToStoredInventoryStack(shovelStack, qualityTag);
    }

    @Inject(method = "getInventoryStack", at = @At("RETURN"), cancellable = true, remap = false)
    private void create_Delight_Core$restoreContainedItemQuality(ItemStack shovelStack, CallbackInfoReturnable<ItemStack> cir) {
        CompoundTag qualityTag = create_Delight_Core$copyStoredQualityTag(shovelStack);
        ItemStack containedStack = cir.getReturnValue();
        if (qualityTag == null || containedStack.isEmpty()) {
            return;
        }

        ItemStack result = containedStack.copy();
        result.getOrCreateTag().put(QualityUtils.QUALITY_TAG, qualityTag);
        cir.setReturnValue(result);
    }

    @Inject(method = "removeItem", at = @At("TAIL"), remap = false)
    private void create_Delight_Core$clearContainedItemQuality(ItemStack shovelStack, CallbackInfo ci) {
        CompoundTag tag = shovelStack.getTag();
        if (tag != null) {
            tag.remove(create_Delight_Core$STORED_QUALITY_TAG);
            if (tag.isEmpty()) {
                shovelStack.setTag(null);
            }
        }
    }

    @Unique
    private static CompoundTag create_Delight_Core$copyQualityTag(ItemStack stack) {
        CompoundTag tag = stack.getTagElement(QualityUtils.QUALITY_TAG);
        return tag != null ? tag.copy() : null;
    }

    @Unique
    private static void create_Delight_Core$applyQualityToStoredInventoryStack(ItemStack shovelStack, CompoundTag qualityTag) {
        CompoundTag shovelTag = shovelStack.getTag();
        if (shovelTag == null || !shovelTag.contains(create_Delight_Core$INVENTORY_TAG, 10)) {
            return;
        }

        ItemStackHandler inventory = new ItemStackHandler(1);
        inventory.deserializeNBT(shovelTag.getCompound(create_Delight_Core$INVENTORY_TAG));
        ItemStack containedStack = inventory.getStackInSlot(0);
        if (containedStack.isEmpty()) {
            return;
        }

        containedStack.getOrCreateTag().put(QualityUtils.QUALITY_TAG, qualityTag.copy());
        inventory.setStackInSlot(0, containedStack);
        shovelTag.put(create_Delight_Core$INVENTORY_TAG, inventory.serializeNBT());
        shovelStack.setTag(shovelTag);
    }

    @Unique
    private static CompoundTag create_Delight_Core$copyStoredQualityTag(ItemStack stack) {
        CompoundTag tag = stack.getTagElement(create_Delight_Core$STORED_QUALITY_TAG);
        return tag != null ? tag.copy() : null;
    }
}
