

package io.github.jasonsimpart.createdelightcore.mixin.create;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalDoubleRef;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.simibubi.create.content.processing.basin.BasinRecipe;
import de.cadentem.quality_food.config.QualityConfig;
import de.cadentem.quality_food.core.Quality;
import de.cadentem.quality_food.util.QualityUtils;
import java.util.List;

import de.cadentem.quality_food.util.Utils;
import io.github.jasonsimpart.createdelightcore.content.util.QualityFoodUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.At.Shift;

@Mixin(
        value = {BasinRecipe.class},
        remap = false,
        priority = 1100 //覆盖quality food自己写的mixin
)
public abstract class BasinRecipeMixin {

    public BasinRecipeMixin() {
    }

    @ModifyVariable(
            method = {"apply(Lcom/simibubi/create/content/processing/basin/BasinBlockEntity;Lnet/minecraft/world/item/crafting/Recipe;Z)Z"},
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraftforge/items/IItemHandler;extractItem(IIZ)Lnet/minecraft/world/item/ItemStack;",
                    ordinal = 0,
                    shift = Shift.BY,
                    by = 2
            )
    )
    private static ItemStack quality_food$storeInput(ItemStack stack, @Local Ingredient ingredient, @Share("count") LocalIntRef count, @Share("weight") LocalDoubleRef weight) {
        if (Utils.isValidItem(stack) && ingredient.test(stack)) {
            count.set(count.get() + 1);
            weight.set(weight.get() + QualityConfig.getWeight(QualityUtils.getQuality(stack)));
        }
        return stack;
    }

    @ModifyArg(
            method = {"apply(Lcom/simibubi/create/content/processing/basin/BasinBlockEntity;Lnet/minecraft/world/item/crafting/Recipe;Z)Z"},
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/content/processing/basin/BasinBlockEntity;acceptOutputs(Ljava/util/List;Ljava/util/List;Z)Z"
            ),
            index = 0
    )
    private static List<ItemStack> quality_food$applyQuality(List<ItemStack> stacks, @Share("count") LocalIntRef count, @Share("weight") LocalDoubleRef weight) {
        stacks.forEach((stack) -> QualityFoodUtil.applyQuality(stack, count.get(), weight.get(), null));
        return stacks;
    }
}
