package io.github.jasonsimpart.createdelightcore.eventhandlers;

import io.github.jasonsimpart.createdelightcore.CreateDelightCore;
import io.github.jasonsimpart.createdelightcore.content.configuration.ConfigurationModuleItem;
import io.github.jasonsimpart.createdelightcore.content.configuration.ConfigurationModuleClientInput;
import io.github.jasonsimpart.createdelightcore.content.configuration.ConfigurationModuleManager;
import io.github.jasonsimpart.createdelightcore.content.configuration.ConfigurationModeSnapshot;
import io.github.jasonsimpart.createdelightcore.content.configuration.RadialConfigurationMenu;
import net.minecraft.client.Minecraft;
import net.createmod.catnip.gui.ScreenOpener;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = CreateDelightCore.MODID, value = Dist.CLIENT)
public class ClientForgeEventHandler {
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        ConfigurationModuleClientInput.tick(minecraft);
        Player player = minecraft.player;
        if (player == null || player.isSpectator() || minecraft.screen != null) {
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
        ItemStack stack = player.getItemInHand(hand);
        List<ConfigurationModeSnapshot> modes = ConfigurationModuleManager.getSnapshotAvailableModes(stack);
        if (modes.size() < 2) {
            return;
        }
        if (!ConfigurationModuleClientInput.consumeOpenClick(minecraft)) {
            return;
        }
        ScreenOpener.open(new RadialConfigurationMenu(hand, stack, modes,
                ConfigurationModuleManager.getSnapshotModeId(stack).orElse(null)));
    }
}
