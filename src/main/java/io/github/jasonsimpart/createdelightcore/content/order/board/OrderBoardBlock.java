package io.github.jasonsimpart.createdelightcore.content.order.board;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;
import io.github.jasonsimpart.createdelightcore.registry.CDBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;

public class OrderBoardBlock extends Block implements IBE<OrderBoardBlockEntity>, IWrenchable {
    public OrderBoardBlock(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                 BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (player instanceof ServerPlayer serverPlayer) {
            NetworkHooks.openScreen(serverPlayer, new SimpleMenuProvider(
                    (containerId, inventory, ignored) -> new OrderBoardMenu(containerId, inventory, pos),
                    Component.translatable("block.createdelightcore.order_board")
            ), pos);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public Class<OrderBoardBlockEntity> getBlockEntityClass() {
        return OrderBoardBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends OrderBoardBlockEntity> getBlockEntityType() {
        return CDBlockEntities.ORDER_BOARD.get();
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {
        IBE.onRemove(state, level, pos, newState);
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType type) {
        return false;
    }
}
