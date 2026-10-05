package io.github.jasonsimpart.createdelightcore.content.humidity;

import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

public class SmartHumidityRegulatorRenderer extends KineticBlockEntityRenderer<SmartHumidityRegulatorBlockEntity> {
    public static final PartialModel COG = PartialModel.of(ResourceLocation.fromNamespaceAndPath(
            "createdelightcore", "block/smart_humidity_regulator/cog"));

    public SmartHumidityRegulatorRenderer(BlockEntityRendererProvider.Context context) { super(context); }

    @Override protected SuperByteBuffer getRotatedModel(SmartHumidityRegulatorBlockEntity be, BlockState state) {
        return CachedBuffers.partial(COG, state);
    }
}
