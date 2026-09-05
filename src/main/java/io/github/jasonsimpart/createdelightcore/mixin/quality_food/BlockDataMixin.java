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

import java.util.Comparator;
import java.util.Deque;
import java.util.Set;
import java.util.TreeSet;

/** Keeps CDR's cooking quality roll while following Quality Food's 2.4 cooking queue API. */
@Mixin(value = BlockData.class, remap = false)
public class BlockDataMixin {
    @Final
    @Shadow
    private Deque<BlockData.CookingEntry> cookingQueue;

    @Final
    @Shadow
    private static RandomSource RANDOM;

    @Inject(method = "useQuality", at = @At("HEAD"), cancellable = true)
    private void useQualityMixin(ItemStack stack, Player player, CallbackInfo ci) {
        if (cookingQueue.isEmpty()) {
            return;
        }

        Set<Quality> qualities = new TreeSet<>(Comparator.comparingInt(Quality::level));
        double qualityBonus = 0;

        for (int i = 0; i < stack.getCount(); i++) {
            BlockData.CookingEntry entry = cookingQueue.poll();

            // Ignore entries whose quality can no longer be resolved by Quality Food.
            if (entry != null) {
                qualityBonus += entry.bonus();
                qualities.add(entry.quality());
            }
        }

        double finalBonus = stack.getCount() == 0 ? 0 : qualityBonus / stack.getCount();
        Quality selected = qualities.stream().findFirst().orElse(Quality.NONE);

        // Use the lowest stored ingredient quality as the base quality.
        if (selected != Quality.NONE) {
            QualityUtils.applyQuality(stack, selected);
        }

        for (Quality quality : Quality.values()) {
            if (quality.level() == 0) {
                continue;
            }

            double chance = RANDOM.nextDouble();
            chance = Modification.luck(player).apply(chance);
            chance = Modification.additive((float) finalBonus / (quality.level() * quality.level())).apply(chance);

            if (chance > 1 - QualityConfig.getChance(quality)) {
                selected = quality;
            }
        }

        if (selected != Quality.NONE) {
            QualityUtils.applyQuality(stack, selected, true);
        }

        ci.cancel();
    }
}
