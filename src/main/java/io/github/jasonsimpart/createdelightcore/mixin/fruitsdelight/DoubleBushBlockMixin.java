package io.github.jasonsimpart.createdelightcore.mixin.fruitsdelight;

import com.llamalad7.mixinextras.sugar.Local;
import de.cadentem.quality_food.util.Utils;
import dev.xkmc.fruitsdelight.content.block.DoubleBushBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(DoubleBushBlock.class)
public class DoubleBushBlockMixin {
    @ModifyArg(method = "setGrowth", at = @At(value = "INVOKE", ordinal = 1, target = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"), index = 1)
    private BlockState quality_food$keepQualityWhenGrowingUpward(BlockState pNewState, @Local(argsOnly = true) Level level, @Local(argsOnly = true) BlockPos position ) {
        Utils.storeQuality(pNewState, level, position, Direction.UP);
        return pNewState;
    }
}
