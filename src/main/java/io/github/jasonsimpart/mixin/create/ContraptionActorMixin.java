package io.github.jasonsimpart.mixin.create;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.api.behaviour.movement.MovementBehaviour;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.actors.harvester.HarvesterMovementBehaviour;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.kinetics.deployer.DeployerMovementBehaviour;
import io.github.jasonsimpart.content.quality.harvest.QualityHarvestAutomationContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = AbstractContraptionEntity.class, remap = false)
public abstract class ContraptionActorMixin {
    @WrapOperation(
            method = "tickActors",
            at = @At(value = "INVOKE", target = "Lcom/simibubi/create/api/behaviour/movement/MovementBehaviour;visitNewPosition(Lcom/simibubi/create/content/contraptions/behaviour/MovementContext;Lnet/minecraft/core/BlockPos;)V")
    )
    private void createdelightcore$withQualityHarvestContext(
            MovementBehaviour behaviour,
            MovementContext context,
            BlockPos pos,
            Operation<Void> original
    ) {
        if (!(behaviour instanceof HarvesterMovementBehaviour) && !(behaviour instanceof DeployerMovementBehaviour)) {
            original.call(behaviour, context, pos);
            return;
        }
        BlockState state = context.world.getBlockState(pos);
        QualityHarvestAutomationContext.HarvestData previous = QualityHarvestAutomationContext.push(context, pos, state);
        try {
            original.call(behaviour, context, pos);
        } finally {
            QualityHarvestAutomationContext.pop(previous);
        }
    }
}
