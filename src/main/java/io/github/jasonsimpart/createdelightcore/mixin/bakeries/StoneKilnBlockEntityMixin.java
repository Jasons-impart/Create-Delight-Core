package io.github.jasonsimpart.createdelightcore.mixin.bakeries;

import com.renyigesai.bakeries.block.stone_kiln.StoneKilnBlockEntity;
import de.cadentem.quality_food.util.QualityUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(StoneKilnBlockEntity.class)
public class StoneKilnBlockEntityMixin {
    @ModifyArg(
            method = "stoneKilnCookingTick(Lcom/renyigesai/bakeries/recipe/StoneKilnRecipe;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraftforge/items/ItemStackHandler;setStackInSlot(ILnet/minecraft/world/item/ItemStack;)V"
            ),
            index = 1,
            remap = false
    )
    private ItemStack create_Delight_Core$applyQualityToStoneKilnResult(ItemStack result) {
        return create_Delight_Core$applyInputQuality(result);
    }

    @ModifyArg(
            method = "smokingCookingTick(Lnet/minecraft/world/item/crafting/SmokingRecipe;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraftforge/items/ItemStackHandler;setStackInSlot(ILnet/minecraft/world/item/ItemStack;)V"
            ),
            index = 1,
            remap = false
    )
    private ItemStack create_Delight_Core$applyQualityToSmokingResult(ItemStack result) {
        return create_Delight_Core$applyInputQuality(result);
    }

    @Unique
    private ItemStack create_Delight_Core$applyInputQuality(ItemStack result) {
        CompoundTag qualityTag = create_Delight_Core$copyQualityTag(((StoneKilnBlockEntity) (Object) this).getItem(0));
        if (qualityTag != null) {
            result.getOrCreateTag().put(QualityUtils.QUALITY_TAG, qualityTag);
        }
        return result;
    }

    @Unique
    private static CompoundTag create_Delight_Core$copyQualityTag(ItemStack stack) {
        CompoundTag tag = stack.getTagElement(QualityUtils.QUALITY_TAG);
        return tag != null ? tag.copy() : null;
    }
}
