package io.github.jasonsimpart.createdelightcore.mixin.create;

import com.simibubi.create.foundation.fluid.FluidIngredient;
import io.github.jasonsimpart.createdelightcore.util.FluidTagResolver;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = FluidIngredient.FluidTagIngredient.class, remap = false)
public abstract class FluidTagIngredientMixin extends FluidIngredient {

    private static final int CDC_TAG_SYNC_MARKER = -1;

    @Shadow
    protected TagKey<Fluid> tag;

    @Inject(method = "determineMatchingFluidStacks", at = @At("RETURN"), cancellable = true)
    private void createdelightcore$resolveFluidTagFromHolders(CallbackInfoReturnable<List<FluidStack>> cir) {
        List<FluidStack> resolved = cir.getReturnValue();
        if ((resolved != null && !resolved.isEmpty()) || tag == null)
            return;

        List<FluidStack> stacks = FluidTagResolver.resolve(tag, getRequiredAmount());
        if (!stacks.isEmpty())
            cir.setReturnValue(stacks);
    }

    @Inject(method = "writeInternal(Lnet/minecraft/network/FriendlyByteBuf;)V", at = @At("HEAD"), cancellable = true)
    private void createdelightcore$writeTagKeyWithResolvedStacks(FriendlyByteBuf buffer, CallbackInfo ci) {
        buffer.writeVarInt(CDC_TAG_SYNC_MARKER);
        buffer.writeBoolean(tag != null);
        if (tag != null)
            buffer.writeResourceLocation(tag.location());

        List<FluidStack> stacks = getMatchingFluidStacks();
        buffer.writeVarInt(stacks.size());
        stacks.forEach(buffer::writeFluidStack);
        ci.cancel();
    }

    @Inject(method = "readInternal(Lnet/minecraft/network/FriendlyByteBuf;)V", at = @At("HEAD"), cancellable = true)
    private void createdelightcore$readTagKeyWithResolvedStacks(FriendlyByteBuf buffer, CallbackInfo ci) {
        int size = buffer.readVarInt();
        if (size == CDC_TAG_SYNC_MARKER) {
            if (buffer.readBoolean()) {
                ResourceLocation tagId = buffer.readResourceLocation();
                tag = FluidTags.create(tagId);
            }
            size = buffer.readVarInt();
        }

        List<FluidStack> stacks = new ArrayList<>(size);
        for (int i = 0; i < size; i++)
            stacks.add(buffer.readFluidStack());
        matchingFluidStacks = stacks.isEmpty() && tag != null ? null : stacks;
        ci.cancel();
    }
}
