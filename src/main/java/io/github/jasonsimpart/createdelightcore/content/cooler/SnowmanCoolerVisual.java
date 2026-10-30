// Adapted from Create More Recipes by iglee42 (MIT). See META-INF/licenses/create-more-recipes.txt.
package io.github.jasonsimpart.createdelightcore.content.cooler;

import java.util.function.Consumer;

import com.simibubi.create.content.processing.burner.ScrollInstance;
import io.github.jasonsimpart.createdelightcore.content.cooler.SnowmanCoolerModels;
import net.createmod.catnip.animation.AnimationTickHolder;
import org.jetbrains.annotations.Nullable;

import com.simibubi.create.AllPartialModels;
import com.simibubi.create.AllSpriteShifts;
import com.simibubi.create.foundation.render.AllInstanceTypes;

import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.visual.DynamicVisual;
import dev.engine_room.flywheel.api.visual.TickableVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import dev.engine_room.flywheel.lib.model.Models;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import dev.engine_room.flywheel.lib.transform.Translate;
import dev.engine_room.flywheel.lib.visual.AbstractBlockEntityVisual;
import dev.engine_room.flywheel.lib.visual.SimpleDynamicVisual;
import dev.engine_room.flywheel.lib.visual.SimpleTickableVisual;
import net.createmod.catnip.render.SpriteShiftEntry;
import net.createmod.catnip.math.AngleHelper;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;

public class SnowmanCoolerVisual extends AbstractBlockEntityVisual<SnowmanCoolerBlockEntity> implements SimpleDynamicVisual, SimpleTickableVisual {

	private SnowmanCoolerBlock.HeatLevel heatLevel;

	private final TransformedInstance head;

	private final boolean isInert;

	@Nullable
	private ScrollInstance flame;
	@Nullable
	private TransformedInstance goggles;
	@Nullable
	private TransformedInstance hat;

	private boolean validBlockAbove;

	public SnowmanCoolerVisual(VisualizationContext ctx, SnowmanCoolerBlockEntity blockEntity, float partialTick) {
		super(ctx, blockEntity, partialTick);

		heatLevel = SnowmanCoolerBlock.HeatLevel.IDLE;
		validBlockAbove = blockEntity.isValidBlockAbove();

		PartialModel blazeModel = SnowmanCoolerRenderer.getBlazeModel(heatLevel, validBlockAbove);
		isInert = blazeModel == SnowmanCoolerModels.SNOWMAN_INERT;

		head = instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.partial(blazeModel))
				.createInstance();

		head.light(LightTexture.FULL_BRIGHT);

		animate(partialTick);
	}

	@Override
	public void tick(TickableVisual.Context context) {
		blockEntity.tickAnimation();
	}

	@Override
	public void beginFrame(DynamicVisual.Context ctx) {
		if (!isVisible(ctx.frustum()) || doDistanceLimitThisFrame(ctx)) {
			return;
		}

		animate(ctx.partialTick());
	}

	private void animate(float partialTicks) {
		float animation = blockEntity.headAnimation.getValue(partialTicks) * .175f;

		boolean validBlockAbove = animation > 0.125f;
		SnowmanCoolerBlock.HeatLevel heatLevel = blockEntity.getHeatLevelForRender();

		if (validBlockAbove != this.validBlockAbove || heatLevel != this.heatLevel) {
			this.validBlockAbove = validBlockAbove;

			PartialModel blazeModel = SnowmanCoolerRenderer.getBlazeModel(heatLevel, validBlockAbove);
			instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.partial(blazeModel))
					.stealInstance(head);

			
			this.heatLevel = heatLevel;
		}

		// Switch between showing/hiding the flame
		if (validBlockAbove && flame == null) {
			setupFlameInstance();
		} else if (!validBlockAbove && flame != null) {
			flame.delete();
			flame = null;
		}

		if (blockEntity.goggles && goggles == null) {
			goggles = instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.partial(isInert ? AllPartialModels.BLAZE_GOGGLES_SMALL : AllPartialModels.BLAZE_GOGGLES))
					.createInstance();
			goggles.light(LightTexture.FULL_BRIGHT);
		} else if (!blockEntity.goggles && goggles != null) {
			goggles.delete();
			goggles = null;
		}

		boolean hatPresent = blockEntity.hat || blockEntity.stockKeeper;
		if (hatPresent && hat == null) {
			hat = instancerProvider()
					.instancer(InstanceTypes.TRANSFORMED,
						Models.partial(
							blockEntity.stockKeeper ? AllPartialModels.LOGISTICS_HAT : AllPartialModels.TRAIN_HAT))
					.createInstance();
			hat.light(LightTexture.FULL_BRIGHT);
		} else if (!hatPresent && hat != null) {
			hat.delete();
			hat = null;
		}

		var hashCode = blockEntity.hashCode();
		float time = AnimationTickHolder.getRenderTime(level);
		float renderTick = time + (hashCode % 13) * 16f;
		float offsetMult = heatLevel.isAtLeast(SnowmanCoolerBlock.HeatLevel.FADING) ? 64 : 16;
		float offset = Mth.sin((float) ((renderTick / 16f) % (2 * Math.PI))) / offsetMult;
		float headY = offset - (animation * .75f);

		float horizontalAngle = AngleHelper.rad(blockEntity.headAngle.getValue(partialTicks));

		head.setIdentityTransform()
				.translate(getVisualPosition())
				.translateY(headY)
				.translate(Translate.CENTER)
				.rotateY(horizontalAngle)
				.translateBack(Translate.CENTER)
				.setChanged();

		if (goggles != null) {
			goggles.setIdentityTransform()
					.translate(getVisualPosition())
					.translateY(headY + 8 / 16f)
					.translate(Translate.CENTER)
					.rotateY(horizontalAngle)
					.translateBack(Translate.CENTER)
					.setChanged();
		}

		if (hat != null) {
			hat.setIdentityTransform()
					.translate(getVisualPosition())
					.translateY(headY)
					.translateY(0.75f);
			hat.rotateCentered(horizontalAngle + Mth.PI, Direction.UP)
					.translate(0.5f, 0, 0.5f)
					.light(LightTexture.FULL_BRIGHT);

			hat.setChanged();
		}


	}

	private void setupFlameInstance() {
		flame = instancerProvider().instancer(AllInstanceTypes.SCROLLING, Models.partial(SnowmanCoolerModels.SNOWMAN_FLAME))
				.createInstance();

		flame.position(getVisualPosition())
				.light(LightTexture.FULL_BRIGHT);

		SpriteShiftEntry spriteShift =
				heatLevel == SnowmanCoolerBlock.HeatLevel.FREEZING ? SnowmanCoolerModels.SUPER_COOLER_FLAME : SnowmanCoolerModels.COOLER_FLAME;

		float spriteWidth = spriteShift.getTarget()
				.getU1()
				- spriteShift.getTarget()
				.getU0();

		float spriteHeight = spriteShift.getTarget()
				.getV1()
				- spriteShift.getTarget()
				.getV0();

		float speed = 1 / 32f + 1 / 64f * heatLevel.ordinal();

		flame.speedU = speed / 2;
		flame.speedV = speed;

		flame.scaleU = spriteWidth / 2;
		flame.scaleV = spriteHeight / 2;

		flame.diffU = spriteShift.getTarget().getU0() - spriteShift.getOriginal().getU0();
		flame.diffV = spriteShift.getTarget().getV0() - spriteShift.getOriginal().getV0();
	}

	@Override
	public void updateLight(float partialTick) {
	}

	@Override
	public void collectCrumblingInstances(Consumer<@Nullable Instance> consumer) {

	}

	@Override
	protected void _delete() {
		head.delete();
		if (flame != null) {
			flame.delete();
		}
		if (goggles != null) {
			goggles.delete();
		}
		if (hat != null) {
			hat.delete();
		}
	}
}
