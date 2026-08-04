package io.github.jasonsimpart.createdelightcore.eventhandlers;

import io.github.jasonsimpart.createdelightcore.CreateDelightCore;
import io.github.jasonsimpart.createdelightcore.content.order.machine.OrderParserScreen;
import io.github.jasonsimpart.createdelightcore.content.order.machine.OrderRequesterScreen;
import io.github.jasonsimpart.createdelightcore.content.configuration.ConfigurationModuleItemDecorator;
import io.github.jasonsimpart.createdelightcore.registry.CDItems;
import io.github.jasonsimpart.createdelightcore.registry.CDMenus;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterItemDecorationsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = CreateDelightCore.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEventHandler {
    @SubscribeEvent
    public static void registerItemDecorations(RegisterItemDecorationsEvent event) {
        ConfigurationModuleItemDecorator decorator = new ConfigurationModuleItemDecorator();
        event.register(CDItems.KINETIC_CONFIGURATION_MODULE.get(), decorator);
        event.register(CDItems.STRUCTURAL_CONFIGURATION_MODULE.get(), decorator);
        event.register(CDItems.FLUID_CONFIGURATION_MODULE.get(), decorator);
        event.register(CDItems.CONTROL_CONFIGURATION_MODULE.get(), decorator);
        event.register(CDItems.LOGISTICS_CONFIGURATION_MODULE.get(), decorator);
    }

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(CDMenus.ORDER_PARSER.get(), OrderParserScreen::new);
            MenuScreens.register(CDMenus.ORDER_REQUESTER.get(), OrderRequesterScreen::new);
        });
    }
}
