package io.github.jasonsimpart.createdelightcore.registry;

import io.github.jasonsimpart.createdelightcore.CreateDelightCore;
import io.github.jasonsimpart.createdelightcore.content.order.board.OrderBoardMenu;
import io.github.jasonsimpart.createdelightcore.content.order.machine.OrderRequesterMenu;
import io.github.jasonsimpart.createdelightcore.content.order.supply.SupplyCommissionMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class CDMenus {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, CreateDelightCore.MODID);

    public static final RegistryObject<MenuType<OrderRequesterMenu>> ORDER_REQUESTER =
            MENUS.register("order_requester", () -> IForgeMenuType.create(OrderRequesterMenu::new));

    public static final RegistryObject<MenuType<OrderBoardMenu>> ORDER_BOARD =
            MENUS.register("order_board", () -> IForgeMenuType.create(OrderBoardMenu::new));

    public static final RegistryObject<MenuType<SupplyCommissionMenu>> SUPPLY_COMMISSION_TABLE =
            MENUS.register("supply_commission_table", () -> IForgeMenuType.create(SupplyCommissionMenu::new));

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}
