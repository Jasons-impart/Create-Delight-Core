package io.github.jasonsimpart.createdelightcore.mixin.ratatouille;

import com.llamalad7.mixinextras.sugar.Local;
import de.cadentem.quality_food.core.Quality;
import de.cadentem.quality_food.util.QualityUtils;
import net.minecraft.world.item.ItemStack;
import org.forsteri.ratatouille.content.frozen_block.FrozenBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Ratatouille 冷冻方块在 {@code CollectingChillness} 满时用 {@code FreezingRecipe#getResultItem}
 * 换新堆叠，丢掉输入品质。鼓风机冷冻走 Create RecipeApplier（Quality Food 已覆盖）；
 * 本 mixin 只修方块实体 tick 路径。
 * {@code getResultItem} 返回共享配方模板，必须先 {@code copy()} 再 setCount / 写品质。
 * {@code @ModifyVariable(ordinal = 1)} 对准 LVT {@code newStack}（slot 17）；0 是输入 {@code itemStack}。
 * 类级 {@code remap=false}（mod 方法名）；{@code ItemEntity#setItem} 的 {@code @At} 用
 * {@code remap=true} 以写入生产映射 {@code m_32045_}。
 */
@Mixin(value = FrozenBlockEntity.class, remap = false)
public abstract class FrozenBlockEntityQualityMixin {

    @ModifyVariable(
            method = "tick",
            at = @At(
                    value = "INVOKE_ASSIGN",
                    target = "Lorg/forsteri/ratatouille/content/frozen_block/FreezingRecipe;getResultItem(Lnet/minecraft/core/RegistryAccess;)Lnet/minecraft/world/item/ItemStack;",
                    remap = true
            ),
            ordinal = 1 // newStack (output); 0 = itemStack (input)
    )
    private static ItemStack createdelightcore$copyFreezeResultTemplate(ItemStack result) {
        return result == null || result.isEmpty() ? result : result.copy();
    }

    @ModifyArg(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/item/ItemEntity;setItem(Lnet/minecraft/world/item/ItemStack;)V",
                    remap = true
            ),
            index = 0
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
