package io.github.jasonsimpart.createdelightcore.mixin.festival_delicacies;

import cn.foggyhillside.festival_delicacies.blocks.FruitingCropBlock;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import de.cadentem.quality_food.capability.LevelData;
import de.cadentem.quality_food.util.DropData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FruitingCropBlock.class)
public class FruitingCropBlockQualityMixin {
    @Inject(
            method = "use",
            at = @At(
                    value = "INVOKE",
                    target = "Lcn/foggyhillside/festival_delicacies/blocks/FruitingCropBlock;popResource(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/item/ItemStack;)V"
            )
    )
    private void create_Delight_Core$setHarvestQuality(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hit,
            CallbackInfoReturnable<InteractionResult> cir,
            @Share("create_Delight_Core$previousDropData") LocalRef<DropData> previousRef) {
        previousRef.set(DropData.CURRENT.get());
        DropData.CURRENT.set(new DropData(
                LevelData.get(level, pos, true),
                pos,
                state,
                player,
                level.getBlockState(pos.below())));
    }

    @Inject(
            method = "use",
            at = @At(
                    value = "INVOKE",
                    target = "Lcn/foggyhillside/festival_delicacies/blocks/FruitingCropBlock;popResource(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/item/ItemStack;)V",
                    shift = At.Shift.AFTER
            )
    )
    private void create_Delight_Core$restoreHarvestQuality(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hit,
            CallbackInfoReturnable<InteractionResult> cir,
            @Share("create_Delight_Core$previousDropData") LocalRef<DropData> previousRef) {
        DropData previous = previousRef.get();
        if (previous == null) {
            DropData.CURRENT.remove();
        } else {
            DropData.CURRENT.set(previous);
        }
    }
}
