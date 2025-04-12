package io.github.jasonsimpart.createdelightcore.mixin.quality_food;

import de.cadentem.quality_food.config.QualityConfig;
import de.cadentem.quality_food.core.Modification;
import de.cadentem.quality_food.core.Quality;
import de.cadentem.quality_food.util.QualityUtils;
import io.github.jasonsimpart.createdelightcore.CreateDelightCore;
import io.github.jasonsimpart.createdelightcore.content.util.EclipticSeasonsUtil;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static de.cadentem.quality_food.util.QualityUtils.*;

@Mixin(QualityUtils.class)
public abstract class QualityFoodMixin {
    @Unique
    private static final RandomSource create_Delight_Core$RANDOM = RandomSource.create();
    @Inject(method = "applyQuality(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/block/state/BlockState;Lde/cadentem/quality_food/core/Quality;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/block/state/BlockState;)V", at = @At("HEAD"), cancellable = true, remap = false)
    private static void applyQualityMixin(ItemStack stack, BlockState state, Quality blockQuality, Player player, BlockState farmland, CallbackInfo ci) {
        if (isRelevantCrop(state)) {
            Quality selected = Quality.NONE;

            for (Quality quality : Quality.values()) {
                if (quality.level() == 0) {
                    continue;
                }

                double chance;
                if (blockQuality.level() == 0) {
                    // Weight would be 0, meaning no quality can be calculated
                    chance = QualityConfig.getChance(quality);
                } else {
                    chance = QualityConfig.calculateChance(quality, QualityConfig.getWeight(blockQuality));
                }
                chance = Modification.harvestOrSeedMultiplier(quality, stack).apply(chance);
                chance = Modification.luck(player).apply(chance);
                chance = Modification.farmland(state, farmland).apply(chance);
                // 非玩家收割不会拥有品质
                float growChance = 0;
                if (player != null) {
                    growChance = EclipticSeasonsUtil.getGrowChance(player.level(), player.getOnPos(), state);
//                CreateDelightCore.LOGGER.info("growChance:" + growChance);
                }
                chance = Modification.multiplicative(growChance).apply(chance);
                if (chance > 0 && chance >= create_Delight_Core$RANDOM.nextDouble()) {
                    selected = quality;
                }
            }

            applyQuality(stack, selected);
        } else if (isValidQuality(blockQuality)) {
            // The block itself if it has quality
            applyQuality(stack, blockQuality);
        } else if (blockQuality != Quality.NONE_PLAYER_PLACED) {
            // The block itself or harvested items when the crop has no quality
            applyQuality(stack, player);
        }
        ci.cancel();
    }
}
