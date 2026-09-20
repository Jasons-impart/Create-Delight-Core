package io.github.jasonsimpart.mixin.create;

import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.fluids.tank.CreativeFluidTankBlockEntity;
import com.simibubi.create.content.fluids.transfer.EmptyingRecipe;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.fluid.FluidHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** The same recipes can be emptied into tanks by hand; return every product to the player. */
@Mixin(value = FluidHelper.class, remap = false)
public abstract class FluidHelperOutputsMixin {
    @Inject(method = "tryEmptyItemIntoBE", at = @At("HEAD"), cancellable = true)
    private static void createdelightcore$returnAllHandOutputs(Level level, Player player, InteractionHand hand,
            ItemStack held, SmartBlockEntity be, CallbackInfoReturnable<Boolean> cir) {
        var holder = AllRecipeTypes.EMPTYING.find(new SingleRecipeInput(held), level);
        if (holder.isEmpty() || !(holder.get().value() instanceof EmptyingRecipe recipe)
                || recipe.getRollableResults().size() <= 1) return;
        var tank = level.getCapability(Capabilities.FluidHandler.BLOCK, be.getBlockPos(), null);
        var fluid = recipe.getResultingFluid();
        if (tank == null || tank.fill(fluid, FluidAction.SIMULATE) != fluid.getAmount()) {
            cir.setReturnValue(false);
            return;
        }
        if (!level.isClientSide) {
            tank.fill(fluid, FluidAction.EXECUTE);
            if (!player.isCreative() && !(be instanceof CreativeFluidTankBlockEntity)) {
                var outputs = recipe.rollResults(level.random);
                var remaining = held.copy();
                remaining.shrink(1);
                int firstInventoryOutput = 0;
                if (remaining.isEmpty() && !outputs.isEmpty()) {
                    remaining = outputs.getFirst();
                    firstInventoryOutput = 1;
                }
                player.setItemInHand(hand, remaining);
                for (int i = firstInventoryOutput; i < outputs.size(); i++) {
                    player.getInventory().placeItemBackInInventory(outputs.get(i));
                }
            }
        }
        cir.setReturnValue(true);
    }
}
