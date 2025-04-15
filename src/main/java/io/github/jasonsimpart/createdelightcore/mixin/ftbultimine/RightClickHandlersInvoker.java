package io.github.jasonsimpart.createdelightcore.mixin.ftbultimine;

import dev.ftb.mods.ftbultimine.FTBUltiminePlayerData;
import dev.ftb.mods.ftbultimine.RightClickHandlers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(RightClickHandlers.class)
public interface RightClickHandlersInvoker {

    @Invoker(value = "axeStripping", remap = false)
    public static int invokeAxeStripping(ServerPlayer player, InteractionHand hand, BlockPos clickPos, FTBUltiminePlayerData data) {
        return 0;
    }

    @Invoker(value = "shovelFlattening", remap = false)
    public static int invokeShovelFlattening(ServerPlayer player, InteractionHand hand, BlockPos clickPos, FTBUltiminePlayerData data) {
        return 0;
    }
    @Invoker(value = "farmlandConversion", remap = false)
    public static int invokeFarmlandConversion(ServerPlayer player, InteractionHand hand, BlockPos clickPos, FTBUltiminePlayerData data) {
        return 0;
    }
}
