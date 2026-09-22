package io.github.jasonsimpart.content.quality.harvest;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;
import io.github.jasonsimpart.registry.ModBlockEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class LifeMatterInjectorBlock extends DirectionalBlock implements IBE<LifeMatterInjectorBlockEntity>, IWrenchable {
    public static final com.mojang.serialization.MapCodec<LifeMatterInjectorBlock> CODEC = simpleCodec(LifeMatterInjectorBlock::new);

    public LifeMatterInjectorBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected com.mojang.serialization.MapCodec<? extends DirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getNearestLookingDirection().getOpposite());
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack held, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        boolean relevant = QualityHarvestControllerBlockEntity.isLifeMatter(held);
        if (level.isClientSide) {
            return relevant ? ItemInteractionResult.SUCCESS : ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        LifeMatterInjectorBlockEntity blockEntity = getBlockEntity(level, pos);
        if (blockEntity == null) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (QualityHarvestControllerBlockEntity.isLifeMatter(held)) {
            int inserted = blockEntity.insertLifeMatter(held);
            if (inserted > 0 && !player.getAbilities().instabuild) {
                held.shrink(inserted);
            }
            sendStatus(player, blockEntity);
            return ItemInteractionResult.SUCCESS;
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        LifeMatterInjectorBlockEntity blockEntity = getBlockEntity(level, pos);
        if (blockEntity == null) {
            return InteractionResult.PASS;
        }

        if (player.isShiftKeyDown() && !blockEntity.getInputStack().isEmpty()) {
            ItemStack removed = blockEntity.removeInputStack();
            if (!player.addItem(removed)) {
                player.drop(removed, false);
            }
        }
        sendStatus(player, blockEntity);
        return InteractionResult.SUCCESS;
    }

    private void sendStatus(Player player, LifeMatterInjectorBlockEntity blockEntity) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        Component power = blockEntity.isPowered()
                ? Component.translatable("createdelightcore.life_matter_injector.powered").withStyle(ChatFormatting.RED)
                : Component.translatable("createdelightcore.life_matter_injector.ready").withStyle(ChatFormatting.GREEN);
        Component facing = Component.translatable("createdelightcore.direction."
                + blockEntity.getBlockState().getValue(FACING).getSerializedName());
        Component docking = dockingStatus(blockEntity);
        serverPlayer.displayClientMessage(Component.translatable(
                "createdelightcore.life_matter_injector.status",
                blockEntity.getInputStack().getCount(),
                blockEntity.getLastTransferred(),
                facing,
                docking,
                power), true);
    }

    private Component dockingStatus(LifeMatterInjectorBlockEntity blockEntity) {
        if (!blockEntity.hasControllerInRange()) {
            return Component.translatable("createdelightcore.life_matter_injector.docking.none")
                    .withStyle(ChatFormatting.GRAY);
        }
        int stored = Math.max(0, blockEntity.getLastTargetStored());
        if (!blockEntity.hasAcceptingControllerInRange()) {
            return Component.translatable("createdelightcore.life_matter_injector.docking.full",
                            stored, QualityHarvestControllerBlockEntity.CAPACITY)
                    .withStyle(ChatFormatting.YELLOW);
        }
        return Component.translatable("createdelightcore.life_matter_injector.docking.ready",
                        stored, QualityHarvestControllerBlockEntity.CAPACITY)
                .withStyle(ChatFormatting.GREEN);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {
        if (!moving && !state.is(newState.getBlock())) {
            withBlockEntityDo(level, pos, LifeMatterInjectorBlockEntity::dropContents);
        }
        IBE.onRemove(state, level, pos, newState);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        LifeMatterInjectorBlockEntity blockEntity = getBlockEntity(level, pos);
        if (blockEntity == null) {
            return 0;
        }
        return blockEntity.getInputStack().isEmpty() ? 0 : 15;
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
        return false;
    }

    @Override
    public Class<LifeMatterInjectorBlockEntity> getBlockEntityClass() {
        return LifeMatterInjectorBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends LifeMatterInjectorBlockEntity> getBlockEntityType() {
        return ModBlockEntities.LIFE_MATTER_INJECTOR.get();
    }
}
