package io.github.jasonsimpart.createdelightcore.mixin.northstar;

import com.lightning.northstar.world.gen.feature.CraterFeature;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = CraterFeature.class, remap = false)
public abstract class CraterFeatureMixin {
    @Redirect(
            method = "placeColumnBelow",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/WorldGenLevel;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z",
                    remap = true
            ),
            require = 1
    )
    private boolean createdelightcore$placeSupportedCraterFloor(WorldGenLevel level, BlockPos pos, BlockState state, int flags) {
        if (state.getBlock() instanceof FallingBlock
                && FallingBlock.isFree(level.getBlockState(pos.below()))) {
            return false;
        }

        return level.setBlock(pos, state, flags);
    }
}
