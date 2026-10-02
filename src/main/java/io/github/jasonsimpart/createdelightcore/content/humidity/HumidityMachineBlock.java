package io.github.jasonsimpart.createdelightcore.content.humidity;

import com.simibubi.create.content.kinetics.base.KineticBlock;
import com.simibubi.create.foundation.block.IBE;
import io.github.jasonsimpart.createdelightcore.registry.CDBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.fluids.FluidUtil;
import java.util.List;
import org.jetbrains.annotations.Nullable;

public class HumidityMachineBlock extends KineticBlock implements IBE<HumidityMachineBlockEntity> {
    private final boolean dryer;

    public HumidityMachineBlock(Properties properties, boolean dryer) {
        super(properties);
        this.dryer = dryer;
    }

    public boolean isDryer() {
        return dryer;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level,
                                List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("createdelightcore.humidity." + (dryer ? "dryer" : "sprinkler"))
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("createdelightcore.humidity." + (dryer ? "dryer_range" : "sprinkler_range"))
                .withStyle(ChatFormatting.GRAY));
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return Direction.Axis.Y;
    }

    @Override
    public boolean hasShaftTowards(LevelReader level, BlockPos pos, BlockState state, Direction face) {
        return dryer && face.getAxis() == Direction.Axis.Y;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (dryer) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return FluidUtil.getFluidHandler(player.getItemInHand(hand)).isPresent()
                    ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }
        return FluidUtil.interactWithFluidHandler(player, hand, level, pos, hit.getDirection())
                ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }

    @Override
    public Class<HumidityMachineBlockEntity> getBlockEntityClass() {
        return HumidityMachineBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends HumidityMachineBlockEntity> getBlockEntityType() {
        return CDBlockEntities.HUMIDITY_MACHINE.get();
    }
}
