package io.github.jasonsimpart.createdelightcore.mixin.quality_food;

import de.cadentem.quality_food.capability.LevelData;
import de.cadentem.quality_food.util.DropData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 为甜浆果丛的右键收获注入携带玩家信息的 DropData。
 *
 * Quality Food 的 BlockMixin 对 Block.popResource 的兜底 DropData 里 player 恒为 null，
 * 使 applyHarvestQuality 无法区分收获者；为了让 applyHarvestQuality 的
 * "非玩家（null/FakePlayer）不产品质"拦截不误伤玩家右键收获的 LevelData 品质，
 * 这里在 popResource 前覆写为真实玩家上下文。
 *
 * 注入在 Invoke(popResource) 前后：popResource 实参评估（含 QF 的 ModifyArg 品质处理）
 * 发生在这条指令调用之前，head 注入能保证两层品质判定都读到玩家上下文。
 */
@Mixin(SweetBerryBushBlock.class)
public class SweetBerryBushDropDataMixin {
    @Inject(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/SweetBerryBushBlock;popResource(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/item/ItemStack;)V"))
    private void create_Delight_Core$setCropData(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit, CallbackInfoReturnable<InteractionResult> cir) {
        DropData.CURRENT.set(new DropData(LevelData.get(level, pos, true), pos, state, player, level.getBlockState(pos.below())));
    }

    @Inject(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/SweetBerryBushBlock;popResource(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/item/ItemStack;)V", shift = At.Shift.AFTER))
    private void create_Delight_Core$clearCropData(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit, CallbackInfoReturnable<InteractionResult> cir) {
        DropData.CURRENT.remove();
    }
}
