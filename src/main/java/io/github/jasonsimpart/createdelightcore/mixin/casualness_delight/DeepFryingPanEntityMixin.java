package io.github.jasonsimpart.createdelightcore.mixin.casualness_delight;

import com.va11halla.casualness_delight.enity.DeepFryingPanEnity;
import de.cadentem.quality_food.util.QualityUtils;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.List;

@Mixin(DeepFryingPanEnity.class)
public class DeepFryingPanEntityMixin {
    @Final
    @Shadow(remap = false)
    private ItemStackHandler inventory;
    @ModifyArg(method = "cookAndOutputItems", at = @At(value = "INVOKE", target = "Lvectorwing/farmersdelight/common/utility/ItemUtils;spawnItemEntity(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;DDDDDD)V"), index = 1, remap = false)
    public ItemStack applyQuality(ItemStack stack) {
        QualityUtils.applyQuality(stack, List.of(inventory.getStackInSlot(0)), null);
        return stack;
    }

}
