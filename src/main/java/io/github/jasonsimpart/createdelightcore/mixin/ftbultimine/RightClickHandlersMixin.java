package io.github.jasonsimpart.createdelightcore.mixin.ftbultimine;

import de.cadentem.quality_food.capability.LevelData;
import de.cadentem.quality_food.core.Quality;
import de.cadentem.quality_food.util.DropData;
import de.cadentem.quality_food.util.QualityUtils;
import dev.ftb.mods.ftbultimine.FTBUltiminePlayerData;
import dev.ftb.mods.ftbultimine.ItemCollection;
import dev.ftb.mods.ftbultimine.RightClickHandlers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;
import java.util.List;


@Mixin(RightClickHandlers.class)
public class RightClickHandlersMixin {
    @Inject(method = "cropHarvesting", at = @At(value = "INVOKE", target = "Ldev/ftb/mods/ftbultimine/ItemCollection;add(Lnet/minecraft/world/item/ItemStack;)V"), locals = LocalCapture.CAPTURE_FAILHARD, remap = false)
    private static void cropHarvestingApplyQuality(ServerPlayer player, InteractionHand hand, BlockPos clickPos, Direction face, FTBUltiminePlayerData data, CallbackInfoReturnable<Integer> cir, int clicked, ItemCollection itemCollection, Iterator var7, BlockPos pos, BlockState state, BlockEntity blockEntity, List drops, Iterator var12, ItemStack stack) {
        QualityUtils.applyQuality(stack, state, LevelData.get(player.level(), pos), player, player.level().getBlockState(pos.below()));
    }
}
