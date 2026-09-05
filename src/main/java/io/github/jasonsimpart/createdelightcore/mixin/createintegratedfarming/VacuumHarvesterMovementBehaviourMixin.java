package io.github.jasonsimpart.createdelightcore.mixin.createintegratedfarming;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import io.github.jasonsimpart.createdelightcore.content.util.QualityHarvestAutomationContext;
import net.minecraft.core.BlockPos;
import io.github.jasonsimpart.createdelightcore.content.quality.harvest.QualityHarvestInputInventory;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.items.ItemStackHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import plus.dragons.createintegratedfarming.api.harvester.AreaHarvestContext;
import plus.dragons.createintegratedfarming.common.farming.vacuum.VacuumHarvesterHarvesting;
import plus.dragons.createintegratedfarming.common.farming.vacuum.VacuumHarvesterMovementBehaviour;

@Mixin(value = VacuumHarvesterMovementBehaviour.class, remap = false)
public abstract class VacuumHarvesterMovementBehaviourMixin {
    @WrapOperation(
            method = "harvestArea",
            at = @At(
                    value = "INVOKE",
                    target = "Lplus/dragons/createintegratedfarming/common/farming/vacuum/VacuumHarvesterHarvesting;harvestArea(Lplus/dragons/createintegratedfarming/api/harvester/AreaHarvestContext;Lnet/minecraft/core/BlockPos;I)Lplus/dragons/createintegratedfarming/common/farming/vacuum/VacuumHarvesterHarvesting$HarvestResult;"
            )
    )
    private VacuumHarvesterHarvesting.HarvestResult createdelightcore$pushMountedItems(
            AreaHarvestContext context,
            BlockPos pos,
            int range,
            Operation<VacuumHarvesterHarvesting.HarvestResult> original,
            @Local(argsOnly = true) MovementContext movementContext) {
        ItemStackHandler qualityInputs = QualityHarvestInputInventory.create(
                movementContext.blockEntityData == null ? new CompoundTag() : movementContext.blockEntityData);
        return QualityHarvestAutomationContext.withActiveItems(
                qualityInputs,
                () -> original.call(context, pos, range));
    }
}
