package io.github.jasonsimpart.createdelightcore.mixin.supplementaries;

import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.mehvahdjukaar.moonlight.api.fluids.forge.SoftFluidStackImpl;
import net.mehvahdjukaar.supplementaries.common.block.faucet.FluidOffer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "net.mehvahdjukaar.supplementaries.common.block.faucet.APIFluidTankInteraction", remap = false)
public class SupplementariesFaucetFluidPrecisionMixin {

    @Unique
    private static final ThreadLocal<Integer> createdelightcore$pendingExactDrainMb = new ThreadLocal<>();

    @Inject(
            method = "fill(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/mehvahdjukaar/supplementaries/common/block/faucet/FluidOffer;)Ljava/lang/Integer;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void createdelightcore$fillNonWaterPrecisely(Level level, BlockPos pos, BlockEntity target,
                                                         FluidOffer offer,
                                                         CallbackInfoReturnable<Integer> cir) {
        createdelightcore$pendingExactDrainMb.remove();

        SoftFluidStack fluid = offer.fluid();
        int minAmount = offer.minAmount();
        if (!(fluid instanceof SoftFluidStackImpl impl)) return;

        FluidStack stack = impl.toForgeFluid();
        if (stack.isEmpty() || stack.getFluid().isSame(Fluids.WATER)) return;

        IFluidHandler handler = target.getCapability(ForgeCapabilities.FLUID_HANDLER, Direction.UP).orElse(null);
        if (handler == null) return;

        FluidStack attempt = stack.copy();
        attempt.setAmount(250 * minAmount);
        if (attempt.isEmpty()) {
            cir.setReturnValue(null);
            return;
        }

        int simulated = handler.fill(attempt, IFluidHandler.FluidAction.SIMULATE);
        if (simulated <= 0) {
            cir.setReturnValue(0);
            return;
        }

        FluidStack exactFill = attempt.copy();
        exactFill.setAmount(simulated);
        int filled = handler.fill(exactFill, IFluidHandler.FluidAction.EXECUTE);
        target.setChanged();

        if (filled > 0) {
            createdelightcore$pendingExactDrainMb.set(filled);
        }
        cir.setReturnValue(Mth.ceil(filled / 250.0f));
    }

    @Inject(
            method = "drain(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;Lnet/minecraft/world/level/block/entity/BlockEntity;I)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void createdelightcore$drainExactAmount(Level level, BlockPos pos, Direction dir, BlockEntity source,
                                                    int amount, CallbackInfo ci) {
        Integer exactDrain = createdelightcore$pendingExactDrainMb.get();
        if (exactDrain == null) return;

        createdelightcore$pendingExactDrainMb.remove();

        IFluidHandler handler = source.getCapability(ForgeCapabilities.FLUID_HANDLER, dir).orElse(null);
        if (handler == null) return;

        FluidStack simulated = handler.drain(exactDrain, IFluidHandler.FluidAction.SIMULATE);
        if (simulated.getAmount() != exactDrain) {
            return;
        }

        handler.drain(exactDrain, IFluidHandler.FluidAction.EXECUTE);
        source.setChanged();
        ci.cancel();
    }
}
