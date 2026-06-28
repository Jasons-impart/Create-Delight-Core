package io.github.jasonsimpart.mixin.minecraft;

import io.github.jasonsimpart.disabled.DisabledContentManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.ProtoChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin({ProtoChunk.class, LevelChunk.class, ImposterProtoChunk.class})
public abstract class ChunkAccessDisabledBlockMixin {
    @ModifyVariable(
            method = "setBlockState(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Z)Lnet/minecraft/world/level/block/state/BlockState;",
            at = @At("HEAD"),
            argsOnly = true,
            ordinal = 0
    )
    private BlockState createdelightcore$replaceDisabledBlock(BlockState state, BlockPos pos) {
        return DisabledContentManager.replacementFor(state, (ChunkAccess) (Object) this, pos);
    }
}
