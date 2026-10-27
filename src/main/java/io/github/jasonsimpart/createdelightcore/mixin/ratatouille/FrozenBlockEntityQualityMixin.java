package io.github.jasonsimpart.createdelightcore.mixin.ratatouille;

import com.llamalad7.mixinextras.sugar.Local;
import de.cadentem.quality_food.core.Quality;
import de.cadentem.quality_food.util.QualityUtils;
import net.minecraft.world.item.ItemStack;
import org.forsteri.ratatouille.content.frozen_block.FrozenBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Ratatouille 冷冻方块在 {@code CollectingChillness} 满时用 {@code FreezingRecipe#getResultItem}
 * 换新堆叠，丢掉输入品质。鼓风机冷冻走 Create RecipeApplier（Quality Food 已覆盖）；
 * 本 mixin 只修方块实体 tick 路径。
 */
@Mixin(FrozenBlockEntity.class)
public abstract class FrozenBlockEntityQualityMixin {

    @ModifyArg(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/item/ItemEntity;setItem(Lnet/minecraft/world/item/ItemStack;)V"
            ),
            index = 0,
            remap = false
    )
    private static ItemStack createdelightcore$copyQualityOnFreezeComplete(
            ItemStack outputStack,
            @Local(ordinal = 0) ItemStack inputStack
    ) {
        if (inputStack == null || outputStack == null || inputStack.isEmpty() || outputStack.isEmpty()) {
            return outputStack;
        }
        Quality quality = QualityUtils.getQuality(inputStack);
        if (QualityUtils.isValidQuality(quality) && quality.level() > 0) {
            QualityUtils.applyQuality(outputStack, quality);
        }
        return outputStack;
    }
}
