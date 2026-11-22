package io.github.jasonsimpart.createdelightcore.compat.alcohol;

import de.cadentem.quality_food.util.QualityUtils;
import io.github.createdelight.qualityfoodfluids.api.QualityFoodFluidsApi;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fml.ModList;

import java.util.Objects;

/** A whole serving with Forge's replacement-container semantics. */
public final class FixedItemFluidHandler implements IFluidHandlerItem {
    private final ItemStack fullContainer;
    private final ItemFluidContainers.Rule rule;
    private ItemStack emptyContainer;

    public FixedItemFluidHandler(ItemStack fullContainer, ItemFluidContainers.Rule rule) {
        this.fullContainer = fullContainer;
        this.rule = rule;
    }

    @Override
    public ItemStack getContainer() {
        return emptyContainer == null ? fullContainer : emptyContainer;
    }

    @Override
    public int getTanks() { return 1; }

    @Override
    public int getTankCapacity(int tank) { return tank == 0 ? rule.amount() : 0; }

    @Override
    public FluidStack getFluidInTank(int tank) {
        if (tank != 0 || emptyContainer != null || fullContainer.isEmpty()) return FluidStack.EMPTY;
        var fluid = new FluidStack(rule.fluid(), rule.amount());
        return ModList.get().isLoaded("quality_food_fluids")
                ? QualityFoodFluidsApi.withQuality(fluid, QualityUtils.getQuality(fullContainer)) : fluid;
    }

    @Override
    public boolean isFluidValid(int tank, FluidStack stack) { return false; }

    @Override
    public int fill(FluidStack resource, FluidAction action) { return 0; }

    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        if (resource.isEmpty() || resource.getAmount() < rule.amount() || fullContainer.getCount() != 1) {
            return FluidStack.EMPTY;
        }
        var held = getFluidInTank(0);
        if (held.isEmpty() || resource.getFluid() != held.getFluid() || !Objects.equals(resource.getTag(), held.getTag())) {
            return FluidStack.EMPTY;
        }
        return drainServing(held, action);
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        if (maxDrain < rule.amount() || fullContainer.getCount() != 1) return FluidStack.EMPTY;
        return drainServing(getFluidInTank(0), action);
    }

    private FluidStack drainServing(FluidStack held, FluidAction action) {
        if (!held.isEmpty() && action.execute()) emptyContainer = new ItemStack(rule.emptyContainer());
        return held;
    }
}
