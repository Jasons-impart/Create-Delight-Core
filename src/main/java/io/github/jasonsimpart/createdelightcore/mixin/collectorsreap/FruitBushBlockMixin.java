package io.github.jasonsimpart.createdelightcore.mixin.collectorsreap;

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

/**
 * 修复残留的果丛上半块（HALF=upper）在下方方块已被替换为非果丛方块时被右键，
 * FruitBushBlock.use() 对无 AGE 属性的方块调用 getValue 抛出 IllegalArgumentException 导致的崩溃。
 * 上游 Issue 跟踪：Create-Delight-Remake#2353
 */
@Mixin(FruitBushBlock.class)
public class FruitBushBlockMixin {
    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void create_Delight_Core$guardOrphanedUpperHalf(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit, CallbackInfoReturnable<InteractionResult> cir) {
        if (state.getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.UPPER
                && !(level.getBlockState(pos.below()).getBlock() instanceof FruitBushBlock)) {
            cir.setReturnValue(InteractionResult.PASS);
        }
    }
}
