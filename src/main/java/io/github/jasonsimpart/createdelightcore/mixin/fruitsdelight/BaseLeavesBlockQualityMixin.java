package io.github.jasonsimpart.createdelightcore.mixin.fruitsdelight;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import de.cadentem.quality_food.capability.LevelData;
import de.cadentem.quality_food.util.DropData;
import dev.xkmc.fruitsdelight.content.block.BaseLeavesBlock;
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

@Mixin(BaseLeavesBlock.class)
public abstract class BaseLeavesBlockQualityMixin {
    @Inject(
            method = "use",
            at = @At(
                    value = "INVOKE",
                    target = "Ldev/xkmc/fruitsdelight/content/block/BaseLeavesBlock;doClick(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/world/InteractionResult;",
                    remap = false
            )
    )
    private void create_Delight_Core$setDropDataOnManualHarvest(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit, CallbackInfoReturnable<InteractionResult> cir, @Share("create_Delight_Core$leavesDropDataSetByUse") LocalRef<Boolean> createdRef) {
        if (DropData.CURRENT.get() != null) {
            return;
        }
        DropData.CURRENT.set(new DropData(LevelData.get(level, pos, true), pos, state, player, level.getBlockState(pos.below())));
        createdRef.set(Boolean.TRUE);
    }

    @Inject(
            method = "use",
            at = @At(
                    value = "INVOKE",
                    target = "Ldev/xkmc/fruitsdelight/content/block/BaseLeavesBlock;doClick(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/world/InteractionResult;",
                    remap = false,
                    shift = At.Shift.AFTER
            )
    )
    private void create_Delight_Core$clearDropDataOnManualHarvest(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit, CallbackInfoReturnable<InteractionResult> cir, @Share("create_Delight_Core$leavesDropDataSetByUse") LocalRef<Boolean> createdRef) {
        if (Boolean.TRUE.equals(createdRef.get())) {
            DropData.CURRENT.remove();
        }
    }
}
