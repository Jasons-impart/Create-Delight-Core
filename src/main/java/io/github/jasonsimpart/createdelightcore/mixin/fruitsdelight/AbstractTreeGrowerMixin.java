package io.github.jasonsimpart.createdelightcore.mixin.fruitsdelight;

import io.github.jasonsimpart.createdelightcore.content.util.FruitsDelightTreeQualityContext;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.grower.AbstractTreeGrower;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractTreeGrower.class)
public abstract class AbstractTreeGrowerMixin {

    @Inject(method = "growTree", at = @At("HEAD"))
    private void create_Delight_Core$captureGrowthContext(ServerLevel level, ChunkGenerator generator, BlockPos pos, BlockState state, RandomSource random, CallbackInfoReturnable<Boolean> cir) {
        FruitsDelightTreeQualityContext.begin(level, pos, state);
    }

    @Inject(method = "growTree", at = @At("RETURN"))
    private void create_Delight_Core$applyTreeQuality(ServerLevel level, ChunkGenerator generator, BlockPos pos, BlockState state, RandomSource random, CallbackInfoReturnable<Boolean> cir) {
        FruitsDelightTreeQualityContext.finish(level, cir.getReturnValue());
    }
}
