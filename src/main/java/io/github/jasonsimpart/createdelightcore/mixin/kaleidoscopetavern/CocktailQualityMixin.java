package io.github.jasonsimpart.createdelightcore.mixin.kaleidoscopetavern;

import com.github.ysbbbbbb.kaleidoscopetavern.item.CocktailBlockItem;
import com.github.ysbbbbbb.kaleidoscopetavern.item.SignatureCocktailBlockItem;
import io.github.jasonsimpart.createdelightcore.compat.kaleidoscopetavern.TavernQuality;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/** Signature cocktails override both methods, so both classes must use the same QF effect carrier. */
@Pseudo
@Mixin(value = {CocktailBlockItem.class, SignatureCocktailBlockItem.class}, remap = false)
public abstract class CocktailQualityMixin {
    @Inject(method = "addDrinkEffect", at = @At("HEAD"), cancellable = true)
    private void createdelightcore$qualityEffects(ItemStack stack, Level level, LivingEntity consumer, CallbackInfo ci) {
        TavernQuality.drink(stack, consumer);
        ci.cancel();
    }

    @Inject(method = "appendHoverText", at = @At("HEAD"), cancellable = true, remap = true)
    private void createdelightcore$qualityTooltip(ItemStack stack, Level level, List<Component> tooltip,
                                                 TooltipFlag flag, CallbackInfo ci) {
        TavernQuality.appendCocktailTooltip(stack, tooltip);
        ci.cancel();
    }
}
