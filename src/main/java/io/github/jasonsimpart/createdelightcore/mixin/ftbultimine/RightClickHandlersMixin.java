package io.github.jasonsimpart.createdelightcore.mixin.ftbultimine;

import com.llamalad7.mixinextras.sugar.Local;
import de.cadentem.quality_food.capability.LevelData;
import de.cadentem.quality_food.util.QualityUtils;
import dev.ftb.mods.ftbultimine.FTBUltiminePlayerData;
import dev.ftb.mods.ftbultimine.RightClickHandlers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;



@Mixin(RightClickHandlers.class)
public class RightClickHandlersMixin {
    @Inject(method = "cropHarvesting", at = @At(value = "INVOKE", target = "Ldev/ftb/mods/ftbultimine/ItemCollection;add(Lnet/minecraft/world/item/ItemStack;)V"), remap = false)
    private static void cropHarvestingApplyQuality(ServerPlayer player, InteractionHand hand, BlockPos clickPos, Direction face, FTBUltiminePlayerData data, CallbackInfoReturnable<Integer> cir, @Local(ordinal = 1) BlockPos pos, @Local BlockState state, @Local ItemStack stack) {
        QualityUtils.applyQuality(stack, state, LevelData.get(player.level(), pos), player, player.level().getBlockState(pos.below()));
    }
}
