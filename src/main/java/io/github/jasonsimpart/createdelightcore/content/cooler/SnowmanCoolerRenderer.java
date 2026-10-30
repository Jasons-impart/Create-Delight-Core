// Adapted from Create More Recipes by iglee42 (MIT). See META-INF/licenses/create-more-recipes.txt.
package io.github.jasonsimpart.createdelightcore.content.cooler;

import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.AllSpriteShifts;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.contraptions.render.ContraptionMatrices;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlockEntity;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;

import com.simibubi.create.foundation.virtualWorld.VirtualRenderWorld;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import io.github.jasonsimpart.createdelightcore.content.cooler.SnowmanCoolerModels;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.animation.LerpedFloat;
import net.createmod.catnip.math.AngleHelper;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SpriteShiftEntry;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import io.github.jasonsimpart.createdelightcore.content.cooler.SnowmanCoolerBlock.HeatLevel;

public class SnowmanCoolerRenderer extends SafeBlockEntityRenderer<SnowmanCoolerBlockEntity> {

	public SnowmanCoolerRenderer(BlockEntityRendererProvider.Context context) {}

	@Override
	protected void renderSafe(SnowmanCoolerBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource bufferSource,
		int light, int overlay) {
		SnowmanCoolerBlock.HeatLevel heatLevel = be.getHeatLevelFromBlock();

		Level level = be.getLevel();
		BlockState blockState = be.getBlockState();
		float animation = be.headAnimation.getValue(partialTicks) * .175f;
		float horizontalAngle = AngleHelper.rad(be.headAngle.getValue(partialTicks));
		boolean canDrawFlame = heatLevel.isAtLeast(SnowmanCoolerBlock.HeatLevel.FADING);
		boolean drawGoggles = be.goggles;
		PartialModel drawHat = be.hat ? AllPartialModels.TRAIN_HAT : be.stockKeeper ? AllPartialModels.LOGISTICS_HAT : null;
		int hashCode = be.hashCode();

		renderShared(ms, null, bufferSource,
			level, blockState, heatLevel, animation, horizontalAngle,
			canDrawFlame, drawGoggles, drawHat, hashCode);
	}

	public static void renderInContraption(MovementContext context, VirtualRenderWorld renderWorld,
										   ContraptionMatrices matrices, MultiBufferSource bufferSource, LerpedFloat headAngle, boolean conductor) {
		BlockState state = context.state;
		SnowmanCoolerBlock.HeatLevel heatLevel = SnowmanCoolerBlock.getHeatLevelOf(state);

		if (!heatLevel.isAtLeast(SnowmanCoolerBlock.HeatLevel.FADING))
			heatLevel = SnowmanCoolerBlock.HeatLevel.FADING;

		Level level = context.world;
		float horizontalAngle = AngleHelper.rad(headAngle.getValue(AnimationTickHolder.getPartialTicks(level)));
		boolean drawGoggles = context.blockEntityData.contains("Goggles");
		boolean drawHat = conductor || context.blockEntityData.contains("TrainHat");
		int hashCode = context.hashCode();

		renderShared(matrices.getViewProjection(), matrices.getModel(), bufferSource,
			level, state, heatLevel, 0, horizontalAngle,
			false, drawGoggles, drawHat ? AllPartialModels.TRAIN_HAT : null, hashCode);
	}

	public static void renderShared(PoseStack ms, @Nullable PoseStack modelTransform, MultiBufferSource bufferSource,
		Level level, BlockState blockState, SnowmanCoolerBlock.HeatLevel heatLevel, float animation, float horizontalAngle,
		boolean canDrawFlame, boolean drawGoggles, PartialModel drawHat, int hashCode) {

		boolean blockAbove = animation > 0.125f;
		float time = AnimationTickHolder.getRenderTime(level);
		float renderTick = time + (hashCode % 13) * 16f;
		float offsetMult = heatLevel.isAtLeast(SnowmanCoolerBlock.HeatLevel.FADING) ? 64 : 16;
		float offset = Mth.sin((float) ((renderTick / 16f) % (2 * Math.PI))) / offsetMult;
		float offset1 = Mth.sin((float) ((renderTick / 16f + Math.PI) % (2 * Math.PI))) / offsetMult;
		float offset2 = Mth.sin((float) ((renderTick / 16f + Math.PI / 2) % (2 * Math.PI))) / offsetMult;
		float headY = offset - (animation * .75f);

		ms.pushPose();

		var blazeModel = getBlazeModel(heatLevel, blockAbove);

		SuperByteBuffer blazeBuffer = CachedBuffers.partial(blazeModel, blockState);
		if (modelTransform != null)
			blazeBuffer.transform(modelTransform);
		blazeBuffer.translate(0, headY, 0);
		draw(blazeBuffer, horizontalAngle, ms, bufferSource.getBuffer(RenderType.solid()));

		if (drawGoggles) {
			PartialModel gogglesModel = blazeModel == SnowmanCoolerModels.SNOWMAN_INERT
					? AllPartialModels.BLAZE_GOGGLES_SMALL : AllPartialModels.BLAZE_GOGGLES;

			SuperByteBuffer gogglesBuffer = CachedBuffers.partial(gogglesModel, blockState);
			if (modelTransform != null)
				gogglesBuffer.transform(modelTransform);
			gogglesBuffer.translate(0, headY + 8 / 16f, 0);
			draw(gogglesBuffer, horizontalAngle, ms, bufferSource.getBuffer(RenderType.solid()));
		}

		if (drawHat != null) {
			SuperByteBuffer hatBuffer = CachedBuffers.partial(drawHat, blockState);
			if (modelTransform != null)
				hatBuffer.transform(modelTransform);
			hatBuffer.translate(0, headY, 0);
			if (blazeModel == SnowmanCoolerModels.SNOWMAN_INERT) {
				hatBuffer.translateY(0.5f)
						.center()
						.scale(0.75f)
						.uncenter();
			} else {
				hatBuffer.translateY(0.75f);
			}
			VertexConsumer cutout = bufferSource.getBuffer(RenderType.cutoutMipped());
			hatBuffer
					.rotateCentered(horizontalAngle + Mth.PI, Direction.UP)
					.translate(0.5f, 0, 0.5f)
					.light(LightTexture.FULL_BRIGHT)
					.renderInto(ms, cutout);
		}

		if (canDrawFlame && blockAbove) {
			SpriteShiftEntry spriteShift =
					heatLevel == HeatLevel.FREEZING ? SnowmanCoolerModels.SUPER_COOLER_FLAME : SnowmanCoolerModels.COOLER_FLAME;

			float spriteWidth = spriteShift.getTarget()
					.getU1()
					- spriteShift.getTarget()
					.getU0();

			float spriteHeight = spriteShift.getTarget()
					.getV1()
					- spriteShift.getTarget()
					.getV0();

			float speed = 1 / 32f + 1 / 64f * heatLevel.ordinal();

			double vScroll = speed * time;
			vScroll = vScroll - Math.floor(vScroll);
			vScroll = vScroll * spriteHeight / 2;

			double uScroll = speed * time / 2;
			uScroll = uScroll - Math.floor(uScroll);
			uScroll = uScroll * spriteWidth / 2;

			SuperByteBuffer flameBuffer = CachedBuffers.partial(SnowmanCoolerModels.SNOWMAN_FLAME, blockState);
			if (modelTransform != null)
				flameBuffer.transform(modelTransform);
			flameBuffer.shiftUVScrolling(spriteShift, (float) uScroll, (float) vScroll);

			VertexConsumer cutout = bufferSource.getBuffer(RenderType.cutoutMipped());
			draw(flameBuffer, horizontalAngle, ms, cutout);
		}

		ms.popPose();
	}

	public static PartialModel getBlazeModel(SnowmanCoolerBlock.HeatLevel heatLevel, boolean blockAbove) {
		if (heatLevel.isAtLeast(HeatLevel.FREEZING)) {
			return blockAbove ? SnowmanCoolerModels.SNOWMAN_SUPER_ACTIVE : SnowmanCoolerModels.SNOWMAN_SUPER;
		} else if (heatLevel.isAtLeast(SnowmanCoolerBlock.HeatLevel.FADING)) {
			return blockAbove ? SnowmanCoolerModels.SNOWMAN_ACTIVE
				: SnowmanCoolerModels.SNOWMAN_IDLE;
		} else {
			return SnowmanCoolerModels.SNOWMAN_INERT;
		}
	}

	private static void draw(SuperByteBuffer buffer, float horizontalAngle, PoseStack ms, VertexConsumer vc) {
		buffer.rotateCentered(horizontalAngle, Direction.UP)
			.light(LightTexture.FULL_BRIGHT)
			.renderInto(ms, vc);
	}
}
