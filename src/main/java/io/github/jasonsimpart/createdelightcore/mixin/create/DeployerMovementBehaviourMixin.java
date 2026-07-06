package io.github.jasonsimpart.createdelightcore.mixin.create;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.kinetics.deployer.DeployerMovementBehaviour;
import io.github.jasonsimpart.createdelightcore.content.util.QualityHarvestAutomationContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = DeployerMovementBehaviour.class, remap = false)
public abstract class DeployerMovementBehaviourMixin {
    @Inject(method = "visitNewPosition", at = @At("HEAD"))
    private void create_Delight_Core$pushQualityHarvestContext(
            MovementContext context,
            BlockPos pos,
            CallbackInfo ci,
            @Share("create_Delight_Core$previousQualityHarvest") LocalRef<QualityHarvestAutomationContext.HarvestData> previousRef
    ) {
        BlockState state = context.world.getBlockState(pos);
        previousRef.set(QualityHarvestAutomationContext.push(context, pos, state));
    }

    @Inject(method = "visitNewPosition", at = @At("RETURN"))
    private void create_Delight_Core$popQualityHarvestContext(
            MovementContext context,
            BlockPos pos,
            CallbackInfo ci,
            @Share("create_Delight_Core$previousQualityHarvest") LocalRef<QualityHarvestAutomationContext.HarvestData> previousRef
    ) {
        QualityHarvestAutomationContext.pop(previousRef.get());
    }
}
