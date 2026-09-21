package io.github.jasonsimpart.mixin.constructionwand;

import de.cadentem.quality_food.core.attachments.AttachmentHandler;
import de.cadentem.quality_food.core.codecs.Quality;
import de.cadentem.quality_food.util.Utils;
import nadiendev.constructionwand.basics.WandUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = WandUtil.class, remap = false)
public abstract class WandUtilMixin {
    @Inject(method = "placeBlock", at = @At("RETURN"))
    private static void createdelightcore$markPlayerPlaced(Level world, Player player, BlockState block, BlockPos pos, @Nullable ItemStack item, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue() && Utils.isValidBlock(block.getBlock())) {
            world.getData(AttachmentHandler.LEVEL_DATA).set(pos, Quality.PLAYER_PLACED);
        }
    }
}
