package io.github.jasonsimpart.createdelightcore.content.order.machine;

import io.github.jasonsimpart.createdelightcore.registry.CDBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class OrderParserBlockEntity extends OrderMachineBlockEntity {
    public OrderParserBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public OrderParserBlockEntity(BlockPos pos, BlockState state) {
        this(CDBlockEntities.ORDER_PARSER.get(), pos, state);
    }
}
