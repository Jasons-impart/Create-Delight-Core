package io.github.jasonsimpart.createdelightcore.mixin.kaleidoscopetavern;

import com.github.ysbbbbbb.kaleidoscopetavern.item.BottleBlockItem;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import de.cadentem.quality_food.core.Quality;
import de.cadentem.quality_food.util.QualityUtils;
import io.github.jasonsimpart.createdelightcore.compat.kaleidoscopetavern.TavernQuality;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Pseudo
@Mixin(value = BottleBlockItem.class, remap = false)
public abstract class BottleQualityMixin {
    @ModifyReturnValue(method = "getMaxLevelDrink", at = @At("RETURN"))
    private static ItemStack createdelightcore$diamondCreativeBottle(ItemStack result) {
        return TavernQuality.supportsQuality(result) ? TavernQuality.qualify(result, Quality.DIAMOND) : result;
    }

    @Inject(method = "isValidForShaker", at = @At("HEAD"), cancellable = true)
    private static void createdelightcore$requireIron(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (TavernQuality.isQualityWine(stack)) {
            cir.setReturnValue(QualityUtils.getQuality(stack).level() >= Quality.IRON.level());
        }
    }

    @Inject(method = "appendHoverText", at = @At("HEAD"), cancellable = true, remap = true)
    private void createdelightcore$qualityTooltip(ItemStack stack, Level level, List<Component> tooltip,
                                                  TooltipFlag flag, CallbackInfo ci) {
        if (!TavernQuality.isBarrelProduct(stack)) return;
        TavernQuality.appendBottleTooltip(stack, tooltip);
        ci.cancel();
    }
}
