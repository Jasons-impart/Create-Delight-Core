package io.github.jasonsimpart.mixin.cmr;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "fr.iglee42.cmr.cooler.SnowmanCoolerBlock", remap = false)
public class MixinSnowmanCoolerBlock {
    @Redirect(method = "tryInsert", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V"), remap = true)
    private static void createdelightcore$keepDrainedFluidContainer(ItemStack stack, int amount) {
        if (stack.getCapability(Capabilities.FluidHandler.ITEM) == null || stack.hasCraftingRemainingItem()) {
            stack.shrink(amount);
        }
    }
}
