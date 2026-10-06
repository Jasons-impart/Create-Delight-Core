package io.github.jasonsimpart.createdelightcore.content.humidity;

import com.simibubi.create.content.kinetics.base.KineticBlock;
import com.simibubi.create.content.kinetics.simpleRelays.ICogWheel;
import com.simibubi.create.foundation.block.IBE;
import io.github.jasonsimpart.createdelightcore.registry.CDBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.fluids.FluidUtil;

public class SmartHumidityRegulatorBlock extends KineticBlock
        implements ICogWheel, IBE<SmartHumidityRegulatorBlockEntity> {
    public SmartHumidityRegulatorBlock(Properties properties) {
        super(properties);
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return Direction.Axis.Y;
    }

    @Override
    public boolean hasShaftTowards(LevelReader level, BlockPos pos, BlockState state, Direction face) {
        return false;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (hit.getDirection() != Direction.UP) return InteractionResult.PASS;
        if (level.isClientSide)
            return FluidUtil.getFluidHandler(player.getItemInHand(hand)).isPresent()
                    ? InteractionResult.SUCCESS : InteractionResult.PASS;
        return FluidUtil.interactWithFluidHandler(player, hand, level, pos, Direction.UP)
                ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }

    @Override
    public Class<SmartHumidityRegulatorBlockEntity> getBlockEntityClass() {
        return SmartHumidityRegulatorBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends SmartHumidityRegulatorBlockEntity> getBlockEntityType() {
        return CDBlockEntities.SMART_HUMIDITY_REGULATOR.get();
    }
}
