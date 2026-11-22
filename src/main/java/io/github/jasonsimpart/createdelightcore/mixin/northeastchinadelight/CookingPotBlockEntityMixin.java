package io.github.jasonsimpart.createdelightcore.mixin.northeastchinadelight;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.jasonsimpart.createdelightcore.compat.northeastchinadelight.SeasoningBottleCompat;
import io.github.jasonsimpart.createdelightcore.content.item.SeasoningBottleItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import vectorwing.farmersdelight.common.block.entity.CookingPotBlockEntity;
import vectorwing.farmersdelight.common.crafting.CookingPotRecipe;

@Mixin(value = CookingPotBlockEntity.class, remap = false)
public abstract class CookingPotBlockEntityMixin {
    @Shadow
    protected abstract void ejectIngredientRemainder(ItemStack stack);

    @WrapOperation(method = "processCooking", at = @At(value = "INVOKE", remap = true,
            target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V"))
    private void createdelightcore$drainSeasoning(ItemStack stack, int count, Operation<Void> original,
                                                  @Local(argsOnly = true) CookingPotRecipe recipe) {
        if (!SeasoningBottleCompat.isBottle(stack)) {
            original.call(stack, count);
            return;
        }
        Level level = ((CookingPotBlockEntity) (Object) this).getLevel();
        if (level != null) {
            ItemStack ingredient = SeasoningBottleCompat.asIngredient(stack);
            ItemStack result = recipe.getResultItem(level.registryAccess());
            SeasoningBottleCompat.consume(stack, SeasoningBottleCompat.cookingPotAmount(ingredient, result));
            if (SeasoningBottleItem.contents(stack).isEmpty()) {
                ItemStack emptyBottle = stack.copy();
                stack.shrink(count);
                ejectIngredientRemainder(emptyBottle);
            }
        }
    }
}
