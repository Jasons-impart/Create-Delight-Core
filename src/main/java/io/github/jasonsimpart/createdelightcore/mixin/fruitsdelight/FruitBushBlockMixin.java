package io.github.jasonsimpart.createdelightcore.mixin.fruitsdelight;

import de.cadentem.quality_food.capability.LevelData;
import de.cadentem.quality_food.util.DropData;
import dev.xkmc.fruitsdelight.content.block.FruitBushBlock;
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

@Mixin(FruitBushBlock.class)
public class FruitBushBlockMixin {
    @Inject(method = "use", at = @At(value = "INVOKE", target = "Ldev/xkmc/fruitsdelight/content/block/FruitBushBlock;popResource(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/item/ItemStack;)V"))
    public void applyQuality(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result, CallbackInfoReturnable<InteractionResult> cir) {
        DropData.CURRENT.set(new DropData(LevelData.get(level, pos, true), state,  player, level.getBlockState(pos.below())));
    }
    @Inject(method = "use", at = @At(value = "INVOKE", target = "Ldev/xkmc/fruitsdelight/content/block/FruitBushBlock;popResource(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/item/ItemStack;)V", shift = At.Shift.AFTER))
    public void clearCropData(BlockState state, Level level, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit, CallbackInfoReturnable<InteractionResult> cir) {
        DropData.CURRENT.remove();
    }
}
