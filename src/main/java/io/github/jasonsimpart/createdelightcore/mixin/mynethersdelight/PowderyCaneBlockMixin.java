package io.github.jasonsimpart.createdelightcore.mixin.mynethersdelight;

import com.soytutta.mynethersdelight.common.block.PowderyCaneBlock;
import com.soytutta.mynethersdelight.common.registry.MNDBlocks;
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

@Mixin(PowderyCaneBlock.class)
public class PowderyCaneBlockMixin {
    @Inject(method = "use", at = @At(value = "INVOKE", target = "Lcom/soytutta/mynethersdelight/common/block/PowderyCaneBlock;popResource(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/item/ItemStack;)V"))
    public void setDropData(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result, CallbackInfoReturnable<InteractionResult> cir) {
        BlockPos farmlandPos = pos.below();
        while (level.getBlockState(farmlandPos).is(MNDBlocks.POWDERY_CANE.get()))
            farmlandPos = farmlandPos.below();
        DropData.current.set(new DropData(LevelData.get(level, pos, true), state, player, level.getBlockState(farmlandPos)));
    }

    @Inject(method = "use", at = @At(value = "INVOKE", target = "Lcom/soytutta/mynethersdelight/common/block/PowderyCaneBlock;popResource(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/item/ItemStack;)V", shift = At.Shift.AFTER))
    public void clearCropData(BlockState state, Level level, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit, CallbackInfoReturnable<InteractionResult> cir) {
        DropData.current.remove();
    }
}
