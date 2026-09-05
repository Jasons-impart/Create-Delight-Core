package io.github.jasonsimpart.createdelightcore.mixin.fruitsdelight;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import de.cadentem.quality_food.capability.LevelData;
import de.cadentem.quality_food.util.DropData;
import dev.xkmc.fruitsdelight.content.block.PassableLeavesBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PassableLeavesBlock.class)
public abstract class PassableLeavesBlockQualityMixin {
    @Inject(method = "dropFruit", at = @At("HEAD"), remap = false)
    private void create_Delight_Core$setDropDataOnFruitDrop(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci, @Share("create_Delight_Core$dropDataSetByPassableLeavesDrop") LocalRef<Boolean> createdRef) {
        if (DropData.CURRENT.get() != null) {
            return;
        }
        DropData.CURRENT.set(new DropData(LevelData.get(level, pos, true), pos, state, null, level.getBlockState(pos.below())));
        createdRef.set(Boolean.TRUE);
    }

    @Inject(method = "dropFruit", at = @At("TAIL"), remap = false)
    private void create_Delight_Core$clearDropDataOnFruitDrop(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci, @Share("create_Delight_Core$dropDataSetByPassableLeavesDrop") LocalRef<Boolean> createdRef) {
        if (Boolean.TRUE.equals(createdRef.get())) {
            DropData.CURRENT.remove();
        }
    }
}
