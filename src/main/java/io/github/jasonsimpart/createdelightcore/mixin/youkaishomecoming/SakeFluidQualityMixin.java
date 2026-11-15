package io.github.jasonsimpart.createdelightcore.mixin.youkaishomecoming;

import de.cadentem.quality_food.core.Quality;
import de.cadentem.quality_food.util.QualityUtils;
import io.github.createdelight.qualityfoodfluids.api.QualityFoodFluidsApi;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Youkai's bottle capabilities construct their fluid without copying item NBT. */
@Pseudo
@Mixin(targets = {
        "dev.xkmc.youkaishomecoming.content.item.fluid.SakeFluidWrapper",
        "dev.xkmc.youkaishomecoming.content.item.fluid.SlipFluidWrapper"
}, remap = false)
public abstract class SakeFluidQualityMixin {
    @Shadow(remap = false) protected ItemStack container;

    @Inject(method = "getFluid", at = @At("RETURN"), cancellable = true)
    private void createdelightcore$inheritBottleQuality(CallbackInfoReturnable<FluidStack> cir) {
        FluidStack fluid = cir.getReturnValue();
        if (fluid.isEmpty()) return;
        var container = ((IFluidHandlerItem) this).getContainer();
        // Partially filled slip bottles already store the fluid and its quality in SakeFluid.
        if (container.getTag() != null && container.getTag().contains("SakeFluid", 10)) return;
        // The high-level emptying helper also reads this capability, which would recurse here.
        if (QualityFoodFluidsApi.canCarryQuality(fluid)) {
            cir.setReturnValue(QualityFoodFluidsApi.withQuality(fluid, QualityUtils.getQuality(container)));
        }
    }

    @Inject(method = "setFluid", at = @At("RETURN"))
    private void createdelightcore$inheritFilledFluidQuality(FluidStack fluid, CallbackInfo ci) {
        // A full slip bottle is replaced by a fresh item and no longer has embedded fluid NBT.
        // This capability owns its container; write only its quality without re-entering fluid helpers.
        container.removeTagKey(QualityUtils.QUALITY_TAG);
        Quality quality = QualityFoodFluidsApi.getQuality(fluid);
        if (quality != Quality.NONE) QualityUtils.applyQuality(container, quality);
    }
}
