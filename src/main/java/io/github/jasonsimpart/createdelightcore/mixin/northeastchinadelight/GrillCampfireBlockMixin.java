package io.github.jasonsimpart.createdelightcore.mixin.northeastchinadelight;

import com.gunmu.northeast_china_delight.block.GrillCampfireBlock;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.jasonsimpart.createdelightcore.compat.northeastchinadelight.SeasoningBottleCompat;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = GrillCampfireBlock.class, remap = false)
public abstract class GrillCampfireBlockMixin {
    @WrapOperation(method = "useOnGrill", at = @At(value = "INVOKE", ordinal = 1, remap = true, target =
            "Lnet/minecraft/world/entity/player/Player;getItemInHand(Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack createdelightcore$matchSeasoning(Player player, InteractionHand hand, Operation<ItemStack> original) {
        ItemStack stack = original.call(player, hand);
        if (SeasoningBottleCompat.isBottle(stack)
                && !SeasoningBottleCompat.hasAmount(stack, SeasoningBottleCompat.GRILL_DOSE)) {
            return ItemStack.EMPTY;
        }
        return SeasoningBottleCompat.asIngredient(stack);
    }

    @WrapOperation(method = "useOnGrill", at = @At(value = "INVOKE", remap = true, target =
            "Lnet/minecraft/world/entity/player/Player;setItemInHand(Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/item/ItemStack;)V"))
    private void createdelightcore$drainSeasoning(Player player, InteractionHand hand, ItemStack remainder,
                                                   Operation<Void> original) {
        ItemStack held = player.getItemInHand(hand);
        if (SeasoningBottleCompat.isBottle(held)) {
            SeasoningBottleCompat.consume(held, SeasoningBottleCompat.GRILL_DOSE);
        } else {
            original.call(player, hand, remainder);
        }
    }
}
