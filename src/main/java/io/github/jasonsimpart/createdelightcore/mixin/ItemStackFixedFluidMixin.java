package io.github.jasonsimpart.createdelightcore.mixin;

import io.github.jasonsimpart.createdelightcore.compat.alcohol.FixedItemFluidHandler;
import io.github.jasonsimpart.createdelightcore.compat.alcohol.ItemFluidContainers;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityProvider;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import javax.annotation.Nullable;

@Mixin(ItemStack.class)
public abstract class ItemStackFixedFluidMixin extends CapabilityProvider<ItemStack> {
    @Unique private FixedItemFluidHandler createdelightcore$fixedFluidHandler;
    @Unique private LazyOptional<IFluidHandlerItem> createdelightcore$fixedFluidCapability;
    @Unique private boolean createdelightcore$capsInvalid;

    protected ItemStackFixedFluidMixin(Class<ItemStack> baseClass) {
        super(baseClass);
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction side) {
        var existing = super.getCapability(capability, side);
        if (capability != ForgeCapabilities.FLUID_HANDLER_ITEM || createdelightcore$capsInvalid || existing.isPresent()) {
            return existing;
        }
        // Late lookup also serves ItemStacks created before startup definitions were frozen.
        if (createdelightcore$fixedFluidCapability == null) {
            if (createdelightcore$fixedFluidHandler == null) {
                ItemStack stack = (ItemStack) (Object) this;
                var rule = ItemFluidContainers.find(stack);
                if (rule == null) return existing;
                createdelightcore$fixedFluidHandler = new FixedItemFluidHandler(stack, rule);
            }
            createdelightcore$fixedFluidCapability = LazyOptional.of(() -> createdelightcore$fixedFluidHandler);
        }
        return createdelightcore$fixedFluidCapability.cast();
    }

    @Override
    public void invalidateCaps() {
        createdelightcore$capsInvalid = true;
        super.invalidateCaps();
        if (createdelightcore$fixedFluidCapability != null) {
            createdelightcore$fixedFluidCapability.invalidate();
            createdelightcore$fixedFluidCapability = null;
        }
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        createdelightcore$capsInvalid = false;
    }
}
