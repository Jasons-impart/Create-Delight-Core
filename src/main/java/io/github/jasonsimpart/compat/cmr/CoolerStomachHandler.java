package io.github.jasonsimpart.compat.cmr;

import com.mojang.datafixers.util.Pair;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.fluid.SmartFluidTank;
import fr.iglee42.cmr.cooler.SnowmanCoolerBlockEntity;
import fr.iglee42.cmr.cooler.SnowmanCoolerBlockEntity.FuelType;
import io.github.jasonsimpart.mixin.cmr.SnowmanCoolerAccessor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.Map;

public final class CoolerStomachHandler {
    public static final Map<Fluid, Pair<ResourceLocation, LiquidCoolerFuel>> LIQUID_COOLER_FUEL_MAP = new HashMap<>();

    private CoolerStomachHandler() {
    }

    public static boolean tick(SmartBlockEntity entity) {
        if (!(entity instanceof SnowmanCoolerBlockEntity cooler)
                || !(entity instanceof SnowmanCoolerAccessor coolerAccessor)
                || !(entity instanceof CoolerStomachAccess stomachAccess)) {
            return false;
        }

        SmartFluidTank stomach = stomachAccess.createdelightcore$getStomach();
        if (stomach == null || stomach.getFluid().isEmpty()) {
            return false;
        }

        FluidStack fluidStack = stomach.getFluid();
        Pair<ResourceLocation, LiquidCoolerFuel> entry = LIQUID_COOLER_FUEL_MAP.get(fluidStack.getFluid());
        if (entry == null) {
            return false;
        }

        LiquidCoolerFuel fuel = entry.getSecond();
        FuelType activeFuel = fuel.freezing() ? FuelType.SPECIAL : FuelType.NORMAL;
        if (activeFuel.ordinal() < coolerAccessor.createdelightcore$getActiveFuel().ordinal()) {
            return false;
        }

        if (fluidStack.getAmount() < fuel.amountConsumedPerTick()) {
            stomach.drain(fluidStack.getAmount(), IFluidHandler.FluidAction.EXECUTE);
            return false;
        }

        int newBurnTime = coolerAccessor.createdelightcore$getRemainingBurnTime() + fuel.burnTime();
        if (newBurnTime > getMaxHeatCapacity()) {
            return false;
        }

        coolerAccessor.createdelightcore$setActiveFuel(activeFuel);
        coolerAccessor.createdelightcore$setRemainingBurnTime(newBurnTime);
        cooler.updateBlockState();
        stomach.drain(fuel.amountConsumedPerTick(), IFluidHandler.FluidAction.EXECUTE);
        return true;
    }

    public static void tryUpdateFuel(@NotNull SmartBlockEntity entity, ItemStack itemStack, boolean forceOverflow, boolean simulate, CallbackInfoReturnable<Boolean> cir) {
        if (!(entity instanceof CoolerStomachAccess stomachAccess)) {
            return;
        }

        SmartFluidTank stomach = stomachAccess.createdelightcore$getStomach();
        if (stomach == null) {
            return;
        }

        IFluidHandlerItem handler = itemStack.getCapability(Capabilities.FluidHandler.ITEM);
        if (handler == null || handler.getTanks() != 1) {
            return;
        }

        FluidStack fluidStack = handler.getFluidInTank(0);
        if (fluidStack.isEmpty() || !LIQUID_COOLER_FUEL_MAP.containsKey(fluidStack.getFluid())) {
            return;
        }

        FluidStack stomachFluid = stomach.getFluid();
        if (!stomachFluid.isEmpty() && !stomachFluid.getFluid().isSame(fluidStack.getFluid())) {
            return;
        }

        if (stomachFluid.getAmount() + fluidStack.getAmount() > stomach.getCapacity() && !forceOverflow) {
            return;
        }

        if (!simulate) {
            FluidStack drained = handler.drain(fluidStack.getAmount(), IFluidHandler.FluidAction.EXECUTE);
            stomach.fill(drained, IFluidHandler.FluidAction.EXECUTE);
        }

        cir.setReturnValue(true);
    }

    private static int getMaxHeatCapacity() {
        return SnowmanCoolerBlockEntity.MAX_HEAT_CAPACITY;
    }

    public record LiquidCoolerFuel(int burnTime, boolean freezing, int amountConsumedPerTick) {
    }
}
