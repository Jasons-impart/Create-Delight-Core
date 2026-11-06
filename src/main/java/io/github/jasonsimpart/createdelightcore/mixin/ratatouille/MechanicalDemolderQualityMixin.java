package io.github.jasonsimpart.createdelightcore.mixin.ratatouille;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.foundation.recipe.RecipeApplier;
import de.cadentem.quality_food.core.Quality;
import de.cadentem.quality_food.util.QualityUtils;
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
 * {@code RecipeApplierMixin} 会直接改配方结果栈，上一份金星会留在共享模板上，
 * 下一份铁星/无星就会串品质（蛋糕模具：烤箱后仍是铁星，脱模却变成金星）。
 * 先 {@code copy()}，再按当前输入覆盖品质；无星输入则写成 {@link Quality#NONE}。
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
            QualityUtils.applyQuality(stack, quality);
            copied.add(stack);
        }
        return copied;
    }
}