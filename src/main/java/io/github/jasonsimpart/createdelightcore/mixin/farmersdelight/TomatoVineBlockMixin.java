package io.github.jasonsimpart.createdelightcore.mixin.farmersdelight;

import de.cadentem.quality_food.capability.LevelData;
import de.cadentem.quality_food.util.DropData;
import dev.xkmc.fruitsdelight.init.registrate.FDBlocks;
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
import vectorwing.farmersdelight.common.block.TomatoVineBlock;
import vectorwing.farmersdelight.common.registry.ModBlocks;

@Mixin(value = TomatoVineBlock.class, remap = false)
public class TomatoVineBlockMixin {
    @Inject(method = "use", at = @At(value = "INVOKE", target = "Lvectorwing/farmersdelight/common/block/TomatoVineBlock;popResource(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/item/ItemStack;)V"))
    public void setDropData(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit, CallbackInfoReturnable<InteractionResult> cir) {
        BlockPos blockPos = pos.below();
        while (level.getBlockState(blockPos).is(ModBlocks.TOMATO_CROP.get())) blockPos = blockPos.below();
        DropData.CURRENT.set(new DropData(LevelData.get(level, pos, true), pos, state, player, level.getBlockState(blockPos)));
    }
    @Inject(method = "use", at = @At(value = "INVOKE", target = "Lvectorwing/farmersdelight/common/block/TomatoVineBlock;popResource(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/item/ItemStack;)V", shift = At.Shift.AFTER))
    public void clearCropData(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit, CallbackInfoReturnable<InteractionResult> cir) {
        DropData.CURRENT.remove();
    }
}
