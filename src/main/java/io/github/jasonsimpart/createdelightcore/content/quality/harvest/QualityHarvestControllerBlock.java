package io.github.jasonsimpart.createdelightcore.content.quality.harvest;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;
import io.github.jasonsimpart.createdelightcore.registry.CDBlockEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;

public class QualityHarvestControllerBlock extends Block implements IBE<QualityHarvestControllerBlockEntity>, IWrenchable {
    public QualityHarvestControllerBlock(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                 BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        QualityHarvestControllerBlockEntity blockEntity = getBlockEntity(level, pos);
        if (blockEntity == null) {
            return InteractionResult.PASS;
        }

        ItemStack held = player.getItemInHand(hand);
        if (held.isEmpty() && player.isShiftKeyDown() && blockEntity.hasCalibrator()) {
            ItemStack removed = blockEntity.removeCalibrator();
            if (!player.addItem(removed)) {
                player.drop(removed, false);
            }
            sendStatus(player, blockEntity);
            return InteractionResult.SUCCESS;
        }

        if (QualityHarvestControllerBlockEntity.isCalibrator(held) && blockEntity.setCalibrator(held)) {
            if (!player.getAbilities().instabuild) {
                held.shrink(1);
            }
            sendStatus(player, blockEntity);
            return InteractionResult.SUCCESS;
        }

        if (QualityHarvestControllerBlockEntity.isLifeMatter(held)) {
            int inserted = blockEntity.insertLifeMatter(held, held.getCount());
            if (inserted > 0) {
                if (!player.getAbilities().instabuild) {
                    held.shrink(inserted);
                }
                sendStatus(player, blockEntity);
                return InteractionResult.SUCCESS;
            }
        }

        sendStatus(player, blockEntity);
        return InteractionResult.SUCCESS;
    }

    private void sendStatus(Player player, QualityHarvestControllerBlockEntity blockEntity) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        Component calibrator = blockEntity.hasCalibrator()
                ? blockEntity.getCalibrator().getHoverName()
                : Component.translatable("createdelightcore.quality_harvest_controller.no_calibrator")
                .withStyle(ChatFormatting.GRAY);
        serverPlayer.displayClientMessage(Component.translatable(
                "createdelightcore.quality_harvest_controller.status",
                blockEntity.getLifeMatterStored(),
                blockEntity.getLifeMatterCapacity(),
                calibrator), true);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {
        if (!moving && !state.is(newState.getBlock())) {
            withBlockEntityDo(level, pos, QualityHarvestControllerBlockEntity::dropContents);
        }
        IBE.onRemove(state, level, pos, newState);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        QualityHarvestControllerBlockEntity blockEntity = getBlockEntity(level, pos);
        if (blockEntity == null) {
            return 0;
        }
        return Math.max(0, Math.min(15,
                Math.round(15.0F * blockEntity.getLifeMatterStored() / blockEntity.getLifeMatterCapacity())));
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType type) {
        return false;
    }

    @Override
    public Class<QualityHarvestControllerBlockEntity> getBlockEntityClass() {
        return QualityHarvestControllerBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends QualityHarvestControllerBlockEntity> getBlockEntityType() {
        return CDBlockEntities.QUALITY_HARVEST_CONTROLLER.get();
    }
}
