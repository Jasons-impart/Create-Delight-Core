package io.github.jasonsimpart.createdelightcore.mixin.create;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.simibubi.create.content.contraptions.actors.harvester.HarvesterMovementBehaviour;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import io.github.jasonsimpart.createdelightcore.content.util.QualityHarvestAutomationContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(value = HarvesterMovementBehaviour.class, remap = false)
public abstract class HarvesterMovementBehaviourMixin {
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

    @ModifyArg(
            method = "visitNewPosition",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/foundation/utility/BlockHelper;destroyBlockAs(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;FLjava/util/function/Consumer;)V"
            ),
            index = 5
    )
    private Consumer<ItemStack> create_Delight_Core$applyAutomatedHarvestQuality(Consumer<ItemStack> original) {
        return stack -> {
            QualityHarvestAutomationContext.applyQuality(stack);
            original.accept(stack);
        };
    }
}
