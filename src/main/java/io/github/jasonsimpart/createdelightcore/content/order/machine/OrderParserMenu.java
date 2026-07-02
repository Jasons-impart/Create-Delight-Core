package io.github.jasonsimpart.createdelightcore.content.order.machine;

import io.github.jasonsimpart.createdelightcore.registry.CDMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

public class OrderParserMenu extends OrderMachineMenu {
    public OrderParserMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        super(CDMenus.ORDER_PARSER.get(), containerId, playerInventory, data);
    }

    public OrderParserMenu(MenuType<OrderParserMenu> type, int containerId, Inventory playerInventory,
                           FriendlyByteBuf data) {
        super(type, containerId, playerInventory, data);
    }

    public OrderParserMenu(int containerId, Inventory playerInventory, OrderParserBlockEntity blockEntity) {
        super(CDMenus.ORDER_PARSER.get(), containerId, playerInventory, blockEntity);
    }
}
