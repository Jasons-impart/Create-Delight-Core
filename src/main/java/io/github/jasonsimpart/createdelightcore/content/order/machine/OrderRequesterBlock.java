package io.github.jasonsimpart.createdelightcore.content.order.machine;

import io.github.jasonsimpart.createdelightcore.registry.CDBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class OrderRequesterBlock extends AbstractOrderMachineBlock<OrderRequesterBlockEntity> {
    public OrderRequesterBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos,
                                boolean moving) {
        withBlockEntityDo(level, pos, blockEntity -> blockEntity.onRedstonePowerChanged(level.hasNeighborSignal(pos)));
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        OrderRequesterBlockEntity blockEntity = getBlockEntity(level, pos);
        return blockEntity != null && blockEntity.didLastRequestSucceed() ? 15 : 0;
    }

    @Override
    public Class<OrderRequesterBlockEntity> getBlockEntityClass() {
        return OrderRequesterBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends OrderRequesterBlockEntity> getBlockEntityType() {
        return CDBlockEntities.ORDER_REQUESTER.get();
    }

    @Override
    protected OrderMachineMenu createMenu(int containerId, Inventory inventory, OrderRequesterBlockEntity blockEntity) {
        return new OrderRequesterMenu(containerId, inventory, blockEntity);
    }

    @Override
    protected Component getMenuTitle() {
        return Component.translatable("block.createdelightcore.order_requester");
    }
}
