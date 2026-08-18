package io.github.jasonsimpart.createdelightcore.content.order.data;

import net.minecraft.resources.ResourceLocation;

public record OrderSupplyData(ResourceLocation item,
                              String race,
                              int count,
                              int tickets,
                              int money,
                              int days) {
    public OrderSupplyData {
        race = race == null ? "" : race;
        count = Math.max(1, count);
        tickets = Math.max(0, tickets);
        money = Math.max(0, money);
        days = Math.max(1, days);
    }
}
