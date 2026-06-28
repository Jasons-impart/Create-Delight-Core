package io.github.jasonsimpart.mixin.minecraft;

import io.github.jasonsimpart.disabled.DisabledContentManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LevelChunk.class)
public abstract class LevelChunkDisabledBlockMixin {
    @Shadow
    @Final
    Level level;

    @ModifyVariable(
            method = "setBlockState(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Z)Lnet/minecraft/world/level/block/state/BlockState;",
            at = @At("HEAD"),
            argsOnly = true,
            ordinal = 0
    )
    private BlockState createdelightcore$replaceDisabledBlock(BlockState state, BlockPos pos) {
        if (this.level.isClientSide) {
            return state;
        }

        return DisabledContentManager.replacementFor(state, (LevelChunk) (Object) this, pos);
    }
}
