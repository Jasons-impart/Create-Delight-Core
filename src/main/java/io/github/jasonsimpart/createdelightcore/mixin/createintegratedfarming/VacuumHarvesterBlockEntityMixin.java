package io.github.jasonsimpart.createdelightcore.mixin.createintegratedfarming;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.jasonsimpart.createdelightcore.content.quality.harvest.QualityHarvestInputInventory;
import io.github.jasonsimpart.createdelightcore.content.util.QualityHarvestAutomationContext;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.ItemStackHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import plus.dragons.createintegratedfarming.api.harvester.AreaHarvestContext;
import plus.dragons.createintegratedfarming.common.farming.vacuum.VacuumHarvesterBlockEntity;
import plus.dragons.createintegratedfarming.common.farming.vacuum.VacuumHarvesterHarvesting;

@Mixin(value = VacuumHarvesterBlockEntity.class, remap = false)
public abstract class VacuumHarvesterBlockEntityMixin {
    @Unique
    private ItemStackHandler createdelightcore$qualityInventory;

    @Inject(method = "getItemHandler", at = @At("HEAD"), cancellable = true)
    private void createdelightcore$exposeQualityInput(
            Direction side,
            CallbackInfoReturnable<net.minecraftforge.items.IItemHandler> cir) {
        if (side == Direction.DOWN) {
            cir.setReturnValue(createdelightcore$qualityInventory());
        }
    }

    @Inject(method = "write", at = @At("TAIL"))
    private void createdelightcore$writeQualityInventory(
            CompoundTag tag,
            boolean clientPacket,
            CallbackInfo ci) {
        tag.put(QualityHarvestInputInventory.INVENTORY_TAG,
                createdelightcore$qualityInventory().serializeNBT());
    }

    @Inject(method = "read", at = @At("TAIL"))
    private void createdelightcore$readQualityInventory(
            CompoundTag tag,
            boolean clientPacket,
            CallbackInfo ci) {
        createdelightcore$qualityInventory().deserializeNBT(
                tag.getCompound(QualityHarvestInputInventory.INVENTORY_TAG));
    }

    @Inject(method = "giveContentsTo", at = @At("TAIL"), cancellable = true)
    private void createdelightcore$giveQualityContents(
            Player player,
            CallbackInfoReturnable<Boolean> cir) {
        boolean gaveContents = cir.getReturnValue();
        ItemStackHandler inputs = createdelightcore$qualityInventory();
        for (int slot = 0; slot < inputs.getSlots(); slot++) {
            ItemStack stack = inputs.extractItem(slot, inputs.getSlotLimit(slot), false);
            if (!stack.isEmpty()) {
                player.getInventory().placeItemBackInInventory(stack);
                gaveContents = true;
            }
        }
        cir.setReturnValue(gaveContents);
    }

    @Inject(method = "destroy", at = @At("TAIL"))
    private void createdelightcore$dropQualityContents(CallbackInfo ci) {
        BlockEntity self = (BlockEntity) (Object) this;
        if (self.getLevel() != null) {
            com.simibubi.create.foundation.item.ItemHelper.dropContents(
                    self.getLevel(), self.getBlockPos(), createdelightcore$qualityInventory());
        }
    }

    @WrapOperation(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lplus/dragons/createintegratedfarming/common/farming/vacuum/VacuumHarvesterHarvesting;harvestArea(Lplus/dragons/createintegratedfarming/api/harvester/AreaHarvestContext;Lnet/minecraft/core/BlockPos;I)Lplus/dragons/createintegratedfarming/common/farming/vacuum/VacuumHarvesterHarvesting$HarvestResult;"
            )
    )
    private VacuumHarvesterHarvesting.HarvestResult createdelightcore$pushStationaryItems(
            AreaHarvestContext context,
            net.minecraft.core.BlockPos pos,
            int range,
            Operation<VacuumHarvesterHarvesting.HarvestResult> original) {
        return QualityHarvestAutomationContext.withActiveItems(
                createdelightcore$qualityInventory(),
                () -> original.call(context, pos, range));
    }

    @Unique
    private ItemStackHandler createdelightcore$qualityInventory() {
        if (createdelightcore$qualityInventory == null) {
            createdelightcore$qualityInventory = QualityHarvestInputInventory.create();
        }
        return createdelightcore$qualityInventory;
    }
}
