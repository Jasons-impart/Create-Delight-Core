package io.github.jasonsimpart.createdelightcore.mixin.northeastchinadelight;

import com.gunmu.northeast_china_delight.block.VatBlockEntity;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import de.cadentem.quality_food.core.Quality;
import io.github.createdelight.qualityfoodfluids.api.QualityFoodFluidsApi;
import io.github.jasonsimpart.createdelightcore.compat.northeastchinadelight.PackVinegar;
import io.github.jasonsimpart.createdelightcore.compat.northeastchinadelight.VatVinegarAccess;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(value = VatBlockEntity.class, remap = false)
public abstract class VatVinegarQualityMixin implements VatVinegarAccess {
    @Unique private static final String CREATE_DELIGHT_VINEGAR_QUALITY = "createdelightcore:vinegar_quality";
    @Unique private Quality createdelightcore$vinegarQuality = Quality.NONE;

    @Shadow public abstract Fluid brewedFluid();
    @Shadow public abstract int brewedMb();
    @Shadow public abstract FluidStack getFluidInTank(int tank);

    @Override
    public Quality createdelightcore$getVinegarQuality() {
        return createdelightcore$vinegarQuality;
    }

    @Override
    public void createdelightcore$setVinegarQuality(Quality quality) {
        createdelightcore$vinegarQuality = PackVinegar.capInitialQuality(quality);
    }

    @ModifyReturnValue(method = "getFluidInTank", at = @At("RETURN"))
    private FluidStack createdelightcore$showVinegarQuality(FluidStack fluid) {
        return createdelightcore$qualifyVinegar(fluid, createdelightcore$vinegarQuality);
    }

    @WrapMethod(method = "drain(ILnet/minecraftforge/fluids/capability/IFluidHandler$FluidAction;)Lnet/minecraftforge/fluids/FluidStack;")
    private FluidStack createdelightcore$drainVinegar(int amount, FluidAction action, Operation<FluidStack> original) {
        Quality quality = createdelightcore$vinegarQuality;
        return createdelightcore$qualifyVinegar(original.call(amount, action), quality);
    }

    @Inject(method = "drain(Lnet/minecraftforge/fluids/FluidStack;Lnet/minecraftforge/fluids/capability/IFluidHandler$FluidAction;)Lnet/minecraftforge/fluids/FluidStack;",
            at = @At("HEAD"), cancellable = true)
    private void createdelightcore$matchVinegarQuality(FluidStack request, FluidAction action,
                                                       CallbackInfoReturnable<FluidStack> cir) {
        if (!request.isEmpty() && PackVinegar.isFluid(request.getFluid()) && !request.isFluidEqual(getFluidInTank(0))) {
            cir.setReturnValue(FluidStack.EMPTY);
        }
    }

    @Unique
    private static FluidStack createdelightcore$qualifyVinegar(FluidStack fluid, Quality quality) {
        return !fluid.isEmpty() && PackVinegar.isFluid(fluid.getFluid())
                ? QualityFoodFluidsApi.withQuality(fluid, quality) : fluid;
    }

    @Inject(method = "setProductMb", at = @At("RETURN"))
    private void createdelightcore$clearDrainedVinegar(Fluid fluid, int amount, CallbackInfo ci) {
        if (amount <= 0 && fluid != null && PackVinegar.isFluid(fluid)) createdelightcore$vinegarQuality = Quality.NONE;
    }

    @Inject(method = "saveVatData", at = @At("TAIL"))
    private void createdelightcore$saveVinegarQuality(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
        if (brewedMb() > 0 && brewedFluid() != null && PackVinegar.isFluid(brewedFluid())) {
            tag.putInt(CREATE_DELIGHT_VINEGAR_QUALITY, createdelightcore$vinegarQuality.level());
        }
    }

    @Inject(method = "loadVatData", at = @At("TAIL"))
    private void createdelightcore$loadVinegarQuality(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
        int level = Math.max(0, Math.min(Quality.GOLD.level(), tag.getInt(CREATE_DELIGHT_VINEGAR_QUALITY)));
        createdelightcore$vinegarQuality = brewedMb() > 0 && brewedFluid() != null && PackVinegar.isFluid(brewedFluid())
                ? Quality.get(level) : Quality.NONE;
    }
}
