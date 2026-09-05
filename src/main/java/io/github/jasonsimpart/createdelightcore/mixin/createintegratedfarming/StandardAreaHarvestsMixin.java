package io.github.jasonsimpart.createdelightcore.mixin.createintegratedfarming;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import io.github.jasonsimpart.createdelightcore.content.util.QualityHarvestAutomationContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import plus.dragons.createintegratedfarming.api.harvester.AreaHarvestContext;
import plus.dragons.createintegratedfarming.common.farming.harvest.StandardAreaHarvests;

@Mixin(value = StandardAreaHarvests.class, remap = false)
public abstract class StandardAreaHarvestsMixin {
    @Inject(method = "harvest", at = @At("HEAD"))
    private static void createdelightcore$pushQualityContext(
            AreaHarvestContext context,
            BlockPos pos,
            BlockState state,
            CallbackInfoReturnable<Boolean> cir,
            @Share("createdelightcore$previousQuality") LocalRef<QualityHarvestAutomationContext.HarvestData> previous,
            @Share("createdelightcore$pushedQuality") LocalBooleanRef pushed) {
        IItemHandlerModifiable items = QualityHarvestAutomationContext.getActiveItems();
        if (items == null) {
            return;
        }

        previous.set(QualityHarvestAutomationContext.push(context.level(), pos, state, items));
        pushed.set(true);
    }

    @Inject(method = "harvest", at = @At("RETURN"))
    private static void createdelightcore$popQualityContext(
            AreaHarvestContext context,
            BlockPos pos,
            BlockState state,
            CallbackInfoReturnable<Boolean> cir,
            @Share("createdelightcore$previousQuality") LocalRef<QualityHarvestAutomationContext.HarvestData> previous,
            @Share("createdelightcore$pushedQuality") LocalBooleanRef pushed) {
        if (pushed.get()) {
            QualityHarvestAutomationContext.pop(previous.get());
        }
    }
}
