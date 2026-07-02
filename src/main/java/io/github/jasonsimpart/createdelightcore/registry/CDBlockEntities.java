package io.github.jasonsimpart.createdelightcore.registry;

import com.tterrag.registrate.util.entry.BlockEntityEntry;
import io.github.jasonsimpart.createdelightcore.content.order.machine.OrderParserBlockEntity;
import io.github.jasonsimpart.createdelightcore.content.order.machine.OrderRequesterBlockEntity;

import static io.github.jasonsimpart.createdelightcore.CreateDelightCore.REGISTRATE;

public class CDBlockEntities {
    public static final BlockEntityEntry<OrderParserBlockEntity> ORDER_PARSER =
            REGISTRATE.<OrderParserBlockEntity>blockEntity("order_parser", OrderParserBlockEntity::new)
                    .validBlock(CDBlocks.ORDER_PARSER)
                    .register();

    public static final BlockEntityEntry<OrderRequesterBlockEntity> ORDER_REQUESTER =
            REGISTRATE.<OrderRequesterBlockEntity>blockEntity("order_requester", OrderRequesterBlockEntity::new)
                    .validBlock(CDBlocks.ORDER_REQUESTER)
                    .register();

    public static void init() {
    }
}
