package io.github.jasonsimpart.createdelightcore.mixin.northeastchinadelight;

import com.gunmu.northeast_china_delight.block.BigPotBlockEntity;
import com.gunmu.northeast_china_delight.block.BigPotCooking;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.jasonsimpart.createdelightcore.compat.northeastchinadelight.SeasoningBottleCompat;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = BigPotCooking.class, remap = false)
public abstract class BigPotCookingMixin {
    @ModifyVariable(method = "usable", at = @At("HEAD"), argsOnly = true)
    private static ItemStack createdelightcore$checkDose(ItemStack stack) {
        if (SeasoningBottleCompat.isBottle(stack)
                && !SeasoningBottleCompat.hasAmount(stack, SeasoningBottleCompat.COOKING_DOSE)) {
            return ItemStack.EMPTY;
        }
        return SeasoningBottleCompat.asIngredient(stack);
    }

    @ModifyVariable(method = "isSeasoning", at = @At("HEAD"), argsOnly = true)
    private static ItemStack createdelightcore$matchSeasoning(ItemStack stack) {
        return SeasoningBottleCompat.asIngredient(stack);
    }

    @WrapOperation(method = {"onUse", "dropIn"}, at = @At(value = "INVOKE", target =
            "Lcom/gunmu/northeast_china_delight/block/BigPotCooking;addOne(Lcom/gunmu/northeast_china_delight/block/BigPotBlockEntity;Lnet/minecraft/world/item/ItemStack;ZLnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V"))
    private static void createdelightcore$storeDose(BigPotBlockEntity pot, ItemStack stack, boolean seasoning,
                                                     Level level, BlockPos pos, BlockState state, Operation<Void> original) {
        original.call(pot, SeasoningBottleCompat.asIngredient(stack, SeasoningBottleCompat.COOKING_DOSE),
                seasoning, level, pos, state);
    }

    @WrapOperation(method = "onUse", at = @At(value = "INVOKE", target =
            "Lcom/gunmu/northeast_china_delight/block/BigPotCooking;remainderOf(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/item/ItemStack;"))
    private static ItemStack createdelightcore$keepCreativeBottle(ItemStack stack, Operation<ItemStack> original,
                                                                 @Local(argsOnly = true) Player player) {
        return SeasoningBottleCompat.isBottle(stack) && player.getAbilities().instabuild
                ? ItemStack.EMPTY : original.call(stack);
    }

    @Inject(method = "remainderOf", at = @At("HEAD"), cancellable = true)
    private static void createdelightcore$returnBottle(ItemStack stack, CallbackInfoReturnable<ItemStack> cir) {
        if (SeasoningBottleCompat.isBottle(stack)) {
            cir.setReturnValue(SeasoningBottleCompat.remainder(stack, SeasoningBottleCompat.COOKING_DOSE));
        }
    }
}
