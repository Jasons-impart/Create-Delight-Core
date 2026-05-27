package io.github.jasonsimpart.mixin.cmr;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.fluid.SmartFluidTank;
import io.github.jasonsimpart.compat.cmr.CoolerStomachAccess;
import io.github.jasonsimpart.compat.cmr.CoolerStomachHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(targets = "fr.iglee42.cmr.cooler.SnowmanCoolerBlockEntity", remap = false)
public abstract class MixinSnowmanCoolerBlockEntity extends SmartBlockEntity implements CoolerStomachAccess {
    @Unique
    private SmartFluidTank createdelightcore$stomach;

    public MixinSnowmanCoolerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public SmartFluidTank createdelightcore$getStomach() {
        return createdelightcore$stomach;
    }

    @Inject(method = "addBehaviours", at = @At("TAIL"))
    private void createdelightcore$addStomach(List<BlockEntityBehaviour> behaviours, CallbackInfo ci) {
        createdelightcore$stomach = new SmartFluidTank(1000, contents -> notifyUpdate()) {
            @Override
            public boolean isFluidValid(@NotNull FluidStack stack) {
                return CoolerStomachHandler.LIQUID_COOLER_FUEL_MAP.containsKey(stack.getFluid());
            }
        };
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lfr/iglee42/cmr/cooler/SnowmanCoolerBlockEntity;updateBlockState()V", ordinal = 1), cancellable = true)
    private void createdelightcore$tickLiquidFuel(CallbackInfo ci) {
        if (CoolerStomachHandler.tick(this)) {
            ci.cancel();
        }
    }

    @Inject(method = "read", at = @At("TAIL"))
    private void createdelightcore$readStomach(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket, CallbackInfo ci) {
        if (createdelightcore$stomach != null && tag.contains("Stomach")) {
            createdelightcore$stomach.readFromNBT(registries, tag.getCompound("Stomach"));
        }
    }

    @Inject(method = "write", at = @At("TAIL"))
    private void createdelightcore$writeStomach(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket, CallbackInfo ci) {
        if (createdelightcore$stomach != null) {
            tag.put("Stomach", createdelightcore$stomach.writeToNBT(registries, new CompoundTag()));
        }
    }

    @Inject(method = "tryUpdateFuel", at = @At("HEAD"), cancellable = true)
    private void createdelightcore$tryUpdateLiquidFuel(ItemStack itemStack, boolean forceOverflow, boolean simulate, CallbackInfoReturnable<Boolean> cir) {
        CoolerStomachHandler.tryUpdateFuel(this, itemStack, forceOverflow, simulate, cir);
    }
}
