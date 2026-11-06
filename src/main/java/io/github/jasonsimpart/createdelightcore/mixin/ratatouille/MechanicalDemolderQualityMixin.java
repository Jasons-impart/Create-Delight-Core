package io.github.jasonsimpart.createdelightcore.mixin.ratatouille;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.foundation.recipe.RecipeApplier;
import de.cadentem.quality_food.core.Quality;
import de.cadentem.quality_food.util.QualityUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import org.forsteri.ratatouille.content.demolder.MechanicalDemolderBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ArrayList;
import java.util.List;

/**
 * 机械脱模器通过 {@link RecipeApplier#applyRecipeOn} 出货。Quality Food 的
 * {@code RecipeApplierMixin} 会：
 * 1. 直接改配方共享结果栈（上一份金星会串到下一份）；
 * 2. 按原料权重再随机掷一次品质（铁星也可能掷出金星）。
 * {@link QualityUtils#applyQuality(ItemStack, Quality)} 默认不覆盖、不降档已有品质，
 * 所以必须先去掉结果上的 {@code quality_food} 标签，再写成当前输入的品质。
 */
@Mixin(value = MechanicalDemolderBlockEntity.class, remap = false)
public abstract class MechanicalDemolderQualityMixin {

    @WrapOperation(
            method = "tryProcessOnBelt",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/foundation/recipe/RecipeApplier;applyRecipeOn(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/crafting/Recipe;Z)Ljava/util/List;"
            )
    )
    private List<ItemStack> createdelightcore$copyAndInheritDemoldQuality(
            Level level,
            ItemStack input,
            Recipe<?> recipe,
            boolean returnProcessingRemainder,
            Operation<List<ItemStack>> original
    ) {
        List<ItemStack> outputs = original.call(level, input, recipe, returnProcessingRemainder);
        if (outputs == null || outputs.isEmpty()) {
            return outputs;
        }
        Quality quality = input != null && !input.isEmpty() ? QualityUtils.getQuality(input) : Quality.NONE;
        if (!QualityUtils.isValidQuality(quality) || quality.level() <= 0) {
            quality = Quality.NONE;
        }
        List<ItemStack> copied = new ArrayList<>(outputs.size());
        for (ItemStack output : outputs) {
            if (output == null || output.isEmpty()) {
                copied.add(output);
                continue;
            }
            ItemStack stack = output.copy();
            CompoundTag tag = stack.getTag();
            if (tag != null) {
                tag.remove(QualityUtils.QUALITY_TAG);
                if (tag.isEmpty()) {
                    stack.setTag(null);
                }
            }
            QualityUtils.applyQuality(stack, quality);
            copied.add(stack);
        }
        return copied;
    }
}
