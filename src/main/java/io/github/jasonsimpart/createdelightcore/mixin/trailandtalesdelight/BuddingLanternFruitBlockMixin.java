package io.github.jasonsimpart.createdelightcore.mixin.trailandtalesdelight;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import de.cadentem.quality_food.capability.LevelData;
import de.cadentem.quality_food.core.Quality;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import show.tatd.mod.block.BuddingLanternFruitBlock;

@Mixin(BuddingLanternFruitBlock.class)
public class BuddingLanternFruitBlockMixin {
    @Inject(method = "updateShape", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/LevelAccessor;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"))
    private void create_Delight_Core$captureQualityOnUpdateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos, CallbackInfoReturnable<BlockState> cir, @Share("create_Delight_Core$lanternFruitQualityOnUpdateShape") LocalRef<Quality> qualityRef) {
        qualityRef.set(LevelData.get(level, pos));
    }

    @Inject(method = "updateShape", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/LevelAccessor;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z", shift = At.Shift.AFTER))
    private void create_Delight_Core$restoreQualityOnUpdateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos, CallbackInfoReturnable<BlockState> cir, @Share("create_Delight_Core$lanternFruitQualityOnUpdateShape") LocalRef<Quality> qualityRef) {
        create_Delight_Core$restoreQuality(level, pos, qualityRef.get());
    }

    @Inject(method = "growPastMaxAge", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;setBlockAndUpdate(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Z"))
    private void create_Delight_Core$captureQualityOnGrowPastMaxAge(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci, @Share("create_Delight_Core$lanternFruitQualityOnGrowPastMaxAge") LocalRef<Quality> qualityRef) {
        qualityRef.set(LevelData.get(level, pos));
    }

    @Inject(method = "growPastMaxAge", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;setBlockAndUpdate(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Z", shift = At.Shift.AFTER))
    private void create_Delight_Core$restoreQualityOnGrowPastMaxAge(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci, @Share("create_Delight_Core$lanternFruitQualityOnGrowPastMaxAge") LocalRef<Quality> qualityRef) {
        create_Delight_Core$restoreQuality(level, pos, qualityRef.get());
    }

    @Inject(method = "performBonemeal", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;setBlockAndUpdate(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Z", ordinal = 1))
    private void create_Delight_Core$captureQualityOnBonemealTransition(ServerLevel level, RandomSource random, BlockPos pos, BlockState state, CallbackInfo ci, @Share("create_Delight_Core$lanternFruitQualityOnBonemealTransition") LocalRef<Quality> qualityRef) {
        qualityRef.set(LevelData.get(level, pos));
    }

    @Inject(method = "performBonemeal", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;setBlockAndUpdate(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Z", ordinal = 1, shift = At.Shift.AFTER))
    private void create_Delight_Core$restoreQualityOnBonemealTransition(ServerLevel level, RandomSource random, BlockPos pos, BlockState state, CallbackInfo ci, @Share("create_Delight_Core$lanternFruitQualityOnBonemealTransition") LocalRef<Quality> qualityRef) {
        create_Delight_Core$restoreQuality(level, pos, qualityRef.get());
    }

    @Unique
    private static void create_Delight_Core$restoreQuality(LevelAccessor level, BlockPos pos, Quality quality) {
        if (quality != null) {
            LevelData.set(level, pos, quality);
        }
    }
}
