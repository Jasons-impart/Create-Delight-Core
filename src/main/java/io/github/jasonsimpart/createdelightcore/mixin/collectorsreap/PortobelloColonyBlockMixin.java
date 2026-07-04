package io.github.jasonsimpart.createdelightcore.mixin.collectorsreap;

import net.brdle.collectorsreap.common.block.PortobelloColonyBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vectorwing.farmersdelight.common.block.MushroomColonyBlock;

@Mixin(PortobelloColonyBlock.class)
public class PortobelloColonyBlockMixin {
    @Inject(method = "isValidBonemealTarget", at = @At("HEAD"), cancellable = true)
    private void create_Delight_Core$restoreBonemealTarget(LevelReader level, BlockPos pos, BlockState state, boolean isClient, CallbackInfoReturnable<Boolean> cir) {
        MushroomColonyBlock colony = (MushroomColonyBlock) (Object) this;
        cir.setReturnValue(state.getValue(colony.getAgeProperty()) < colony.getMaxAge());
    }

    @Inject(method = "isBonemealSuccess", at = @At("HEAD"), cancellable = true)
    private void create_Delight_Core$restoreBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(true);
    }
}
