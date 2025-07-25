package io.github.jasonsimpart.createdelightcore.mixin.constructionwand;

import de.cadentem.quality_food.capability.LevelData;
import de.cadentem.quality_food.core.Quality;
import de.cadentem.quality_food.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import thetadev.constructionwand.basics.WandUtil;

@Mixin(value = WandUtil.class, remap = false)
public class WandUtilMixin {
    @Inject(method = "placeBlock", at = @At(value = "RETURN"))
    private static void placeBlock$applyQuality(Level world, Player player, BlockState block, BlockPos pos, BlockItem item, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue() && Utils.isValidBlock(block))
            LevelData.set(world, pos, Quality.NONE_PLAYER_PLACED);
    }
}
