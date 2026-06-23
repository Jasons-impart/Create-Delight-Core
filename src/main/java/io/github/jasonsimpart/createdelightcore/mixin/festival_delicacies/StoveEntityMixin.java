package io.github.jasonsimpart.createdelightcore.mixin.festival_delicacies;

import cn.foggyhillside.festival_delicacies.blocks.StoveBlock;
import cn.foggyhillside.festival_delicacies.blocks.entities.StoveEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = StoveEntity.class, remap = false)
public class StoveEntityMixin {
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private static void createdelightcore$resetRemovedCookingModes(Level level, BlockPos pos, BlockState state, StoveEntity entity, CallbackInfo ci) {
        int cookingType = state.getValue(StoveBlock.COOKING_TYPE);
        if (!level.isClientSide && cookingType > 4) {
            level.setBlock(pos, state.setValue(StoveBlock.COOKING_TYPE, 1), 2);
            ci.cancel();
        }
    }
}
