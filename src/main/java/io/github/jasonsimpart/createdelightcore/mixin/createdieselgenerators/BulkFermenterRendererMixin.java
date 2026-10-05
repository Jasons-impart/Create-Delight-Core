package io.github.jasonsimpart.createdelightcore.mixin.createdieselgenerators;

import com.jesz.createdieselgenerators.CDGPartialModels;
import com.jesz.createdieselgenerators.content.bulk_fermenter.BulkFermenterBlockEntity;
import com.jesz.createdieselgenerators.content.bulk_fermenter.BulkFermenterRenderer;
import com.jesz.createdieselgenerators.content.bulk_fermenter.BulkFermentingRecipe;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform.Sided;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringRenderer;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import io.github.jasonsimpart.createdelightcore.compat.createdieselgenerators.BulkFermenterFilteringAccess;
import io.github.jasonsimpart.createdelightcore.compat.createdieselgenerators.BulkFermenterPartialModels;
import net.createmod.catnip.data.Iterate;
import net.createmod.catnip.render.CachedBuffers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = BulkFermenterRenderer.class, remap = false)
public abstract class BulkFermenterRendererMixin extends SafeBlockEntityRenderer<BulkFermenterBlockEntity> {
    @Inject(
            method = "renderSafe(Lcom/jesz/createdieselgenerators/content/bulk_fermenter/BulkFermenterBlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void createdelightcore$renderNarrowGauge(BulkFermenterBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay, CallbackInfo ci) {
        ci.cancel();
        createdelightcore$renderFilterHolders(be, ms, buffer, light);
        if (!be.isController()) {
            FilteringRenderer.renderOnBlockEntity(be, partialTicks, ms, buffer, light, overlay);
            return;
        }

        BlockState blockState = be.getBlockState();
        VertexConsumer builder = buffer.getBuffer(RenderType.cutout());
        ms.pushPose();
        TransformStack.of(ms).translate(be.getWidth() / 2f, .5, be.getWidth() / 2f);

        float dialPivotY = .375f;
        float dialPivotZ = .5f;
        BulkFermenterFilteringAccess access = (BulkFermenterFilteringAccess) be;
        BulkFermentingRecipe recipe = access.createdelightcore$getCurrentRecipe();
        int width = access.createdelightcore$getWidth();
        int height = access.createdelightcore$getHeight();
        float progress = recipe == null
                ? 0
                : (float) Mth.clamp(Mth.lerp(partialTicks, be.processingTime + Math.sqrt(width * height), be.processingTime)
                / recipe.getProcessingDuration(), 0, 1);
        float gaugeOffset = width / 2f - .5f;
        float gaugeCenterY = .5f;
        float gaugeCenterZ = .5f;

        for (Direction direction : Iterate.horizontalDirections) {
            ms.pushPose();
            CachedBuffers.partial(CDGPartialModels.BULK_FERMENTER_GAUGE, blockState)
                    .rotateYDegrees(direction.toYRot())
                    .uncenter()
                    .translate(gaugeOffset, 0, 0)
                    .translate(0, gaugeCenterY, gaugeCenterZ)
                    .scale(1, .75f, .75f)
                    .translate(0, -gaugeCenterY, -gaugeCenterZ)
                    .light(light)
                    .renderInto(ms, builder);

            CachedBuffers.partial(AllPartialModels.BOILER_GAUGE_DIAL, blockState)
                    .rotateYDegrees(direction.toYRot())
                    .uncenter()
                    .translate(gaugeOffset, 0, 0)
                    .translate(0, gaugeCenterY, gaugeCenterZ)
                    .scale(1, .75f, .75f)
                    .translate(0, -gaugeCenterY, -gaugeCenterZ)
                    .translate(0, dialPivotY, dialPivotZ)
                    .rotateXDegrees(180 * progress - 90)
                    .translate(0, -dialPivotY, -dialPivotZ)
                    .light(light)
                    .renderInto(ms, builder);
            ms.popPose();
        }

        ms.popPose();
        FilteringRenderer.renderOnBlockEntity(be, partialTicks, ms, buffer, light, overlay);
    }

    @Unique
    private void createdelightcore$renderFilterHolders(BulkFermenterBlockEntity be, PoseStack ms, MultiBufferSource buffer, int light) {
        BulkFermenterFilteringAccess access = (BulkFermenterFilteringAccess) be;
        FilteringBehaviour filtering = access.createdelightcore$getFilter();
        if (filtering == null || !filtering.isActive() || !(filtering.getSlotPositioning() instanceof Sided slot)) {
            return;
        }

        BlockState state = be.getBlockState();
        VertexConsumer builder = buffer.getBuffer(RenderType.cutout());
        Direction previousSide = slot.getSide();
        try {
            for (Direction direction : Iterate.directions) {
                if (!access.createdelightcore$isFilterSideDisplayed(direction)) {
                    continue;
                }
                slot.fromSide(direction);
                if (!slot.shouldRender(be.getLevel(), be.getBlockPos(), state)) {
                    continue;
                }

                Vec3 location = slot.getLocalOffset(be.getLevel(), be.getBlockPos(), state);
                ms.pushPose();
                ms.translate(location.x, location.y, location.z);
                slot.rotate(be.getLevel(), be.getBlockPos(), state, ms);
                CachedBuffers.partial(BulkFermenterPartialModels.FILTER_HOLDER, state)
                        .translate(-.5f, -.5f, -.5f)
                        .light(light)
                        .renderInto(ms, builder);
                ms.popPose();
            }
        } finally {
            slot.fromSide(previousSide);
        }
    }
}
