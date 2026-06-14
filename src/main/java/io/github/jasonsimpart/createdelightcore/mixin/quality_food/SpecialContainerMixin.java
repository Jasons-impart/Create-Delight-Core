package io.github.jasonsimpart.createdelightcore.mixin.quality_food;

import de.cadentem.quality_food.compat.SpecialContainer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = SpecialContainer.class, remap = false)
public abstract class SpecialContainerMixin {
    @Final
    @Shadow
    private List<ItemStack> ingredients;

    @Inject(method = {"setItem", "m_6836_"}, at = @At("HEAD"), cancellable = true, remap = false)
    private void createdelightcore$skipOutOfBoundsSlot(int slot, ItemStack stack, CallbackInfo ci) {
        if (slot < 0 || slot >= ingredients.size()) {
            ci.cancel();
        }
    }
}
