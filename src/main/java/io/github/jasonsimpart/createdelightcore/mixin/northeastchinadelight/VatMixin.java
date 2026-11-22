package io.github.jasonsimpart.createdelightcore.mixin.northeastchinadelight;

import com.gunmu.northeast_china_delight.block.Vat;
import com.gunmu.northeast_china_delight.block.VatBlockEntity;
import com.gunmu.northeast_china_delight.crafting.VatBrewing;
import com.gunmu.northeast_china_delight.crafting.VatRecipe;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.jasonsimpart.createdelightcore.compat.northeastchinadelight.SeasoningBottleCompat;
import io.github.jasonsimpart.createdelightcore.content.item.SeasoningBottleItem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = Vat.class, remap = false)
public abstract class VatMixin {
    @WrapOperation(method = "vatUseItem", at = @At(value = "INVOKE", target =
            "Lcom/gunmu/northeast_china_delight/crafting/VatBrewing;forInsert(Lcom/gunmu/northeast_china_delight/block/VatBlockEntity;Lnet/minecraft/world/item/ItemStack;)Lcom/gunmu/northeast_china_delight/crafting/VatRecipe;"))
    private VatRecipe createdelightcore$matchSeasoning(VatBlockEntity vat, ItemStack stack, Operation<VatRecipe> original) {
        return original.call(vat, SeasoningBottleCompat.asIngredient(stack));
    }

    @WrapOperation(method = "vatUseItem", at = @At(value = "INVOKE", target =
            "Lcom/gunmu/northeast_china_delight/block/Vat;insert(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lcom/gunmu/northeast_china_delight/block/VatBlockEntity;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;Lcom/gunmu/northeast_china_delight/crafting/VatRecipe;Lnet/minecraft/world/InteractionHand;)Z"))
    private boolean createdelightcore$insertDose(Vat block, Level level, BlockPos pos, VatBlockEntity vat,
                                                 Player player, ItemStack stack, VatRecipe recipe, InteractionHand hand,
                                                 Operation<Boolean> original) {
        if (!SeasoningBottleCompat.isBottle(stack)) {
            return original.call(block, level, pos, vat, player, stack, recipe, hand);
        }
        ItemStack ingredient = SeasoningBottleCompat.asIngredient(stack);
        int slot = recipe.findSlotFor(ingredient, VatBrewing.counts(vat, null));
        if (slot < 0) {
            return false;
        }
        int amount = recipe.slots().get(slot).perDose() ? SeasoningBottleCompat.COOKING_DOSE : SeasoningBottleItem.CAPACITY;
        if (!SeasoningBottleCompat.hasAmount(stack, amount)) {
            return false;
        }
        ItemStack dose = SeasoningBottleCompat.asIngredient(stack, amount);
        if (!original.call(block, level, pos, vat, player, dose, recipe, hand)) {
            return false;
        }
        if (!player.getAbilities().instabuild) {
            SeasoningBottleCompat.consume(stack, amount);
        }
        return true;
    }

    @WrapOperation(method = "insert", at = @At(value = "INVOKE", remap = true, target =
            "Lnet/minecraft/world/entity/player/Player;setItemInHand(Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/item/ItemStack;)V"))
    private void createdelightcore$keepBottle(Player player, InteractionHand hand, ItemStack remainder,
                                               Operation<Void> original) {
        if (!SeasoningBottleCompat.isBottle(player.getItemInHand(hand))) {
            original.call(player, hand, remainder);
        }
    }
}
