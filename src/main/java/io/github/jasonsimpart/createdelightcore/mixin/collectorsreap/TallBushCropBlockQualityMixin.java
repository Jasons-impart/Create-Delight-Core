package io.github.jasonsimpart.createdelightcore.mixin.collectorsreap;

import de.cadentem.quality_food.capability.LevelData;
import net.brdle.collectorsreap.common.block.FruitBushBlock;
import net.brdle.collectorsreap.common.block.TallBushCropBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TallBushCropBlock.class)
public abstract class TallBushCropBlockQualityMixin {
    @Inject(method = "grow", at = @At("RETURN"), remap = false)
    private void create_Delight_Core$syncFruitBushQuality(
            ServerLevel level,
            BlockState state,
            BlockPos pos,
            int increase,
            CallbackInfo ci) {
        if ((Object) this instanceof FruitBushBlock) {
            create_Delight_Core$syncUpperQuality(level, state, pos);
        }
    }

    @Unique
    private void create_Delight_Core$syncUpperQuality(ServerLevel level, BlockState state, BlockPos pos) {
        BlockPos lowerPos = state.hasProperty(DoublePlantBlock.HALF)
                && state.getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.UPPER
                ? pos.below()
                : pos;
        BlockPos upperPos = lowerPos.above();
        Block block = (Block) (Object) this;
        BlockState lowerState = level.getBlockState(lowerPos);
        BlockState upperState = level.getBlockState(upperPos);
        if (lowerState.is(block)
                && upperState.is(block)
                && lowerState.getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.LOWER
                && upperState.getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.UPPER) {
            LevelData.set(level, upperPos, LevelData.get(level, lowerPos));
        }
    }
}
