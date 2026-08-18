package io.github.jasonsimpart.createdelightcore.content.order.machine;

import io.github.jasonsimpart.createdelightcore.registry.CDMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

public class OrderRequesterMenu extends OrderMachineMenu {
    public OrderRequesterMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        super(CDMenus.ORDER_REQUESTER.get(), containerId, playerInventory, data);
    }

    public OrderRequesterMenu(MenuType<OrderRequesterMenu> type, int containerId, Inventory playerInventory,
                              FriendlyByteBuf data) {
        super(type, containerId, playerInventory, data);
    }

    public OrderRequesterMenu(int containerId, Inventory playerInventory, OrderRequesterBlockEntity blockEntity) {
        super(CDMenus.ORDER_REQUESTER.get(), containerId, playerInventory, blockEntity);
    }
}
