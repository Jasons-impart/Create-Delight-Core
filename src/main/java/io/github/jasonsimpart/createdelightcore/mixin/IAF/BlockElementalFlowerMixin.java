package io.github.jasonsimpart.createdelightcore.mixin.IAF;

import com.github.alexthe666.iceandfire.block.BlockElementalFlower;
import com.github.alexthe666.iceandfire.block.IafBlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Ice and Fire's frost lily canStay already accepts #ice, #snow and
 * #snow_layer_can_survive_on, but worldgen simple_block and player placement
 * go through BushBlock.canSurvive -> mayPlaceOn, which only allows dirt-like
 * blocks and #sand. This mixin widens mayPlaceOn for frost lilies only, so
 * they can generate and be planted on ice and snow without changing fire or
 * lightning lilies.
 */
@Mixin(BlockElementalFlower.class)
public abstract class BlockElementalFlowerMixin {
    @Inject(method = "mayPlaceOn", at = @At("RETURN"), cancellable = true)
    private void createdelightcore$allowFrostLilyOnIceAndSnow(BlockState state, BlockGetter level, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ()) {
            return;
        }
        if ((Object) this == IafBlockRegistry.FROST_LILY.get()) {
            cir.setReturnValue(state.is(BlockTags.ICE) || state.is(BlockTags.SNOW) || state.is(BlockTags.SNOW_LAYER_CAN_SURVIVE_ON));
        }
    }
}
