package io.github.jasonsimpart.createdelightcore.mixin.fruitsdelight;

import dev.xkmc.fruitsdelight.init.food.FruitType;
import io.github.jasonsimpart.createdelightcore.compat.fruitsdelight.CustomFDFruits;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = FruitType.class, remap = false)
public abstract class FruitTypeMixin {
    @Inject(method = "getJelly()Lnet/minecraft/world/item/Item;", at = @At("HEAD"), cancellable = true, require = 0)
    private void createdelightcore$getCustomJelly(CallbackInfoReturnable<Item> cir) {
        CustomFDFruits.getJelly((FruitType) (Object) this).ifPresent(cir::setReturnValue);
    }

    @Inject(method = "getJello()Lnet/minecraft/world/item/Item;", at = @At("HEAD"), cancellable = true, require = 0)
    private void createdelightcore$getCustomJello(CallbackInfoReturnable<Item> cir) {
        CustomFDFruits.getJello((FruitType) (Object) this).ifPresent(cir::setReturnValue);
    }
}
