package io.github.jasonsimpart.createdelightcore.eventhandlers;

import io.github.jasonsimpart.createdelightcore.CreateDelightCore;
import io.github.jasonsimpart.createdelightcore.content.configuration.ConfigurationModuleItem;
import io.github.jasonsimpart.createdelightcore.content.configuration.ConfigurationModuleKeys;
import io.github.jasonsimpart.createdelightcore.network.CDNetwork;
import io.github.jasonsimpart.createdelightcore.network.CycleConfigurationModePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CreateDelightCore.MODID, value = Dist.CLIENT)
public class ClientForgeEventHandler {
    @SubscribeEvent
    public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        if (event.getScrollDelta() == 0 || !ConfigurationModuleKeys.MODIFIER.isDown()) {
            return;
        }
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        InteractionHand hand;
        if (player.getMainHandItem().getItem() instanceof ConfigurationModuleItem) {
            hand = InteractionHand.MAIN_HAND;
        } else if (player.getOffhandItem().getItem() instanceof ConfigurationModuleItem) {
            hand = InteractionHand.OFF_HAND;
        } else {
            return;
        }
        event.setCanceled(true);
        int direction = event.getScrollDelta() > 0 ? -1 : 1;
        CDNetwork.CHANNEL.sendToServer(new CycleConfigurationModePacket(hand, direction));
    }
}
