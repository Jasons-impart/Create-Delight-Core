package io.github.jasonsimpart.createdelightcore.content.order.machine;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class OrderRequesterScreen extends OrderMachineScreen<OrderRequesterMenu> {
    public OrderRequesterScreen(OrderRequesterMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }
}
