package io.github.jasonsimpart.createdelightcore.mixin.quality_food;

import de.cadentem.quality_food.capability.BlockData;
import de.cadentem.quality_food.config.QualityConfig;
import de.cadentem.quality_food.core.Modification;
import de.cadentem.quality_food.core.Quality;
import de.cadentem.quality_food.util.QualityUtils;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Mixin(value = BlockData.class, remap = false)
public class BlockDataMixin {
    @Final
    @Shadow
    private Set<Quality> cookedQualities;

    @Final
    @Shadow
    private static RandomSource RANDOM;

    @Shadow
    private double qualityBonus;

    @Inject(method = "useQuality", at = @At("HEAD"), cancellable = true)
    public void useQualityMixin(ItemStack stack, Player player, CallbackInfo ci) {
        Quality selected = null;

        for (Quality quality : cookedQualities) {
            if (selected == null || quality.level() < selected.level()) {
                selected = quality;
            }
        }

        if (selected != null) {
            QualityUtils.applyQuality(stack, selected);
        } else {
            selected = Quality.NONE;
        }

        for (Quality quality : Quality.values()) {
            if (quality.level() == 0) {
                continue;
            }

            double chance = RANDOM.nextDouble();
            chance = Modification.luck(player).apply(chance);
            chance = Modification.additive((float) qualityBonus).apply(chance);

            if (chance >= 1 - QualityConfig.getChance(quality)) {
                selected = quality;
            }
        }

        QualityUtils.applyQuality(stack, selected, true);

        qualityBonus = 0;
        cookedQualities.clear();
        ci.cancel();
    }

}
