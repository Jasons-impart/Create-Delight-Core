package io.github.jasonsimpart.createdelightcore.content.order.machine;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;
import io.github.jasonsimpart.createdelightcore.CreateDelightCore;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;

public abstract class AbstractOrderMachineBlock<T extends OrderMachineBlockEntity> extends Block implements IBE<T>, IWrenchable {
    protected AbstractOrderMachineBlock(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                 BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        T blockEntity = getBlockEntity(level, pos);
        if (blockEntity == null) {
            CreateDelightCore.LOGGER.warn("Order machine at {} is missing its block entity: {}", pos, state.getBlock());
            return InteractionResult.PASS;
        }

        if (handleOrderRemoval(blockEntity, level, pos, player, hand)) {
            return InteractionResult.SUCCESS;
        }

        handleOrderInsertion(blockEntity, player, hand);
        if (player instanceof ServerPlayer serverPlayer) {
            openMenu(serverPlayer, blockEntity);
        }
        return InteractionResult.SUCCESS;
    }

    protected boolean handleOrderRemoval(OrderMachineBlockEntity blockEntity, Level level, BlockPos pos,
                                         Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (held.isEmpty() && blockEntity.hasOrder() && player.isShiftKeyDown()) {
            ItemStack removed = blockEntity.removeOrderStack();
            if (!player.addItem(removed)) {
                player.drop(removed, false);
            }
            return true;
        }
        return false;
    }

    protected void handleOrderInsertion(OrderMachineBlockEntity blockEntity, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (!held.isEmpty() && !blockEntity.hasOrder() && blockEntity.setOrderStack(held)
                && !player.getAbilities().instabuild) {
            held.shrink(1);
        }
    }

    protected void openMenu(ServerPlayer player, T blockEntity) {
        NetworkHooks.openScreen(player, new SimpleMenuProvider(
                (containerId, inventory, ignored) -> createMenu(containerId, inventory, blockEntity),
                getMenuTitle()
        ), blockEntity.getBlockPos());
    }

    protected abstract OrderMachineMenu createMenu(int containerId, net.minecraft.world.entity.player.Inventory inventory,
                                                   T blockEntity);

    protected abstract Component getMenuTitle();

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {
        if (!state.is(newState.getBlock())) {
            withBlockEntityDo(level, pos, blockEntity -> {
                ItemStack stack = blockEntity.removeOrderStack();
                if (!stack.isEmpty()) {
                    Block.popResource(level, pos, stack);
                }
            });
        }
        IBE.onRemove(state, level, pos, newState);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        T blockEntity = getBlockEntity(level, pos);
        return blockEntity != null && blockEntity.hasOrder() ? 15 : 0;
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, net.minecraft.world.level.pathfinder.PathComputationType type) {
        return false;
    }
}
