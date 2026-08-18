package io.github.jasonsimpart.createdelightcore.content.order.supply;

import com.simibubi.create.foundation.block.IBE;
import io.github.jasonsimpart.createdelightcore.registry.CDBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.network.NetworkHooks;

public class SupplyCommissionBlock extends Block implements IBE<SupplyCommissionBlockEntity> {
    public SupplyCommissionBlock(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                 BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        SupplyCommissionBlockEntity blockEntity = getBlockEntity(level, pos);
        if (blockEntity != null && player instanceof ServerPlayer serverPlayer) {
            NetworkHooks.openScreen(serverPlayer, new SimpleMenuProvider(
                    (containerId, inventory, ignored) -> new SupplyCommissionMenu(containerId, inventory, blockEntity),
                    Component.translatable("block.createdelightcore.supply_commission_table")
            ), pos);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public Class<SupplyCommissionBlockEntity> getBlockEntityClass() {
        return SupplyCommissionBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends SupplyCommissionBlockEntity> getBlockEntityType() {
        return CDBlockEntities.SUPPLY_COMMISSION_TABLE.get();
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.BLOCK;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {
        if (!state.is(newState.getBlock())) {
            withBlockEntityDo(level, pos, blockEntity -> {
                ItemStackHandler inventory = blockEntity.inventory();
                for (int slot = 0; slot < inventory.getSlots(); slot++) {
                    ItemStack stack = inventory.getStackInSlot(slot);
                    if (!stack.isEmpty()) {
                        popResource(level, pos, stack.copy());
                    }
                }
                blockEntity.readyPackages().forEach(stack -> popResource(level, pos, stack));
                blockEntity.releaseCommissionsOnRemoval();
            });
        }
        IBE.onRemove(state, level, pos, newState);
    }
}
