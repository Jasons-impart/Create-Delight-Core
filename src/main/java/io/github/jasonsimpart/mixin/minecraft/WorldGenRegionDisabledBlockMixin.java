package io.github.jasonsimpart.mixin.minecraft;

import io.github.jasonsimpart.disabled.DisabledContentManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(WorldGenRegion.class)
public abstract class WorldGenRegionDisabledBlockMixin {
    @ModifyVariable(method = "setBlock", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private BlockState createdelightcore$replaceDisabledBlock(BlockState state, BlockPos pos) {
        return DisabledContentManager.replacementFor(state, (WorldGenRegion) (Object) this, pos);
    }
}
