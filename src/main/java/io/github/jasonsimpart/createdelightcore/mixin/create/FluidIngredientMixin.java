package io.github.jasonsimpart.createdelightcore.mixin.create;

import com.simibubi.create.foundation.fluid.FluidIngredient;
import io.github.jasonsimpart.createdelightcore.util.FluidTagResolver;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Field;
import java.util.List;

@Mixin(value = FluidIngredient.class, remap = false)
public abstract class FluidIngredientMixin {

    @Shadow
    public List<FluidStack> matchingFluidStacks;

    @Shadow
    public abstract int getRequiredAmount();

    @Inject(method = "getMatchingFluidStacks", at = @At("RETURN"), cancellable = true)
    private void createdelightcore$retryEmptyFluidTagResolution(CallbackInfoReturnable<List<FluidStack>> cir) {
        // Create caches even an empty tag result; retry so late/KubeJS tag additions can still appear in JEI.
        TagKey<Fluid> tag = createdelightcore$getFluidTag();
        if (tag == null)
            return;

        List<FluidStack> current = cir.getReturnValue();
        if (current != null && !current.isEmpty())
            return;

        List<FluidStack> resolved = FluidTagResolver.resolve(tag, getRequiredAmount());
        if (resolved.isEmpty()) {
            // Keep the cache open for a later retry instead of pinning this tag to an empty JEI ingredient list.
            matchingFluidStacks = null;
            return;
        }

        matchingFluidStacks = resolved;
        cir.setReturnValue(resolved);
    }

    @SuppressWarnings("unchecked")
    private TagKey<Fluid> createdelightcore$getFluidTag() {
        try {
            // Avoid a mixin accessor here; direct references to helper mixins can trip Mixin's package load guard.
            Field field = getClass().getDeclaredField("tag");
            field.setAccessible(true);
            Object value = field.get(this);
            return value instanceof TagKey<?> ? (TagKey<Fluid>) value : null;
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }
}
