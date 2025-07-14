package io.github.jasonsimpart.createdelightcore.mixin.displaydelight;

import com.jkvin114.displaydelight.events.InterationManager;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import de.cadentem.quality_food.capability.LevelData;
import de.cadentem.quality_food.core.Quality;
import de.cadentem.quality_food.util.QualityUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(InterationManager.class)
public class InterationManagerMixin {

    @Inject(method = "tryTakeItemWithBareHand", at = @At("HEAD"), remap = false)
    private static void tryTakeItemWithBareHand$storeQuality(Player player, ServerLevel world, BlockHitResult rez, CallbackInfoReturnable<Boolean> cir, @Share("bareHandQuality") LocalRef<Quality> quality) {
        quality.set(LevelData.get(world, rez.getBlockPos()));
    }

    @ModifyArg(method = "tryTakeItemWithBareHand", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;add(Lnet/minecraft/world/item/ItemStack;)Z"))
    private static ItemStack tryTakeItemWithBareHand$applyQualityToInventoryItem(ItemStack pStack, @Local(argsOnly = true) ServerLevel world, @Local(argsOnly = true) BlockHitResult rez, @Share("bareHandQuality") LocalRef<Quality> quality) {
        QualityUtils.applyQuality(pStack, quality.get());
        return pStack;
    }

    @ModifyArg(method = "tryTakeItemWithBareHand", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;setItemInHand(Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/item/ItemStack;)V"), index = 1)
    private static ItemStack tryTakeItemWithBareHand$applyQualityToHandItem(ItemStack pStack, @Local(argsOnly = true) ServerLevel world, @Local(argsOnly = true) BlockHitResult rez, @Share("bareHandQuality") LocalRef<Quality> quality) {
        QualityUtils.applyQuality(pStack, quality.get());
        return pStack;
    }


    @Inject(method = "tryPlaceItemOnPlate", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z", shift = At.Shift.AFTER, ordinal = 1))
    private static void tryPlaceItemOnPlate$applyQuality(Player player, ServerLevel world, BlockHitResult rez, boolean isMainHand, CallbackInfoReturnable<Boolean> cir, @Local ItemStack handStack) {
        Quality quality = QualityUtils.getQuality(handStack);
        if (quality == Quality.NONE)
            LevelData.set(world, rez.getBlockPos(), Quality.NONE_PLAYER_PLACED);
        else
            LevelData.set(world, rez.getBlockPos(), quality);
    }

    @Inject(method = "tryPlaceItemOnPlate", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z", shift = At.Shift.AFTER))
    private static void tryPlaceItemOnPlate$checkQuality(Player player, ServerLevel world, BlockHitResult rez, boolean isMainHand, CallbackInfoReturnable<Boolean> cir, @Local ItemStack handStack) {
        Quality blockQuality = LevelData.get(world, rez.getBlockPos()), itemQuality = QualityUtils.getQuality(handStack);
        if (!(blockQuality == itemQuality || (blockQuality == Quality.NONE_PLAYER_PLACED && itemQuality == Quality.NONE)))
            cir.cancel();
    }


    @Inject(method = "tryPlaceItemOnSmallPlate", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z", shift = At.Shift.AFTER))
    private static void tryPlaceItemOnSmallPlate$applyQuality(Player player, ServerLevel world, BlockHitResult rez, boolean isMainHand, CallbackInfoReturnable<Boolean> cir, @Local ItemStack handStack) {
        Quality quality = QualityUtils.getQuality(handStack);
        if (quality == Quality.NONE)
            LevelData.set(world, rez.getBlockPos(), Quality.NONE_PLAYER_PLACED);
        else
            LevelData.set(world, rez.getBlockPos(), quality);
    }

    @Inject(method = "tryPlaceItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;swing(Lnet/minecraft/world/InteractionHand;Z)V"))
    private static void tryPlaceItem$applyQuality(Player player, ServerLevel world, BlockHitResult rez, boolean isMainHand, CallbackInfoReturnable<Boolean> cir, @Local ItemStack stack) {
        Quality quality = QualityUtils.getQuality(stack);
        if (quality == Quality.NONE)
            LevelData.set(world, rez.getBlockPos(), Quality.NONE_PLAYER_PLACED);
        else
            LevelData.set(world, rez.getBlockPos(), quality);
    }
}
