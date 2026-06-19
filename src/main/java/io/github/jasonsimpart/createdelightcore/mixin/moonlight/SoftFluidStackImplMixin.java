package io.github.jasonsimpart.createdelightcore.mixin.moonlight;

import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.mehvahdjukaar.moonlight.api.fluids.forge.SoftFluidStackImpl;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = SoftFluidStackImpl.class, remap = false)
public abstract class SoftFluidStackImplMixin {
    private static final String ORIGINAL_FORGE_FLUID_TAG = "CreatedelightCoreOriginalForgeFluid";

    @Inject(method = "fromForgeFluid", at = @At("RETURN"), cancellable = true)
    private static void createdelightcore$rememberOriginalForgeFluid(FluidStack forgeStack,
                                                                     CallbackInfoReturnable<SoftFluidStack> cir) {
        SoftFluidStack softStack = cir.getReturnValue();
        if (forgeStack.isEmpty() || softStack == null || softStack.isEmpty()) {
            return;
        }

        Fluid originalFluid = forgeStack.getFluid();
        if (originalFluid == softStack.getVanillaFluid()) {
            return;
        }

        ResourceLocation originalId = ForgeRegistries.FLUIDS.getKey(originalFluid);
        if (originalId == null) {
            return;
        }

        CompoundTag tag = softStack.getTag();
        tag = tag == null ? new CompoundTag() : tag.copy();
        tag.putString(ORIGINAL_FORGE_FLUID_TAG, originalId.toString());
        softStack.setTag(tag);
        cir.setReturnValue(softStack);
    }

    @Inject(method = "toForgeFluid(Lnet/mehvahdjukaar/moonlight/api/fluids/SoftFluidStack;)Lnet/minecraftforge/fluids/FluidStack;",
            at = @At("RETURN"), cancellable = true)
    private static void createdelightcore$restoreOriginalForgeFluid(SoftFluidStack softStack,
                                                                    CallbackInfoReturnable<FluidStack> cir) {
        CompoundTag tag = softStack.getTag();
        if (tag == null || !tag.contains(ORIGINAL_FORGE_FLUID_TAG)) {
            return;
        }

        Fluid originalFluid = ForgeRegistries.FLUIDS.getValue(ResourceLocation.parse(tag.getString(ORIGINAL_FORGE_FLUID_TAG)));
        if (originalFluid == null || originalFluid == Fluids.EMPTY) {
            return;
        }

        FluidStack restored = new FluidStack(originalFluid, cir.getReturnValue().getAmount());
        CompoundTag restoredTag = tag.copy();
        restoredTag.remove(ORIGINAL_FORGE_FLUID_TAG);
        if (!restoredTag.isEmpty()) {
            restored.setTag(restoredTag);
        }
        cir.setReturnValue(restored);
    }
}
