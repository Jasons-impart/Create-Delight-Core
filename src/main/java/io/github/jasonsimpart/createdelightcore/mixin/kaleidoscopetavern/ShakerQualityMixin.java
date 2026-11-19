package io.github.jasonsimpart.createdelightcore.mixin.kaleidoscopetavern;

import com.github.ysbbbbbb.kaleidoscopetavern.blockentity.mixology.ShakerBlockEntity;
import de.cadentem.quality_food.core.Quality;
import de.cadentem.quality_food.util.QualityUtils;
import io.github.jasonsimpart.createdelightcore.compat.kaleidoscopetavern.TavernQuality;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(value = ShakerBlockEntity.class, remap = false)
public abstract class ShakerQualityMixin {
    @Inject(method = "addIngredient", at = @At("HEAD"), cancellable = true)
    private void createdelightcore$explainIronRequirement(ItemStack stack, LivingEntity entity,
                                                         CallbackInfoReturnable<Boolean> cir) {
        if (!TavernQuality.isQualityWine(stack) || QualityUtils.getQuality(stack).level() >= Quality.IRON.level()) return;
        if (entity instanceof Player player && !player.level().isClientSide) {
            player.sendSystemMessage(Component.translatable("tooltip.createdelightcore.tavern.shaker_requires_iron"));
        }
        cir.setReturnValue(false);
    }
}
