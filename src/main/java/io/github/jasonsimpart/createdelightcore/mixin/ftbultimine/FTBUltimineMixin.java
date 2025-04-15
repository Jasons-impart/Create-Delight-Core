package io.github.jasonsimpart.createdelightcore.mixin.ftbultimine;

import com.llamalad7.mixinextras.sugar.Local;
import dev.architectury.event.EventResult;
import dev.ftb.mods.ftbultimine.CooldownTracker;
import dev.ftb.mods.ftbultimine.FTBUltimine;
import dev.ftb.mods.ftbultimine.FTBUltiminePlayerData;
import dev.ftb.mods.ftbultimine.config.FTBUltimineServerConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import se.mickelus.tetra.module.data.ToolData;
import se.mickelus.tetra.properties.IToolProvider;

import java.util.Collection;
import java.util.Comparator;

@Mixin(FTBUltimine.class)
public class FTBUltimineMixin {

    @Inject(method = "blockRightClick", at = @At(value = "INVOKE", target = "Ldev/ftb/mods/ftbultimine/FTBUltiminePlayerData;updateBlocks(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;ZI)Ldev/ftb/mods/ftbultimine/shape/ShapeContext;", shift = At.Shift.AFTER), remap = false, require = 1, cancellable = true)
    public void blockRightClickMixin(Player player, InteractionHand hand, BlockPos clickPos, Direction face, CallbackInfoReturnable<EventResult> cir, @Local ServerPlayer serverPlayer, @Local FTBUltiminePlayerData data) {
        int didWork = 0;
        ItemStack itemStack = serverPlayer.getItemInHand(hand);
        if (itemStack.getItem() instanceof IToolProvider toolProvider) {
            ToolData toolData = toolProvider.getToolData(itemStack);
            Collection<ToolAction> tools = toolData.getValues().stream()
                    .filter(tool -> toolData.getLevel(tool) > 0)
                    .sorted(player.isCrouching() ? Comparator.comparing(ToolAction::name).reversed() : Comparator.comparing(ToolAction::name))
                    .toList();
            for (ToolAction toolAction : tools) {
                if (FTBUltimineServerConfig.RIGHT_CLICK_HOE.get() && ToolActions.HOE_TILL.equals(toolAction)) {
                    didWork = RightClickHandlersInvoker.invokeFarmlandConversion(serverPlayer, hand, clickPos, data);
                } else if (FTBUltimineServerConfig.RIGHT_CLICK_AXE.get() && (ToolActions.AXE_STRIP.equals(toolAction) || ToolActions.AXE_SCRAPE.equals(toolAction) || ToolActions.AXE_WAX_OFF.equals(toolAction))) {
                    didWork = RightClickHandlersInvoker.invokeAxeStripping(serverPlayer, hand, clickPos, data);
                } else if (FTBUltimineServerConfig.RIGHT_CLICK_SHOVEL.get() && ToolActions.SHOVEL_FLATTEN.equals(toolAction)) {
                    didWork = RightClickHandlersInvoker.invokeShovelFlattening(serverPlayer, hand, clickPos, data);
                }
            }
        }

        if (didWork > 0) {
            player.swing(hand);
            if (!player.isCreative()) {
                CooldownTracker.setLastUltimineTime(player, System.currentTimeMillis());
                data.addPendingXPCost(Math.max(0, didWork - 1));
            }
            cir.setReturnValue(EventResult.interruptFalse());
        }

    }
}
