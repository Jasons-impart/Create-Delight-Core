package io.github.jasonsimpart.createdelightcore.mixin.ftbultimine;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import dev.ftb.mods.ftbultimine.ItemCollection;
import dev.ftb.mods.ftbultimine.RightClickHandlers;
import dev.ftb.mods.ftbultimine.crops.ICropLikeHandler;
import io.github.jasonsimpart.createdelightcore.content.util.QualityFoodHarvestContext;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.mutable.MutableInt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RightClickHandlers.class)
public class RightClickHandlersMixin {
    @Inject(method = "lambda$cropHarvesting$2", at = @At("HEAD"), remap = false)
    private static void create_Delight_Core$pushCropHarvestContext(
            ServerPlayer player,
            BlockPos pos,
            BlockState state,
            ItemCollection itemCollection,
            MutableInt clicked,
            ICropLikeHandler handler,
            CallbackInfo ci,
            @Share("create_Delight_Core$previousHarvest") LocalRef<QualityFoodHarvestContext.HarvestData> previousHarvest
    ) {
        previousHarvest.set(QualityFoodHarvestContext.push(player, pos, state));
    }

    @Inject(method = "lambda$cropHarvesting$2", at = @At("RETURN"), remap = false)
    private static void create_Delight_Core$popCropHarvestContext(
            ServerPlayer player,
            BlockPos pos,
            BlockState state,
            ItemCollection itemCollection,
            MutableInt clicked,
            ICropLikeHandler handler,
            CallbackInfo ci,
            @Share("create_Delight_Core$previousHarvest") LocalRef<QualityFoodHarvestContext.HarvestData> previousHarvest
    ) {
        QualityFoodHarvestContext.pop(previousHarvest.get());
    }
}
