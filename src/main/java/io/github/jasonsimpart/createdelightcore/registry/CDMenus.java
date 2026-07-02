package io.github.jasonsimpart.createdelightcore.registry;

import io.github.jasonsimpart.createdelightcore.CreateDelightCore;
import io.github.jasonsimpart.createdelightcore.content.order.machine.OrderParserMenu;
import io.github.jasonsimpart.createdelightcore.content.order.machine.OrderRequesterMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class CDMenus {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, CreateDelightCore.MODID);

    public static final RegistryObject<MenuType<OrderParserMenu>> ORDER_PARSER =
            MENUS.register("order_parser", () -> IForgeMenuType.create(OrderParserMenu::new));

    public static final RegistryObject<MenuType<OrderRequesterMenu>> ORDER_REQUESTER =
            MENUS.register("order_requester", () -> IForgeMenuType.create(OrderRequesterMenu::new));

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}
