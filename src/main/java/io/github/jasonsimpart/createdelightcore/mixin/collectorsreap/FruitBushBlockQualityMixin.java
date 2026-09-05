package io.github.jasonsimpart.createdelightcore.mixin.collectorsreap;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import de.cadentem.quality_food.capability.LevelData;
import de.cadentem.quality_food.util.DropData;
import net.brdle.collectorsreap.common.block.FruitBushBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FruitBushBlock.class)
public class FruitBushBlockQualityMixin {
    @Inject(
            method = "use",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/brdle/collectorsreap/common/block/FruitBushBlock;dropFruit(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)V",
                    remap = false
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
        BlockPos lowerPos = state.getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.UPPER
                ? pos.below()
                : pos;
        BlockState lowerState = level.getBlockState(lowerPos);
        previousRef.set(DropData.CURRENT.get());
        DropData.CURRENT.set(new DropData(
                LevelData.get(level, lowerPos, true),
                lowerPos,
                lowerState,
                player,
                level.getBlockState(lowerPos.below())));
    }

    @Inject(
            method = "use",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/brdle/collectorsreap/common/block/FruitBushBlock;dropFruit(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)V",
                    shift = At.Shift.AFTER,
                    remap = false
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
