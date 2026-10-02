package io.github.jasonsimpart.createdelightcore.content.humidity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public class HumidityMachineRenderer extends KineticBlockEntityRenderer<HumidityMachineBlockEntity> {
    public HumidityMachineRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(HumidityMachineBlockEntity blockEntity, float partialTicks,
                              PoseStack poseStack, MultiBufferSource buffers, int light, int overlay) {
        if (((HumidityMachineBlock) blockEntity.getBlockState().getBlock()).isDryer()) {
            super.renderSafe(blockEntity, partialTicks, poseStack, buffers, light, overlay);
        }
    }

    @Override
    protected BlockState getRenderedBlockState(HumidityMachineBlockEntity blockEntity) {
        return shaft(Direction.Axis.Y);
    }
}
