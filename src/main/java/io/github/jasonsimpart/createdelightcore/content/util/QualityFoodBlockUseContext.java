package io.github.jasonsimpart.createdelightcore.content.util;

import de.cadentem.quality_food.capability.LevelData;
import de.cadentem.quality_food.core.Quality;
import de.cadentem.quality_food.util.QualityUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public final class QualityFoodBlockUseContext {
    private static final TagKey<Block> QUALITY_BLOCKS = TagKey.create(
            Registries.BLOCK,
            ResourceLocation.fromNamespaceAndPath("quality_food", "quality_blocks")
    );
    private static final ThreadLocal<Quality> CURRENT_QUALITY = new ThreadLocal<>();

    private QualityFoodBlockUseContext() {
    }

    public static @Nullable Quality push(Level level, BlockPos pos, BlockState state) {
        Quality previous = CURRENT_QUALITY.get();
        Quality quality = state.is(QUALITY_BLOCKS) ? LevelData.get(level, pos) : Quality.NONE;
        if (QualityUtils.isValidQuality(quality)) {
            CURRENT_QUALITY.set(quality);
        } else {
            CURRENT_QUALITY.remove();
        }
        return previous;
    }

    public static void pop(@Nullable Quality previous) {
        if (previous == null) {
            CURRENT_QUALITY.remove();
            return;
        }
        CURRENT_QUALITY.set(previous);
    }

    public static @Nullable Quality getQuality() {
        return CURRENT_QUALITY.get();
    }
}
