package io.github.jasonsimpart.createdelightcore.registry;

import com.tterrag.registrate.util.entry.BlockEntityEntry;
import io.github.jasonsimpart.createdelightcore.content.order.machine.OrderParserBlockEntity;
import io.github.jasonsimpart.createdelightcore.content.order.machine.OrderRequesterBlockEntity;
import io.github.jasonsimpart.createdelightcore.content.quality.harvest.LifeMatterInjectorBlockEntity;
import io.github.jasonsimpart.createdelightcore.content.quality.harvest.QualityHarvestControllerBlockEntity;

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

    public static final BlockEntityEntry<QualityHarvestControllerBlockEntity> QUALITY_HARVEST_CONTROLLER =
            REGISTRATE.blockEntity("quality_harvest_controller", QualityHarvestControllerBlockEntity::new)
                    .validBlock(CDBlocks.QUALITY_HARVEST_CONTROLLER)
                    .register();

    public static final BlockEntityEntry<LifeMatterInjectorBlockEntity> LIFE_MATTER_INJECTOR =
            REGISTRATE.blockEntity("life_matter_injector", LifeMatterInjectorBlockEntity::new)
                    .validBlock(CDBlocks.LIFE_MATTER_INJECTOR)
                    .register();

    public static void init() {
    }
}
