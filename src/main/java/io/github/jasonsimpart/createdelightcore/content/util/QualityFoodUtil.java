package io.github.jasonsimpart.createdelightcore.content.util;

import de.cadentem.quality_food.config.QualityConfig;
import de.cadentem.quality_food.config.ServerConfig;
import de.cadentem.quality_food.core.Modification;
import de.cadentem.quality_food.core.Quality;
import de.cadentem.quality_food.util.QualityUtils;
import de.cadentem.quality_food.util.Utils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import static de.cadentem.quality_food.util.QualityUtils.isValidQuality;

public class QualityFoodUtil {
    private static final RandomSource RANDOM = RandomSource.create();
    public static void applyQuality(ItemStack stack, int count, double weight, @Nullable Player player) {

        if (count == 0) {
            QualityUtils.applyQuality(stack, player);
        } else {
            Quality selected = Quality.NONE;
            double averageWeight = weight / (double) count;

            for (Quality quality : Quality.values()) {
                if (quality.level() != 0) {
                    double chance = QualityConfig.calculateChance(quality, averageWeight);
                    chance = Modification.luck(player).apply(chance);
                    if (chance > 0.0 && chance >= RANDOM.nextDouble()) {
                        selected = quality;
                    }
                }
            }

            if (stack.getTag() != null) {
                stack.getTag().remove(QualityUtils.QUALITY_TAG);
                if (stack.getTag().isEmpty())
                    stack.setTag(null);
            }
            QualityUtils.applyQuality(stack, selected);
        }
    }

}
