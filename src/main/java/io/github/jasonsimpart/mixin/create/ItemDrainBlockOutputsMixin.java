package io.github.jasonsimpart.mixin.create;

import com.simibubi.create.content.fluids.drain.ItemDrainBlock;
import io.github.jasonsimpart.compat.create.DrainOutputs;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ItemDrainBlock.class, remap = false)
public abstract class ItemDrainBlockOutputsMixin {
    @Inject(method = "onRemove", at = @At("HEAD"))
    private void createdelightcore$dropOutputs(BlockState state, Level level, BlockPos pos,
                                               BlockState replacement, boolean moving, CallbackInfo ci) {
        if (state.hasBlockEntity() && state.getBlock() != replacement.getBlock()
                && level.getBlockEntity(pos) instanceof DrainOutputs outputs) {
            outputs.createdelightcore$dropPendingOutputs();
        }
    }
}
