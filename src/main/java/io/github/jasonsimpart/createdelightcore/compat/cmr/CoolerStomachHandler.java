package io.github.jasonsimpart.createdelightcore.compat.cmr;

import com.mojang.datafixers.util.Pair;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.fluid.SmartFluidTank;
import fr.iglee42.cmr.cooler.SnowmanCoolerBlock;
import fr.iglee42.cmr.cooler.SnowmanCoolerBlockEntity;
import io.github.jasonsimpart.createdelightcore.mixin.cmr.SnowmanCoolerAccessor;
import io.github.jasonsimpart.createdelightcore.util.Triplet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.Map;

public class CoolerStomachHandler {
    public static Map<Fluid, Pair<ResourceLocation, Triplet<Integer, Boolean, Integer>>> LIQUID_COOLER_FUEL_MAP = new HashMap<>();

    public static boolean tick(SmartBlockEntity entity) {
        if (!(entity instanceof SnowmanCoolerAccessor coolerAccessor)) return false;

        @SuppressWarnings("DataFlowIssue")
        SmartFluidTank stomach = (SmartFluidTank) entity.getCapability(ForgeCapabilities.FLUID_HANDLER).orElse(null);

        //noinspection ConstantValue
        if (stomach == null)
            return false;

        if (stomach.getFluid().getAmount() <= 0) return false;

        Triplet<Integer, Boolean, Integer> burnerProperty = LIQUID_COOLER_FUEL_MAP.get(
                stomach.getFluid().getFluid()).getSecond();

        if (burnerProperty == null)
            return false;

        boolean fluidSuperHeats = burnerProperty.getSecond();

        int mbConsuming = burnerProperty.getThird();

        if (stomach.getFluid().getAmount() < mbConsuming) {
            stomach.getFluid().setAmount(0);
            return false;
        }

        if (fluidSuperHeats)
            coolerAccessor.createdelightcore$invokeSetBlockHeat(SnowmanCoolerBlock.HeatLevel.FREEZING);
        else
            coolerAccessor.createdelightcore$invokeSetBlockHeat(SnowmanCoolerBlock.HeatLevel.COOLING);

        int newBurnTime = coolerAccessor.createdelightcore$getRemainingBurnTime() + burnerProperty.getFirst();

        if (newBurnTime > SnowmanCoolerBlockEntity.MAX_HEAT_CAPACITY)
            return false;

        coolerAccessor.createdelightcore$setRemainingBurnTime(newBurnTime);

        stomach.getFluid().shrink(mbConsuming);
        return true;
    }

    public static void tryUpdateFuel(@NotNull SmartBlockEntity entity, ItemStack itemStack, boolean forceOverflow, boolean simulate, CallbackInfoReturnable<Boolean> cir) {
        @SuppressWarnings("DataFlowIssue")
        SmartFluidTank stomach = (SmartFluidTank) entity.getCapability(ForgeCapabilities.FLUID_HANDLER).orElse(null);

        //noinspection ConstantValue
        if (stomach == null) return;

        if (!itemStack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).isPresent()) return;

        @SuppressWarnings("DataFlowIssue")
        IFluidHandler handler = itemStack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).orElse(null);

        if (!stomach.getFluid().isEmpty() && handler.getFluidInTank(0).getFluid() != stomach.getFluid().getFluid()) return;

        if (handler.getTanks() != 1) return;
        FluidStack fluidStack = handler.getFluidInTank(0);
        if (fluidStack.isEmpty()) return;
        if (!CoolerStomachHandler.LIQUID_COOLER_FUEL_MAP.containsKey(fluidStack.getFluid()))
            return;

        if (stomach.getFluid().getAmount() + fluidStack.getAmount() > stomach.getCapacity()) {
            if (!forceOverflow) return;
        }

        if (!simulate) {
            int amount = fluidStack.getAmount();
            FluidStack drained = handler.drain(amount, IFluidHandler.FluidAction.EXECUTE);
            stomach.fill(drained, IFluidHandler.FluidAction.EXECUTE);
        }

        cir.setReturnValue(true);
    }
}
