package io.github.jasonsimpart.mixin.create;

import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BlazeBurnerBlock.class)
public abstract class BlazeBurnerBlockMixin {
    @Redirect(method = "tryInsert", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V"))
    private static void createdelightcore$keepFluidContainer(ItemStack stack, int amount) {
        var handler = stack.getCapability(Capabilities.FluidHandler.ITEM);
        if (handler == null) {
            stack.shrink(amount);
        } else if (stack.hasCraftingRemainingItem()) {
            stack.shrink(amount);
        }
    }
}
