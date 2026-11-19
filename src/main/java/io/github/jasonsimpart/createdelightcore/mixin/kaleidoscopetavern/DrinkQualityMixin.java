package io.github.jasonsimpart.createdelightcore.mixin.kaleidoscopetavern;

import com.github.ysbbbbbb.kaleidoscopetavern.item.DrinkBlockItem;
import io.github.jasonsimpart.createdelightcore.compat.kaleidoscopetavern.TavernQuality;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(value = DrinkBlockItem.class, remap = false)
public abstract class DrinkQualityMixin {
    @Inject(method = "addDrinkEffect", at = @At("HEAD"), cancellable = true)
    private void createdelightcore$qualityEffects(ItemStack stack, Level level, LivingEntity consumer, CallbackInfo ci) {
        if (!TavernQuality.isBarrelProduct(stack)) return;
        TavernQuality.drink(stack, consumer);
        ci.cancel();
    }
}
