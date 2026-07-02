package io.github.jasonsimpart.createdelightcore.content.order.machine;

import io.github.jasonsimpart.createdelightcore.registry.CDBlockEntities;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class OrderParserBlock extends AbstractOrderMachineBlock<OrderParserBlockEntity> {
    public OrderParserBlock(Properties properties) {
        super(properties);
    }

    @Override
    public Class<OrderParserBlockEntity> getBlockEntityClass() {
        return OrderParserBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends OrderParserBlockEntity> getBlockEntityType() {
        return CDBlockEntities.ORDER_PARSER.get();
    }

    @Override
    protected OrderMachineMenu createMenu(int containerId, Inventory inventory, OrderParserBlockEntity blockEntity) {
        return new OrderParserMenu(containerId, inventory, blockEntity);
    }

    @Override
    protected Component getMenuTitle() {
        return Component.translatable("block.createdelightcore.order_parser");
    }
}
