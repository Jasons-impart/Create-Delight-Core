package io.github.jasonsimpart.createdelightcore.mixin.cmr;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.fluid.SmartFluidTank;
import fr.iglee42.cmr.cooler.SnowmanCoolerBlockEntity;
import io.github.jasonsimpart.createdelightcore.compat.cmr.CoolerStomachHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = SnowmanCoolerBlockEntity.class, remap = false)
public abstract class MixinSnowmanCoolerTileEntity extends SmartBlockEntity {
    @Unique public SmartFluidTank createdelightcore$stomach;

    public MixinSnowmanCoolerTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (isFluidHandlerCap(cap))
            return LazyOptional.of(() -> createdelightcore$stomach).cast();
        return super.getCapability(cap, side);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        createdelightcore$stomach = new SmartFluidTank(1000, (FluidStack contents)->{})
        {
            @Override
            public boolean isFluidValid(@NotNull FluidStack stack) {
                return CoolerStomachHandler.LIQUID_COOLER_FUEL_MAP.containsKey(stack.getFluid());
            }
        };
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lfr/iglee42/cmr/cooler/SnowmanCoolerBlockEntity;updateBlockState()V", ordinal = 1), cancellable = true)
    public void tick(CallbackInfo info) {
        if (CoolerStomachHandler.tick(this)) info.cancel();
    }

    @Inject(method = "read", at = @At("TAIL"))
    public void read(CompoundTag compound, boolean clientPacket, CallbackInfo ci) {
        if (createdelightcore$stomach != null) {
            createdelightcore$stomach.readFromNBT(compound.getCompound("Stomach"));
        }
    }

    @Inject(method = "write", at = @At("TAIL"))
    public void write(CompoundTag compound, boolean clientPacket, CallbackInfo ci) {
        if (createdelightcore$stomach != null) {
            compound.put("Stomach", createdelightcore$stomach.writeToNBT(new CompoundTag()));
        }
    }

    @Inject(method = "tryUpdateFuel", at = @At("HEAD"), cancellable = true)
    public void tryUpdateFuel(ItemStack itemStack, boolean forceOverflow, boolean simulate, CallbackInfoReturnable<Boolean> cir) {
        CoolerStomachHandler.tryUpdateFuel(this, itemStack, forceOverflow, simulate, cir);
    }
}
