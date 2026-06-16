package io.github.jasonsimpart.createdelightcore.mixin.vintagedelight;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.items.ItemStackHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "net.ribs.vintagedelight.block.entity.FermentingJarBlockEntity")
public class FermentingJarBlockEntityMixin {
    @Shadow(remap = false)
    private ItemStackHandler itemHandler;

    @Unique
    private int[] createdelightcore$consumedIngredientSlots;

    @Inject(method = "craftItem", at = @At("HEAD"), remap = false, require = 0)
    private void createdelightcore$beginIngredientConsumption(CallbackInfo ci) {
        this.createdelightcore$consumedIngredientSlots = new int[6];
    }

    @Inject(method = "craftItem", at = @At("RETURN"), remap = false, require = 0)
    private void createdelightcore$endIngredientConsumption(CallbackInfo ci) {
        this.createdelightcore$consumedIngredientSlots = null;
    }

    @Inject(method = "consumeIngredient", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private void createdelightcore$consumeEachMatchedSlotOnce(Ingredient ingredient, CallbackInfo ci) {
        if (this.createdelightcore$consumedIngredientSlots == null) {
            return;
        }

        int matchingSlot = -1;
        int matchingSlotConsumed = Integer.MAX_VALUE;
        for (int slot = 0; slot <= 5; slot++) {
            ItemStack stack = this.itemHandler.getStackInSlot(slot);
            int consumed = this.createdelightcore$consumedIngredientSlots[slot];
            if (ingredient.test(stack) && stack.getCount() > consumed && consumed < matchingSlotConsumed) {
                matchingSlot = slot;
                matchingSlotConsumed = consumed;
            }
        }

        if (matchingSlot >= 0) {
            this.itemHandler.extractItem(matchingSlot, 1, false);
            this.createdelightcore$consumedIngredientSlots[matchingSlot]++;
        }

        ci.cancel();
    }
}
