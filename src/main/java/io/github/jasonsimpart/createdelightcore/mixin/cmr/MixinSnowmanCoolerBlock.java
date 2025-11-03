package io.github.jasonsimpart.createdelightcore.mixin.cmr;

import fr.iglee42.cmr.cooler.SnowmanCoolerBlock;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(SnowmanCoolerBlock.class)

public class MixinSnowmanCoolerBlock {
    @Redirect(method = "tryInsert", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V"))
    private static void tryInsert(ItemStack instance, int p_41775_) {
        var handler = instance.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM);
        if (!handler.isPresent()) {
            instance.shrink(p_41775_);
        }
    }
}
